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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;
import android.support.v4.util.SimpleArrayMap;

import java.util.HashMap;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Created by ${Shahbaz} on 19-07-2017.
 */

public final class RuntimePermissionUtils {

    /*public enum Permission{
        Storage, Contact, Location, Sms, Phone, MicroPhone, Camera, Calendar, BodySensor

        private String[] get(){
            switch (this){
                case Storage:
                    return StoragePermission;
                case Contact:
                    return ContactPermission;
                case Location:
                    return LocationPermission;
                case Sms:
                    return SmsPermission;
                case Phone:
                    return PhonePermission;
                case MicroPhone:
                    return MicroPhonePermission;
                case Camera:
                    return CameraPermission;
                case Calendar:
                    return CalendarPermission;
                case BodySensor:
                    return BodySensorPermission;
            }
            return null;
        }
    }*/

    // Map of dangerous permissions introduced in later framework versions.
    // Used to conditionally bypass permission-hold checks on older devices.
    private static final SimpleArrayMap<String, Integer> MIN_SDK_PERMISSIONS;

    public static final String[] StoragePermission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] ContactPermission = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.GET_ACCOUNTS};
    public static final String[] LocationPermission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] SmsPermission = {Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECEIVE_MMS};
    public static final String[] PhonePermission = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.ADD_VOICEMAIL, Manifest.permission.USE_SIP};
    public static final String[] MicroPhonePermission = {Manifest.permission.RECORD_AUDIO};
    public static final String[] CameraPermission = {Manifest.permission.CAMERA};
    public static final String[] CalendarPermission = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
    public static final String[] BodySensorPermission = {Manifest.permission.BODY_SENSORS};

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

    /**
     * Join multiple permissions and pass for request
     */
    public static String[] join(String[]... arrays) {
        // calculate size of target array
        int size = 0;
        for (String[] array : arrays) {
            size += array.length;
        }

        // create list of appropriate size
        java.util.List list = new java.util.ArrayList(size);

        // add arrays
        for (String[] array : arrays) {
            list.addAll(java.util.Arrays.asList(array));
        }

        // create and return final array
        return (String[]) list.toArray(new String[size]);
    }

    public static void openAppSettings(Context context) {
        if (context == null)
            return;
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * To get HashMap of permissions
     * Key as a permission
     * Value as a grant/denied int Integer
     *
     * @param target
     * @param permissions
     * @param grantResults // Pass null if hasSelfPermissions check required
     * @return hashmap
     */
    public static HashMap<String, Integer> getPermissionMap(@NonNull Activity target, String[] permissions, int[] grantResults) {
        if (permissions != null) {
            if (grantResults == null || grantResults.length != permissions.length) {
                grantResults = new int[permissions.length];
                for (int i = 0; i < grantResults.length; i++)
                    grantResults[i] = hasSelfPermissions(target, permissions[i]) ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
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
