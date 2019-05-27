package com.example.draftapplication;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

public class InfoActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private static final int MY_DATA_CHECK_CODE = 100;
    private ImageButton btn;
    private TextToSpeech textToSpeech;
    private String record = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);
        btn = (ImageButton) findViewById(R.id.button1);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("About");
        TextView tv1 = findViewById(R.id.textView);
        String text = "This app was delivered as a product of bachelor thesis. Its potential could contribute to community of visual impaired and blind people. \n\n" +
                "Source of used icon: http://iconshow.me/ \nBackend and message services provided by FireBase. \n \n" +
                "Instruction to use: \n- user is able to sign up directly via google account or he could use any existing email to register \n" +
                "\nLONG PRESS on button enables voice output \nPINK BUTTONS with SPEAKER ICON reads the whole page";

        tv1.setText(text);

        btn.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
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

    private void saySomething(String text, int qmode) {
        if (qmode == 1) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
        else
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }
}
