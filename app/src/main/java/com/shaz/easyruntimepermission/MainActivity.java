package com.shaz.easyruntimepermission;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.shaz.library.erp.RuntimePermissionHandler;
import com.shaz.library.erp.RuntimePermissionUtils;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_CAMERA_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openCamera(View view) {
        RuntimePermissionHandler.requestPermission(REQ_CODE_CAMERA_PERMISSION, this, mPermissionListener, RuntimePermissionUtils.CameraPermission);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        RuntimePermissionHandler.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private RuntimePermissionHandler.PermissionListener mPermissionListener = new RuntimePermissionHandler.PermissionListener() {
        @Override
        public void onRationale(final @NonNull RuntimePermissionHandler.PermissionRequest permissionRequest, final Activity target, final int requestCode, @NonNull final String[] permissions) {
            switch (requestCode) {
                case REQ_CODE_CAMERA_PERMISSION:
                    new AlertDialog.Builder(target)
                            .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    permissionRequest.proceed(target, requestCode, permissions);
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    permissionRequest.cancel(target, requestCode, permissions);
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .setMessage(R.string.camera_permission_rational)
                            .show();
                    break;
            }
        }

        @Override
        public void onAllowed(int requestCode, @NonNull String[] permissions) {
            switch (requestCode) {
                case REQ_CODE_CAMERA_PERMISSION:
                    openCamera();
                    break;
            }
        }

        @Override
        public void onDenied(final @NonNull RuntimePermissionHandler.PermissionRequest permissionRequest, Activity target, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, RuntimePermissionHandler.DENIED_REASON deniedReason) {
            if (deniedReason == RuntimePermissionHandler.DENIED_REASON.USER) {
                switch (requestCode) {
                    case REQ_CODE_CAMERA_PERMISSION:
                        Toast.makeText(target, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }

        @Override
        public void onNeverAsk(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQ_CODE_CAMERA_PERMISSION:
                    new AlertDialog.Builder(MainActivity.this)
                            .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    RuntimePermissionUtils.openAppSettings(MainActivity.this);
                                }
                            })
                            .setNegativeButton(R.string.deny, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(@NonNull DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .setMessage(R.string.camera_pemission_never_ask)
                            .show();
                    break;
            }
        }
    };

    private void openCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivity(intent);
    }
}
