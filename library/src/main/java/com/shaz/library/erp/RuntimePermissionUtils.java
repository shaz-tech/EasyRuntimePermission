package com.shaz.library.erp;

/**
 * Created by ${Shahbaz} on 19-07-2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.util.SimpleArrayMap;

import java.util.ArrayList;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public final class RuntimePermissionUtils {
    // Map of dangerous permissions introduced in later framework versions.
    // Used to conditionally bypass permission-hold checks on older devices.
    private static final SimpleArrayMap<String, Integer> MIN_SDK_PERMISSIONS;

    public static final String[] allPermissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            , android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.GET_ACCOUNTS
            , android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION
            , android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.PROCESS_OUTGOING_CALLS
            , android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS};

    public static final String[] compulStorageCntctPerm = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            , android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS};

    public static final String[] storagePermission = {android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] contactPermission = {android.Manifest.permission.READ_CONTACTS, android.Manifest.permission.WRITE_CONTACTS, android.Manifest.permission.GET_ACCOUNTS};
    public static final String[] locationPermission = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] smsPermission = {android.Manifest.permission.SEND_SMS, android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS};
    public static final String[] phonePermission = {android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.READ_CALL_LOG, android.Manifest.permission.PROCESS_OUTGOING_CALLS, android.Manifest.permission.CALL_PHONE};
    public static final String[] microPhonePermission = {android.Manifest.permission.RECORD_AUDIO};
    public static final String[] cameraPermission = {android.Manifest.permission.CAMERA};

    static {
        MIN_SDK_PERMISSIONS = new SimpleArrayMap<>(8);
        MIN_SDK_PERMISSIONS.put("com.android.voicemail.permission.ADD_VOICEMAIL", 14);
        MIN_SDK_PERMISSIONS.put("android.permission.BODY_SENSORS", 20);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.READ_EXTERNAL_STORAGE", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.USE_SIP", 9);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_CALL_LOG", 16);
        MIN_SDK_PERMISSIONS.put("android.permission.SYSTEM_ALERT_WINDOW", 23);
        MIN_SDK_PERMISSIONS.put("android.permission.WRITE_SETTINGS", 23);
    }

    private RuntimePermissionUtils() {
    }

    /**
     * Checks all given permissions have been granted.
     *
     * @param grantResults results
     * @return returns true if all permissions have been granted.
     */
    public static boolean verifyPermissions(int... grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the permission exists in this SDK version
     *
     * @param permission permission
     * @return returns true if the permission exists in this SDK version
     */
    private static boolean permissionExists(String permission) {
        // Check if the permission could potentially be missing on this device
        Integer minVersion = MIN_SDK_PERMISSIONS.get(permission);
        // If null was returned from the above call, there is no need for a device API level check for the permission;
        // otherwise, we check if its minimum API level requirement is met
        return minVersion == null || Build.VERSION.SDK_INT >= minVersion;
    }

    /**
     * Returns true if the Activity or Fragment has access to all given permissions.
     *
     * @param context     context
     * @param permissions permission list
     * @return returns true if the Activity or Fragment has access to all given permissions.
     */
    public static boolean hasSelfPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (permissionExists(permission) && !hasSelfPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine context has access to the given permission.
     * <p>
     * This is a workaround for RuntimeException of Parcel#readException.
     * For more detail, check this issue https://github.com/hotchemi/PermissionsDispatcher/issues/107
     *
     * @param context    context
     * @param permission permission
     * @return returns true if context has access to the given permission, false otherwise.
     * @see #hasSelfPermissions(Context, String...)
     */
    private static boolean hasSelfPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && "Xiaomi".equalsIgnoreCase(Build.MANUFACTURER)) {
            return hasSelfPermissionForXiaomi(context, permission);
        }
        try {
            return checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } catch (RuntimeException t) {
            return false;
        }
    }

    private static boolean hasSelfPermissionForXiaomi(Context context, String permission) {
        String permissionToOp = AppOpsManagerCompat.permissionToOp(permission);
        if (permissionToOp == null) {
            // in case of normal permissions(e.g. INTERNET)
            return true;
        }
        int noteOp = AppOpsManagerCompat.noteOp(context, permissionToOp, Process.myUid(), context.getPackageName());
        return noteOp == AppOpsManagerCompat.MODE_ALLOWED && checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks given permissions are needed to show rationale.
     *
     * @param activity    activity
     * @param permissions permission list
     * @return returns true if one of the permission is needed to show rationale.
     */
    public static boolean shouldShowRequestPermissionRationale(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks given permissions are needed to show rationale.
     *
     * @param fragment    fragment
     * @param permissions permission list
     * @return returns true if one of the permission is needed to show rationale.
     */
    public static boolean shouldShowRequestPermissionRationale(android.support.v4.app.Fragment fragment, String... permissions) {
        for (String permission : permissions) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    public static ArrayList<String> getPermissionGroups(String[] permissions){
        ArrayList<String> list = new ArrayList<>();
        for(String s : permissions){
            switch(s){
                case android.Manifest.permission.READ_EXTERNAL_STORAGE:
                case android.Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    if(!list.contains(STORAGE)) {
                        list.add(STORAGE);
                    }
                    break;

                case android.Manifest.permission.READ_CONTACTS:
                case android.Manifest.permission.WRITE_CONTACTS:
                case android.Manifest.permission.GET_ACCOUNTS:
                    if(!list.contains(CONTACTS))
                        list.add(CONTACTS);
                    break;

                case android.Manifest.permission.ACCESS_FINE_LOCATION:
                case android.Manifest.permission.ACCESS_COARSE_LOCATION:
                    if(!list.contains(LOCATION))
                        list.add(LOCATION);
                    break;

                case android.Manifest.permission.READ_PHONE_STATE:
                case android.Manifest.permission.READ_CALL_LOG:
                case android.Manifest.permission.PROCESS_OUTGOING_CALLS:
                case android.Manifest.permission.CALL_PHONE:
                    if(!list.contains(PHONE))
                        list.add(PHONE);
                    break;

                case android.Manifest.permission.SEND_SMS:
                case android.Manifest.permission.READ_SMS:
                case android.Manifest.permission.RECEIVE_SMS:
                    if(!list.contains(SMS))
                        list.add(SMS);
                    break;


                case android.Manifest.permission.RECORD_AUDIO:
                    if(!list.contains(MICROPHONE))
                        list.add(MICROPHONE);
                    break;

                case android.Manifest.permission.CAMERA:
                    if(!list.contains(CAMERA))
                        list.add(CAMERA);
                    break;
            }
        }
        return list;
    }

    //ToDo needs to push these strings in strings.xml
    public static final String STORAGE = "Storage Permissions";
    public static final String CONTACTS = "Contacts";
    public static final String LOCATION = "Location Service";
    public static final String PHONE = "Phone States";
    public static final String SMS = "Sms Services";
    public static final String MICROPHONE = "Microphone Service";
    public static final String CAMERA = "Camera";


}
