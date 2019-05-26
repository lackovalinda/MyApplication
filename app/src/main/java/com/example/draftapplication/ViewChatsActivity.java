package com.example.draftapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class ViewChatsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener,  AdapterView.OnItemSelectedListener, Serializable {

    private FirebaseAuth auth;
    private ListView listOfMessages;
    private ChatMessage messageThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_user_activity);

        auth = FirebaseAuth.getInstance();
        ImageButton btn_findChat = (ImageButton) findViewById(R.id.find);
        listOfMessages = (ListView) findViewById(R.id.list_of_users);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Chats");
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
                        break;
                    case "Delete user":
                        break;
                    default:
                        Toast.makeText(getApplicationContext(), "default: " + item.getTitle(), Toast.LENGTH_LONG).show();
                        break;
                }
                return true;
            }
        });

        btn_findChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findChats();
            }
        });

        listOfMessages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                messageThread = (ChatMessage) listOfMessages.getItemAtPosition(position);

            }
        });


    }

    private void displayChatMessages(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Query query;
        if (messageThread != null) {
            query = FirebaseDatabase.getInstance().getReference("Messages/threads");
        }
        else{
            query = FirebaseDatabase.getInstance().getReference("Messages/threads");
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

                //        MessageViewHolder holder = new MessageViewHolder();
                LayoutInflater messageInflater = (LayoutInflater) getApplicationContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

                /*
                protected void populateViewHolder(ChatMessageViewHolder chatMessageViewHolder, ChatModel m, int i)
                {
                    if (model.getMessageUserID() != user.getUid()) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.gravity = Gravity.LEFT;
                        v.setLayoutParams(params);
                    }
                }
                */
            }
        };
        assert listOfMessages != null;
        adapter.startListening();
        listOfMessages.setAdapter(adapter);
    }

    @Override
    public void onInit(int status) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void findChats(){
        String myId  = auth.getCurrentUser().getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages").child("threads");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot d: dataSnapshot.getChildren()){
                    if (d.getKey().contains(myId)){
                        Toast.makeText(getApplicationContext(), d.getKey().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
}
