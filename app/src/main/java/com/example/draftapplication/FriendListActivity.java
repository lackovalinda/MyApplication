package com.example.draftapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class FriendListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ListView listOfUsers;
    private FirebaseUser user;
    private String myUid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.find_user_activity);
        FriendListActivity.this.setTitle("Friend list");

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Find the user");
        setSupportActionBar(myToolbar);
        listOfUsers = (ListView) findViewById(R.id.list_of_users);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        myUid = user.getUid();

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

    }

}
