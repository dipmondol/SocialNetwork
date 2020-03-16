package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class post_create extends AppCompatActivity {
    private ImageView backBtn;
    private ImageView postImage;
    private EditText postCreate;
    private Button postBtn;
    private ProgressDialog postProgressDialog;


    private Uri imageUri;
    private String Description;
    private String saveCurrentDate, saveCurrentTime, postRandomName, currentUserId;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, postRef;
    private StorageReference postImageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("UserInfo");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        postImageReference = FirebaseStorage.getInstance().getReference();


        backBtn = findViewById(R.id.postBackBtnId);

        postCreate = findViewById(R.id.postEditTextId);
        postImage = findViewById(R.id.postImageViewId);
        postBtn = findViewById(R.id.postBtnId);

        postProgressDialog = new ProgressDialog(this);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(post_create.this, MainActivity.class));
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 1);

            }
        });
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                ValidatePostInfo();


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();

            Picasso.with(post_create.this)
                    .load(imageUri)
                    .into(postImage);
        }


    }

    private void ValidatePostInfo() {
        Description = postCreate.getText().toString();
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        } else {
            postProgressDialog.setTitle("Post Create");
            postProgressDialog.setMessage("Post Creating, wait until it successfully created");
            postProgressDialog.show();
            postProgressDialog.setCanceledOnTouchOutside(true);

            StoringImageToStorage();
        }
    }

    private void StoringImageToStorage() {


        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = postImageReference.child("Post Images").child(imageUri.getLastPathSegment() +
                postRandomName + ".jpg");

        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {

                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                    HashMap postMap = new HashMap();
                    postMap.put("postImage",result );

                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            postRef.child(currentUserId+postRandomName).child("postImage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {


                                }
                            });


                        }
                    });
                    SavingPostInfoToDatabase();

                    startActivity(new Intent(post_create.this, MainActivity.class));
                    Toast.makeText(post_create.this, "Post Successfully Created", Toast.LENGTH_SHORT).show();
                    postProgressDialog.dismiss();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(post_create.this, "Error occured" + message, Toast.LENGTH_SHORT).show();
                    postProgressDialog.dismiss();
                }

            }
        });


    }

    private void SavingPostInfoToDatabase() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String postUserName = dataSnapshot.child("userName").getValue().toString();
                    String profileImage = dataSnapshot.child("imageUri").getValue().toString();




                    HashMap postMap = new HashMap();
                    postMap.put("uid", currentUserId);
                    postMap.put("currentDate", saveCurrentDate);
                    postMap.put("currentTime", saveCurrentTime);
                    postMap.put("userName",postUserName);
                    postMap.put("description", Description);
                    postMap.put("imageUri",profileImage);




                    postRef.child(currentUserId + postRandomName).updateChildren(postMap).addOnCompleteListener(
                            new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(post_create.this, MainActivity.class));
                                        Toast.makeText(post_create.this, "Done", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(post_create.this, "Error occured", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            }
                    );
                }

                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
