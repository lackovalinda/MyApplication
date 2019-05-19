package com.example.draftapplication;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    String preferences_name;
    private static final int PERMISSION_ALL = 100;
    private static int SPLASH_TIME_OUT = 3000;
    boolean askOnceAgain = false;
    List<String> permissionsList = new ArrayList<>();

    public SplashActivity() {
        preferences_name = "isFirstTime";
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        Thread go = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(SPLASH_TIME_OUT);
                    if (checkPermissions()) {
                        firstTime();}
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                    }
                }
            };
            go.start();
       }


    public  void  firstTime(){

            SharedPreferences sharedTime = getSharedPreferences(preferences_name, 0);
            if (sharedTime.getBoolean("firstTime", true)) {
                Intent intent = new Intent(this, FirstActivity.class);
                startActivity(intent);
                sharedTime.edit().putBoolean("firstTime", false).apply();
                finish();
            } else {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
    }


    private void showCustomDialog(String message, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(SplashActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", listener)
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                boolean required = false;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        Log.d("Permissions", "Permission Granted: " + permissions[i]);
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        Log.d("Permissions", "Permission Denied: " + permissions[i]);
                        required = true;
                    }
                }
                if (required) {
                    showCustomDialog("You need to allow access to some permissions.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    askOnceAgain = true;
                                    finish();
                                }
                            });
                } else {
                    firstTime();
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        permissionsList.clear();
        if (askOnceAgain) {
            askOnceAgain = false;
            checkPermissions();
        }
    }


    private  boolean checkPermissions(){

        String[] PERMISSIONS = {
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE
        };

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:PERMISSIONS) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSION_ALL);
            return false;
        }
        firstTime();
        return true;
    }
}
