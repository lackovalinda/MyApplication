package com.example.draftapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Locale;

public class ChattingActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private ListView listOfMessages;
    private static final int USER_DATA = 1;
    private static final int MY_DATA_CHECK_CODE = 100;
    private static final int RESULT_SPEECH = 1000;
    private String myUid, otherUserUid, threadId, text, record;
    private ImageButton btn_sendMessage, btn_sendVoiceMessage;
    private EditText input;
    private boolean groupChat = false;
    private TextToSpeech textToSpeech;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_window);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent intent = getIntent();
        User otherUser = (User) intent.getSerializableExtra("otherUser");
        if (otherUser == null){
            groupChat = true;
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        listOfMessages = (ListView) findViewById(R.id.list_of_messages);
        btn_sendMessage = (ImageButton) findViewById(R.id.sendMessage);
        btn_sendVoiceMessage = (ImageButton) findViewById(R.id.sendVoiceMessage);
        input = (EditText)findViewById(R.id.input);
        if (!groupChat) {
            myToolbar.setTitle("Chat with " + otherUser.getUsername());
        }
        else{
            myToolbar.setTitle("Group chat");
        }
        setSupportActionBar(myToolbar);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                String id = item.getTitle().toString();

                switch (id) {
                    case "Users":
                        startActivity(new Intent(getApplicationContext(), UserListActivity.class));
                        break;
                    case "About":
                        startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                        break;
                    case "Profile":
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        break;
                    case "Settings":
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case "Logout user":
                        mAuth.signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                        break;
                    case "Delete user":
                        if (user != null) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getApplicationContext(), "User deleted", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                            }
                                        }
                                    });
                        }
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "default: " + item.getTitle(), Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });

        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = input.getText().toString();
                sendMessages(text, user.getDisplayName(), user.getUid());
                input.setText("");
            }
        });

        btn_sendVoiceMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),"Your device doesn't support Speech to Text", Toast.LENGTH_LONG).show();
                }
            }
        });

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        threadId = "thread1";

        if (!groupChat) {
            myUid = user.getUid();
            otherUserUid = otherUser.getId();
            threadId = getThreadId();
        }
        displayChatMessages(USER_DATA, 1, getIntent());
        listOfMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {
                    }
                });

                Intent checkTTSIntent = new Intent();
                checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);

                ChatMessage msg = (ChatMessage) listOfMessages.getItemAtPosition(position);
                record = msg.getMessageText();

                return true;
            }
        });
    }

    private void displayChatMessages(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Globals g = (Globals)getApplication();
        g.setUserId(myUid);

        Query query;
        if (!groupChat) {
            query = FirebaseDatabase.getInstance().getReference("Messages/threads").child(threadId);
        }
        else{
            query = FirebaseDatabase.getInstance().getReference("Messages/threads").child("thread1");
        }
        FirebaseListOptions<ChatMessage> options =
                new FirebaseListOptions.Builder<ChatMessage>()
                        .setQuery(query, ChatMessage.class)
                        .setLayout(R.layout.message)
                        .build();

        FirebaseListAdapter<ChatMessage> adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));

                if(model.getMessageUserID().equals("HY95Ptm5hdNy9q3vVNTEOGgyjdk1"))
                    v.setRight(position);
                else
                    v.setLeft(position);


            }
        };
        assert listOfMessages != null;
        adapter.startListening();
        listOfMessages.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.button_bar, menu);
        return true;
    }

    public String getThreadId() {
        String threadId = null;

        int result = myUid.compareTo(otherUserUid);
        Toast.makeText(getApplicationContext(), "result je: " + result, Toast.LENGTH_LONG).show();

        if (result < 0) {
            threadId = myUid + otherUserUid;
        } else if (result > 0) {
            threadId = otherUserUid + myUid;
        } else {
            threadId = "thread1";
        }

        return threadId;
    }

    public void sendMessages(String text, String name, String uid) {

        FirebaseDatabase.getInstance()
                .getReference("Messages/threads").child(threadId)
                .push()
                .setValue(new ChatMessage(text, name, uid));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case RESULT_SPEECH:
                if (data != null) {
                    ArrayList<String> textik = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    text = textik.get(0);
                    Toast.makeText(getApplicationContext(), text,  Toast.LENGTH_SHORT).show();
                    myUid = mAuth.getCurrentUser().getUid();
                    sendMessages(text, user.getDisplayName(), myUid);
                } else {
                    Toast.makeText(getApplicationContext(), "You have not recorded any messsage", Toast.LENGTH_SHORT).show();
                }
                break;
            case MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    textToSpeech.setLanguage(Locale.UK);
                    textToSpeech = new TextToSpeech(getApplicationContext(), this);
                } else {
                    Intent installIntent = new Intent();
                    installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (textToSpeech != null) {
            textToSpeech.stop();
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
        if (qmode == 1)
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
        else
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public String getUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

}