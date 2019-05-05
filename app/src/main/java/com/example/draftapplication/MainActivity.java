package com.example.draftapplication;

import android.content.Intent;
/*
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
*/
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView text;
    private FirebaseAuth mAuth;
    private FirebaseUser user = null;
    private EditText email,password, nickname;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);
        MainActivity.this.setTitle("Register");

        text = (TextView) findViewById(R.id.text);
        Button registerButton = (Button) findViewById(R.id.register);
        Button loginButton = (Button) findViewById(R.id.login);
        email = (EditText) findViewById(R.id.profileEmail);
        password = (EditText) findViewById(R.id.password);
        nickname = (EditText) findViewById(R.id.nickname);
        registerButton.setEnabled(true);
        loginButton.setEnabled(true);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth = FirebaseAuth.getInstance();
                final String em = email.getText().toString();
                String pass = password.getText().toString();

                if (TextUtils.isEmpty(em)) {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(getApplicationContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show();
                }
                if (pass.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
                mAuth.createUserWithEmailAndPassword(em, pass)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                final String uid = user.getUid().trim();
                                final String username = nickname.getText().toString().trim();
                                String name[] = new String[1];

                                reference = FirebaseDatabase.getInstance().getReference("Users").child(uid);

                                HashMap<String, String> hashMap = new HashMap<>();
                                hashMap.put("id", uid);
                                hashMap.put("username", username);
                                hashMap.put("email", em);
                                hashMap.put("image", "not set");


                                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                                            if (userSnapShot.getKey().equals("username")) {
                                                name[0] = userSnapShot.getValue().toString();
                                                setName(name[0]);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "E-mail or password is wrong", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                        });
            }
        });


        loginButton.setOnClickListener(v -> {
            if (user == null) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Welcome " + mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                finish();
            }
        }
        );
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        text = findViewById(R.id.text);

        int SIGN_IN_REQUEST_CODE = 9001;
        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                finish();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();
                // Close the app
                finish();
            }
        }
    }

    public void setName(String name){

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).updateProfile(profileUpdates);

    }

}


