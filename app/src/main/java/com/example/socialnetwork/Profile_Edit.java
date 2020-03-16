package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_Edit extends AppCompatActivity {
    private EditText userName, userMobile, userCountryName;
    private Button updateInfoBtn;
    private CircleImageView profileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference userUpdateInfoRef;
    private ProgressDialog profileProgressBar;
    private StorageReference userProfileImageRef;


    private Uri imageUri;
    StorageTask uploadTask;
    String currentUserId;
    final static int galleryPic = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile__edit);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        userUpdateInfoRef = FirebaseDatabase.getInstance().getReference().child("UserInfo").child(currentUserId);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("ProfileImages");
        //DatabaseReference checkImageRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(currentUserId);

        userName = findViewById(R.id.userProfileNameChangeId);
        userMobile = findViewById(R.id.userProfilePhoneNumberId);
        userCountryName = findViewById(R.id.userProfileCountryId);
        updateInfoBtn = findViewById(R.id.userProfileUpdateBtnId);
        profileImage = findViewById(R.id.userProfileImageChangeId);
        profileProgressBar = new ProgressDialog(this);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });


        userUpdateInfoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("imageUri")) {
                        String image = dataSnapshot.child("imageUri").getValue().toString();

                        Picasso.with(Profile_Edit.this)
                                .load(image)
                                .placeholder(R.drawable.profile)
                                .into(profileImage);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        updateInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();

            }


        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPic && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

            Picasso.with(Profile_Edit.this)
                    .load(imageUri)
                    .placeholder(R.drawable.profile)
                    .into(profileImage);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {


                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    userUpdateInfoRef.child("imageUri").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        //Toast.makeText(Profile_Edit.this, "", Toast.LENGTH_SHORT).show();

                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(Profile_Edit.this, "Error: " + message, Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(Profile_Edit.this, "Error: ", Toast.LENGTH_SHORT).show();

            }
        }
    }

    //Image Extension
    public String getImageExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

    }

    private void saveInfo() {
        String user_userName = userName.getText().toString().trim();
        String user_Mobile = userMobile.getText().toString().trim();
        String user_Country = userCountryName.getText().toString().trim();


        profileProgressBar.setTitle("Profile Update");
        profileProgressBar.setMessage("Information Updating");
        profileProgressBar.show();
        profileProgressBar.setCanceledOnTouchOutside(true);


        HashMap usermap = new HashMap();
        usermap.put("userName", user_userName);
        usermap.put("Mobile", user_Mobile);
        usermap.put("Country", user_Country);

        userUpdateInfoRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {

                if (task.isSuccessful()) {


                    Intent intent = new Intent(Profile_Edit.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    Toast.makeText(Profile_Edit.this, "Information update successfully", Toast.LENGTH_SHORT).show();
                    profileProgressBar.dismiss();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(Profile_Edit.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                    profileProgressBar.dismiss();
                }

            }
        });
    }


}


