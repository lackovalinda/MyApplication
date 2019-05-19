package com.example.draftapplication;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FirstActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private static final int MY_DATA_CHECK_CODE = 1;
    private String text, record;

    private TextToSpeech textToSpeech;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.first_activity);
        getSupportActionBar().setTitle("Welcome to the application");

        TextView tv = (TextView) findViewById(R.id.textView);
        ImageButton b1 = (ImageButton) findViewById(R.id.button1);
        ImageButton b2 = (ImageButton) findViewById(R.id.button2);

        text = getString(R.string.instructions);

        tv.setText(text);
        Globals g = (Globals)getApplication();
        g.setSettings(true);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {
                    }
                });

                Intent checkTTSIntent = new Intent();
                checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

                record = text;
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }});
    }


    private void saySomething(String text, int qmode) {
        if (qmode == 1) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
        else
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    textToSpeech.setLanguage(Locale.UK);
                    textToSpeech = new TextToSpeech(this, this);
                } else {
                    Intent installIntent = new Intent();
                    Toast.makeText(getApplicationContext(), "installing TTS", Toast.LENGTH_SHORT).show();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
                break;
        }
    }


    public void printhashkey(){

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.mytrendin.keyhash",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (textToSpeech != null) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
                } else {
                    if (record != null) {
                        saySomething(record, 0);
                        record = null;
                    }else {
                        saySomething("TTS is ready", 0);
                    }
                }
            }
        } else {
            Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_LONG).show();
        }
    }

}