package com.suridosa.callpopup;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suridosa on 2018-02-12.
 */

public class Splash extends AppCompatActivity {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2084;

    private String[] permissions = {
             Manifest.permission.READ_PHONE_STATE
            ,Manifest.permission.READ_CONTACTS
            ,Manifest.permission.INTERNET
            ,Manifest.permission.ACCESS_FINE_LOCATION
            ,Manifest.permission.RECEIVE_BOOT_COMPLETED
            ,Manifest.permission.RECEIVE_SMS
            ,Manifest.permission.SEND_SMS
            ,Manifest.permission.CALL_PHONE
            ,Manifest.permission.PROCESS_OUTGOING_CALLS
//            ,Manifest.permission.SYSTEM_ALERT_WINDOW
    };

    private static final int MULTIPLE_PERMISSIONS = 101;

    boolean permissionResult;

    public void onCreate(Bundle saveInstanceState) {
        //setTheme(R.style.AppTheme);
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_splash);

        permissionResult = true;

        if(Build.VERSION.SDK_INT >= 23) { //안드로이드 6.0 이상일 경우 퍼미션 체크
            //permissionResult = checkPermissions();
            if ( !Settings.canDrawOverlays(this)) {
                askOverlayPermission();
            }
        }

        permissionResult = checkPermissions();

        if( permissionResult ) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            finish();
        }


    }


    private void askOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("Splash","onActivityResult :: "+requestCode);
        // Check which request we're responding to
        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION) {
            // Make sure the request was successful
                permissionResult = true;
        } else {
            finish();
        }
    }

   private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for(String pm: permissions) {
            result = ContextCompat.checkSelfPermission(this, pm) ;
            if( result != PackageManager.PERMISSION_GRANTED ) {
                permissionList.add(pm);
            }
        }

        if(!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case MULTIPLE_PERMISSIONS :
                if(grantResults.length > 0 ) {
                    for(int i=0; i<permissions.length; i++) {
                        if(permissions[i].equals(this.permissions[i])) {
                            if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }


                } else {
                    showToast_PermissionDeny();
                }

                break;
        }
    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용이 가능합니다.\n설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

}
