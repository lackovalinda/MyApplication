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
        btn_switch.setChecked(g.getSettings());

    }

}
