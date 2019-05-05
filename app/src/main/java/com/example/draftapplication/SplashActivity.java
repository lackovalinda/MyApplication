package com.example.draftapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    String preferences_name;
    Globals g;
    boolean condition = false;

    public SplashActivity() {
        preferences_name = "isFirstTime";
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        g = (Globals)getApplication();
        condition = g.getPermissionsAccepted();

        Thread go = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(3000);
                    firstTime();

                }
                    catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        };
        go.start();
    }

    public  void  firstTime(){

        SharedPreferences sharedTime = getSharedPreferences(preferences_name,0);
            if (sharedTime.getBoolean("firstTime", true)) {
                Intent intent = new Intent(getApplicationContext(), FirstActivity.class);
                startActivity(intent);
                sharedTime.edit().putBoolean("firstTime", false).apply();
                finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

    }
}
