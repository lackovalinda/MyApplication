package com.example.draftapplication;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class UserListActivity extends AppCompatActivity implements  TextToSpeech.OnInitListener,  AdapterView.OnItemSelectedListener, Serializable {

    private static final int MY_DATA_CHECK_CODE = 1;
    private static final int USER_CODE = 100;
    private static final int RESULT_SPEECH = 1000;
    private FirebaseListAdapter<User> adapter;
    private ListView listOfUsers;
    private TextToSpeech textToSpeech;
    private EditText email;
    private Set<User> users = new HashSet<>();
    private Query query, dbref;
    private String input, uid, record = null, text, myUid;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Dialog myDialog;
    private User user1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_user_activity);
        myDialog = new Dialog(this);

        FloatingActionButton btn_record = (FloatingActionButton) findViewById(R.id.record_btn);
        FloatingActionButton btn_find = (FloatingActionButton) findViewById(R.id.find);

        email = (EditText) findViewById(R.id.profileEmail);
        listOfUsers = (ListView) findViewById(R.id.list_of_users);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myUid = user.getUid();
        text = "text message";

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Find the user");
        setSupportActionBar(myToolbar);

        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String id = item.getTitle().toString();
                switch (id) {
                    case "Users":
                        //startActivity(new Intent(getApplicationContext(), UserListActivity.class));
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
                        Toast.makeText(getApplicationContext(), "User signed out", Toast.LENGTH_SHORT).show();
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

        btn_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input = email.getText().toString();
                query = FirebaseDatabase.getInstance().getReference().child("Users");
                displayUsers(USER_CODE,1,getIntent());
            }
        });

        listOfUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                user1 = (User) listOfUsers.getItemAtPosition(position);
                showPopup(getCurrentFocus(), user1);
            }
        });

        listOfUsers.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
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

                User user = (User) listOfUsers.getItemAtPosition(position);
                record = user.getUsername();

                return true;
            }
        });
    }

    protected void onActivityResult ( int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
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

                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    break;
        }
    }

    private void displayUsers(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {

                    if (input != null && userSnapShot.getValue(User.class).getEmail().equals(email.getText().toString())){

                        users.add(userSnapShot.getValue(User.class));
                        uid = userSnapShot.getValue(User.class).getId();
                        setUid(uid);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });


        if(dbref != null) {
            FirebaseListOptions<String> options1;
            options1 = new FirebaseListOptions.Builder<String>()
                            .setQuery(dbref, String.class)
                            .setLayout(R.layout.user_item)
                            .build();
            FirebaseListAdapter<String> adapter1;
            adapter1 = new FirebaseListAdapter<String>(options1){

                @Override
                protected void populateView(View v, String model, int position) {
                    TextView email = (TextView) v.findViewById(R.id.profileEmail);
                    TextView name = (TextView) v.findViewById(R.id.username);
                    ImageView photo = (ImageView) v.findViewById(R.id.photo);

                    if (position == 0) {
                        Toast.makeText(UserListActivity.this, model, Toast.LENGTH_SHORT).show();
                    }
                }
            };

        }
        else {
            FirebaseListOptions<User> options;
           options =  new FirebaseListOptions.Builder<User>()
                            .setQuery(query, User.class)
                            .setLayout(R.layout.user_item)
                            .build();


        adapter = new FirebaseListAdapter<User>(options){
            @Override
            protected void populateView(View v,User model, int position) {
                TextView email = (TextView) v.findViewById(R.id.profileEmail);
                TextView name = (TextView) v.findViewById(R.id.username);
                ImageView photo = (ImageView) v.findViewById(R.id.photo);

                email.setText(model.getEmail());
                name.setText(model.getUsername());

                String id = model.getId();
                final long ONE_MEGABYTE = 1024 * 1024;
                StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(id);

                if (uid != null) {
                    //Toast.makeText(UserListActivity.this, "referencia na storage " + storageRef.toString(), Toast.LENGTH_SHORT).show();
                    storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                        Bitmap imageBitmap = null;
                        try {
                            final int THUMBNAIL_SIZE = 64;
                            imageBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_SIZE, THUMBNAIL_SIZE, false);
                            photo.setImageBitmap(imageBitmap);
                        } catch (Exception ex) {
                        }


                    }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception exception) {
                                int errorCode = ((StorageException) exception).getErrorCode();
                                if (errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                    //StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child("Photos").child("photo_1.png");

                                }

                            }

                    });

                }
        }};
        assert listOfUsers != null;
        adapter.startListening();
        listOfUsers.setAdapter(adapter);
    }
    }

    @Override
    public void onStop() {
        super.onStop();
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.button_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setUid(String uid1){
        dbref = FirebaseDatabase.getInstance().getReference().child("Users/"+ uid1);
    }

    public void showPopup(View v, User user1) {

        TextView btn_close, username, email;
        ImageButton btn_sendMessage, btn_sendVoiceMessage;

        myDialog.setContentView(R.layout.user_popup);
        btn_close =(TextView) myDialog.findViewById(R.id.close);
        btn_sendMessage = (ImageButton) myDialog.findViewById(R.id.sendMessage);
        btn_sendVoiceMessage = (ImageButton) myDialog.findViewById(R.id.sendVoiceMessage);
        username = (TextView) myDialog.findViewById(R.id.userName);
        email = (TextView) myDialog.findViewById(R.id.profileEmail);

        btn_close.setText("X");
        email.setText(user1.getEmail());
        username.setText(user1.getUsername());

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        btn_sendVoiceMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {
                    }
                });
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                //intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000000);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition Demo...");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_LONG);
                    t.show();
                }

                //sendMessages(text, user.getDisplayName(), uid);

            }
        });

        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UserListActivity.this, "send message to " + user1.getUsername() + " ?", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), ChattingActivity.class);
                i.putExtra("otherUser", user1);
                startActivity(i);

            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }


    public void sendMessages(String text, String name, String uid) {

        String thread = getThreadId();

        if (thread != null) {
            FirebaseDatabase.getInstance()
                    .getReference("Messages/threads").child(thread)
                    .push()
                    .setValue(new ChatMessage(text, name, uid));

            Intent i = new Intent(getApplicationContext(), ChattingActivity.class);
            i.putExtra("otherUser", user1);
            startActivity(i);
        }
    }

    public String getThreadId() {

        String threadId = "thread1";
        String otherId = user1.getId();
        int result = myUid.compareTo(otherId);

        if (result < 0) {
            threadId = myUid + otherId;
        } else if (result > 0) {
            threadId = otherId + myUid;
        }
        Toast.makeText(getApplicationContext(), "result je  " + result + "   thread id " + threadId, Toast.LENGTH_LONG).show();

        return threadId;
    }
}