/**
 * Copyright (c) 2017, Shahbaz2417
 * <p>
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package com.shaz.library.erp;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.HashMap;

import static com.shaz.library.erp.RuntimePermissionUtils.getPermissionMap;

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
    private static final CommonRuntimePermissionRequest mCommonRuntimePermissionRequest = new CommonRuntimePermissionRequest();
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

        if (mPendingRequest) {
            final HashMap<String, Integer> permissionsMap = getPermissionMap(target, permissions, null);
            int[] grantResults = new int[permissionsMap.size()];
            for (int i = 0; i < grantResults.length; i++)
                grantResults[i] = permissionsMap.get(permissions[i]);
            mPermissionListener.onDenied(mCommonRuntimePermissionRequest, target, requestCode, permissions, grantResults, DENIED_REASON.PENDING);
            return;
        }

        if (RuntimePermissionUtils.hasSelfPermissions(target, permissions)) {
            if (mPermissionListener != null)
                mPermissionListener.onAllowed(requestCode, permissions);
            mPendingRequest = false;
        } else {
            if (RuntimePermissionUtils.shouldShowRequestPermissionRationale(target, permissions)) {
                if (mPermissionListener != null)
                    mPermissionListener.onRationale(mCommonRuntimePermissionRequest, target, requestCode, permissions);
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
                    mPermissionListener.onDenied(mCommonRuntimePermissionRequest, target, requestCode, permissions, grantResults, DENIED_REASON.USER);
            }
        }
    }

    private static final class CommonRuntimePermissionRequest implements PermissionRequest {

        private CommonRuntimePermissionRequest() {
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
                mPermissionListener.onDenied(mCommonRuntimePermissionRequest, target, requestCode, permissions, grantResults, DENIED_REASON.USER);
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

}
