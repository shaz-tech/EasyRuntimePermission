package com.shaz.library.erp;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.HashMap;

/**
 * Created by ${Shahbaz} on 19-07-2017.
 */

public final class RuntimePermissionHandler {

    public enum DENIED_REASON {
        USER, PENDING
    }

    public interface PermissionListener {
        void onRationale(@NonNull PermissionRequest permissionRequest, Activity target, int requestCode, @NonNull String[] permissions);

        void onAllowed(int requestCode, @NonNull String[] permissions);

        void onDenied(@NonNull PermissionRequest permissionRequest, Activity target, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, DENIED_REASON deniedReason);

        void onNeverAsk(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
    }

    private static PermissionListener mPermissionListener;
    private static final InvokeCommonPermissionPermissionRequest mInvokeCommonPermissionPermissionRequest = new InvokeCommonPermissionPermissionRequest();
    private static boolean mPendingRequest;

    public static synchronized void requestPermission(int requestCode, @NonNull Activity target, @NonNull PermissionListener listener, @NonNull String... permissions) {

        if (target == null || permissions == null)
            return;

        mPermissionListener = listener;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (mPermissionListener != null)
                mPermissionListener.onAllowed(requestCode, permissions);
            mPendingRequest = false;
            return;
        }

        if(mPendingRequest) {
            final HashMap<String, Integer> permissionsMap = getPermissionMap(target, permissions, null);
            int[] grantResults = new int[permissionsMap.size()];
            for (int i = 0; i < grantResults.length; i++)
                grantResults[i] = permissionsMap.get(permissions[i]);
            mPermissionListener.onDenied(mInvokeCommonPermissionPermissionRequest, target, requestCode, permissions, grantResults, DENIED_REASON.PENDING);
            return;
        }

        if (RuntimePermissionUtils.hasSelfPermissions(target, permissions)) {
            if (mPermissionListener != null)
                mPermissionListener.onAllowed(requestCode, permissions);
            mPendingRequest = false;
        } else {
            if (RuntimePermissionUtils.shouldShowRequestPermissionRationale(target, permissions)) {
                if (mPermissionListener != null)
                    mPermissionListener.onRationale(mInvokeCommonPermissionPermissionRequest, target, requestCode, permissions);
                mPendingRequest = false;
            } else {
                ActivityCompat.requestPermissions(target, permissions, requestCode);
                mPendingRequest = true;
            }
        }
    }

    public static void onRequestPermissionsResult(Activity target, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (RuntimePermissionUtils.verifyPermissions(grantResults)) {
            mPendingRequest = false;
            if (mPermissionListener != null)
                mPermissionListener.onAllowed(requestCode, permissions);
        } else {
            if (!RuntimePermissionUtils.shouldShowRequestPermissionRationale(target, permissions)) {
                mPendingRequest = false;
                if (mPermissionListener != null)
                    mPermissionListener.onNeverAsk(requestCode, permissions, grantResults);
            } else {
                mPendingRequest = false;
                if (mPermissionListener != null)
                    mPermissionListener.onDenied(mInvokeCommonPermissionPermissionRequest, target, requestCode, permissions, grantResults, DENIED_REASON.USER);
            }
        }
    }

    private static final class InvokeCommonPermissionPermissionRequest implements PermissionRequest {

        private InvokeCommonPermissionPermissionRequest() {
        }

        @Override
        public void proceed(Activity target, int requestCode, String... permissions) {
            ActivityCompat.requestPermissions(target, permissions, requestCode);
            mPendingRequest = true;
        }

        @Override
        public void cancel(Activity target, int requestCode, String... permissions) {
            final HashMap<String, Integer> permissionsMap = getPermissionMap(target, permissions, null);
            int[] grantResults = new int[permissionsMap.size()];
            for (int i = 0; i < grantResults.length; i++)
                grantResults[i] = permissionsMap.get(permissions[i]);
            if (mPermissionListener != null)
                mPermissionListener.onDenied(mInvokeCommonPermissionPermissionRequest, target, requestCode, permissions, grantResults, DENIED_REASON.USER);
            mPendingRequest = false;
        }
    }

    /**
     * Interface used by methods to allow for continuation
     * or cancellation of a permission request.
     */
    public interface PermissionRequest {

        void proceed(Activity target, int requestCode, String... permissions);

        void cancel(Activity target, int requestCode, String... permissions);
    }

    /**
     * To get HashMap of permissions
     * Key as a permission
     * Value as a grant/denied int Integer
     * @param target
     * @param permissions
     * @param grantResults // Pass null if hasSelfPermissions check required
     */
    public static HashMap<String, Integer> getPermissionMap(@NonNull Activity target, String[] permissions, int[] grantResults) {
        if (permissions != null) {
            if (grantResults == null || grantResults.length != permissions.length) {
                grantResults = new int[permissions.length];
                for (int i = 0; i < grantResults.length; i++)
                    grantResults[i] = RuntimePermissionUtils.hasSelfPermissions(target, permissions[i]) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            }
            HashMap<String, Integer> map = new HashMap<>(permissions.length);
            for (int i = 0; i < permissions.length; i++) {
                try {
                    map.put(permissions[i], grantResults[i]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    continue;
                }
            }
            return map;
        }
        return null;
    }

}
