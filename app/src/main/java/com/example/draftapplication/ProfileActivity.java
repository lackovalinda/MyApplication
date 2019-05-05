package com.example.draftapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ProfileActivity extends AppCompatActivity implements OnClickListener, TextToSpeech.OnInitListener, AdapterView.OnItemSelectedListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int TAKE_AVATAR_GALLERY_REQUEST = 1;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference ref;
    private ImageView image;
    private Bitmap bitmap;
    private Intent pickPhoto;
    private String uid;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private Boolean isPhotoSet;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        myToolbar.setTitle("My Profile");
        setSupportActionBar(myToolbar);
        myToolbar.setOnMenuItemClickListener(item -> {

            String id = item.getTitle().toString();

            switch (id) {
                case "Users":
                    startActivity(new Intent(getApplicationContext(), UserListActivity.class));
                    finish();
                    break;
                case "About":
                    startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                    finish();
                    break;
                case "Profile":
                    break;
                case "Settings":
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    finish();
                    break;
                case "Logout user":
                    mAuth.signOut();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
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
                    finish();
                    break;
                default:
                    Toast.makeText(getApplicationContext(), "default: " + item.getTitle(), Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
            });

        TextView email1 = (TextView) findViewById(R.id.profileEmail);
        TextView name1 = (TextView) findViewById(R.id.userName);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        image = (ImageView) findViewById(R.id.imageButton);
        isPhotoSet = false;
        ImageButton btn_send = (ImageButton) findViewById(R.id.sendMessage);
        ImageButton show_friends = (ImageButton) findViewById(R.id.friends);
        ImageButton info = (ImageButton) findViewById(R.id.about);

        info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                finish();
            }
        });

        if (user != null) {
            uid = user.getUid();
            email1.setText(user.getEmail());
            name1.setText(user.getDisplayName());

            ref = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            returnName();

            storage = FirebaseStorage.getInstance();
            storageRef = storage.getReference().child(uid);
            final long ONE_MEGABYTE = 1024 * 1024;

            if (!isPhotoSet) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Retrieving user data");
                progressDialog.show();

                storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
                    byte[] imageData = null;
                    Bitmap imageBitmap = null;
                    try
                    {
                        final int THUMBNAIL_SIZE = 64;
                        imageBitmap = BitmapFactory.decodeByteArray(bytes, 0 , bytes.length);
                        imageBitmap = Bitmap.createScaledBitmap(imageBitmap, 200, 200, false);
                    }
                    catch(Exception ex) {
                    }
                    image.setImageBitmap(imageBitmap);
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                    }
                });

                progressDialog.dismiss();
            }


            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String strAvatarPrompt = "Choose a picture to use as your avatar!";
                    //Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                    pickPhoto = new Intent();
                    pickPhoto.setType("image/*");
                    pickPhoto.setAction(Intent.ACTION_GET_CONTENT);
                    pickPhoto.addCategory(Intent.CATEGORY_OPENABLE);

                    startActivityForResult(Intent.createChooser(pickPhoto, strAvatarPrompt), TAKE_AVATAR_GALLERY_REQUEST);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.button_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult ( int requestCode, int resultCode, Intent data){

            switch (requestCode) {

                case TAKE_AVATAR_GALLERY_REQUEST:
                    if (resultCode == RESULT_OK)
                        try {
                            // We need to recyle unused bitmaps
                            if (bitmap != null) {
                                bitmap.recycle();
                            }
                            InputStream stream = getContentResolver().openInputStream(data.getData());
                            bitmap = BitmapFactory.decodeStream(stream);
                            uid = mAuth.getUid();
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference storageRef = storage.getReference().child(uid);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            Bitmap convertedImage = getResizedBitmap(bitmap, 350);
                            convertedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            final byte[] array = baos.toByteArray();

                            UploadTask uploadTask = storageRef.putBytes(array);

                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception exception) {
                                    Toast.makeText(getApplicationContext(), "picture was not uploaded", Toast.LENGTH_SHORT).show();
                                }
                            });
                            uploadTask.addOnSuccessListener(taskSnapshot -> {
                                ref.child("image").setValue("set");
                                Toast.makeText(getApplicationContext(), "picture was uploaded", Toast.LENGTH_SHORT).show();
                            });

                            stream.close();
                            image.setImageBitmap(convertedImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    break;

                default:
                    super.onActivityResult(requestCode, resultCode, data);

            }
        }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public void returnName() {
        final String[] name = {new String()};
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                               @Override
                                               public void onDataChange(DataSnapshot dataSnapshot) {
                                                   int i = 0;

                                                   for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                                                       if (userSnapShot.getKey().equals("username")) {
                                                           name[0] = userSnapShot.getValue().toString();
                                                           setName(name[0]);
                                                       }
                                                       if (userSnapShot.getKey().equals("image")) {
                                                           if (userSnapShot.getValue().equals("set")){
                                                               isPhotoSet = true;
                                                           }
                                                       }
                                                   }
                                               }

                                               @Override
                                               public void onCancelled(DatabaseError databaseError) {
                                               }
                                           }
        );
    }

    public void setName(String name) {

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates);

    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onInit(int status) {
    }
}