package com.flyingcodes.guidedog.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.flyingcodes.guidedog.R;

public class SplashActivity extends AppCompatActivity {
    private final SplashActivity instance = this;
    private final String TAG = "SplashActivity";

    private static final int PERMISSION_REQUEST_CODE = 0x01;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 0x01) {
                Intent intent = new Intent(SplashActivity.this, GuideDogSearchActivity.class);
                startActivity(intent);
                finish();
                handler.removeMessages(0x01);//移除多余通知
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onPermissionGranted();
            return;
        }
//        handler.sendEmptyMessageDelayed(0x01, 1000);

        String[] permissions = {
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };


        if (checkPermissionAllGranted(permissions)) {
            onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(instance, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void onPermissionGranted() {
        handler.sendEmptyMessageDelayed(0x01, 100);
    }

    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Authorization required");
        builder.setPositiveButton("do it", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("cancel", null);
        builder.show();
    }


    /**
     * check all permission complete
     * @param permissions
     * @return
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Permission not complete：" + permission);
                return false;//Permission not complete
            }else{
                Log.e(TAG, "Permission complete：" + permission);
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // check all permission complete
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                //permission complete
                onPermissionGranted();

            } else {
                // need to be authorized
                openAppDetails();
            }

        }
    }
}
