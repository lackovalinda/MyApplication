package com.example.draftapplication;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("Settings");
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
                        //startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        break;
                    case "Logout user":
                        mAuth.signOut();
                        //googleApiClient.maybeSignOut();
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

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Switch btn_switch = (Switch) findViewById(R.id.switch1);
        Globals g = (Globals)getApplication();
        btn_switch.setChecked(g.getData());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.button_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
