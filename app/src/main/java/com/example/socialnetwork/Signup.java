package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialnetwork.Model.UserInformation;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class Signup extends AppCompatActivity {
    private EditText fullName, userName, email, dateOfBirth, password, confirmPass;
    private RadioButton male, female;
    private TextView backToLogin;
    private CircleImageView profileImageV;
    String gender = "";
    private ProgressDialog loadingBar;
    private Button registerBtn;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private String currentUserId, randomName, saveCurrentDate, saveCurrentTime;

    FirebaseAuth userAuth;
    StorageTask uploadTask;


    private Uri imageUri;

    private static final int IMAGE_REQUEST = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        userAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("UserInfo");

        storageReference = FirebaseStorage.getInstance().getReference().child("ProfileImages");


        fullName = findViewById(R.id.fullName_Id);
        userName = findViewById(R.id.userName_Id);
        email = findViewById(R.id.email_Id);
        dateOfBirth = findViewById(R.id.dobIb);
        password = findViewById(R.id.password_Id);
        confirmPass = findViewById(R.id.confirmpass_Id);
        registerBtn = findViewById(R.id.registerId);
        male = findViewById(R.id.maleId);
        female = findViewById(R.id.femaleId);
        backToLogin = findViewById(R.id.backtoLoginId);
        profileImageV = findViewById(R.id.userProfileImageId);


        loadingBar = new ProgressDialog(this);


        profileImageV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST);
            }
        });

        dateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();

            }
        });


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (uploadTask != null && uploadTask.isInProgress()) {
                    Toast.makeText(Signup.this, "Image Uploading", Toast.LENGTH_SHORT).show();
                } else {

                    saveImage();

                }


            }


        });


    }


    private void pickDate() {

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                String date = day + "-" + (month + 1) + "-" + year;
                dateOfBirth.setText(date);
            }
        };
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener, year, month, day);
        datePickerDialog.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {

            imageUri = data.getData();
            Picasso.with(this)
                    .load(imageUri)
                    .into(profileImageV);

        }

    }
   /* @Override
    protected void onStart() {
        super.onStart();

        currentUser = userAuth.getCurrentUser();
        if(currentUser!=null){

        }
    }

    private void sendUserToMainActivity() {
        Intent Intent = new Intent(Signup.this, Login.class);
        Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Intent);

    }*/

    //Image Extension
    public String getImageExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

    }

    private void saveImage() {

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm:ss a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        final String user_fullname = fullName.getText().toString().trim();
        final String user_username = userName.getText().toString().trim();
        final String user_email = email.getText().toString().trim();
        final String user_dateOfBirth = dateOfBirth.getText().toString().trim();
        final String user_password = password.getText().toString().trim();
        final String user_confirmPassword = confirmPass.getText().toString().trim();


        if (male.isChecked()) {
            gender = "Male";
        }
        if (female.isChecked()) {
            gender = "Female";
        }

        if (user_fullname.isEmpty()) {
            fullName.setError("Enter your Full Name");
            fullName.requestFocus();
            return;
        }
        if (user_username.isEmpty()) {
            userName.setError("Enter your User Name");
            userName.requestFocus();
            return;
        }
        if (user_email.isEmpty()) {
            email.setError("Enter an E-mail address");
            email.requestFocus();
            return;
        }
        if (user_dateOfBirth.isEmpty()) {
            dateOfBirth.setError("Enter your Date of Birth");
            dateOfBirth.requestFocus();
            return;


        }
        if (!Patterns.EMAIL_ADDRESS.matcher(user_email).matches()) {
            email.setError("Enter an valid E-mail");
            email.requestFocus();
            return;

        }
        if (user_password.isEmpty()) {
            password.setError("Enter a password");
            password.requestFocus();
            return;

        }
        if (user_password.length() < 5) {
            password.setError("Minimum length of password is 5");
            password.requestFocus();
            return;
        }
        if (!user_password.equals(user_confirmPassword)) {
            confirmPass.setError("Confirm Your password");
            confirmPass.requestFocus();
            return;
        }

        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Account creating");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(false);

        randomName = saveCurrentDate + saveCurrentTime;
        StorageReference ref = storageReference.child(imageUri.getLastPathSegment() +randomName+ "." + getImageExtension(imageUri));

        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(Signup.this, "Image successfully stored", Toast.LENGTH_SHORT).show();

                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful()) ;
                        final Uri downloadUrl = urlTask.getResult();

                        userAuth.createUserWithEmailAndPassword(user_email, user_password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                            currentUserId = userAuth.getCurrentUser().getUid();
                                            databaseReference = FirebaseDatabase.getInstance().getReference("UserInfo");


                                            UserInformation userInformation = new UserInformation(user_fullname, user_username, user_email,
                                                    user_dateOfBirth, gender, user_password, user_confirmPassword, downloadUrl.toString());

                                            currentUserId = userAuth.getCurrentUser().getUid();
                                            FirebaseDatabase.getInstance().getReference("UserInfo")
                                                    .child(currentUserId)
                                                    .setValue(userInformation);


                                            Toast.makeText(Signup.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Signup.this, Login.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            loadingBar.dismiss();

                                        } else {
                                            String message = task.getException().getMessage();
                                            Toast.makeText(Signup.this, "Error occured" + message, Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }


                                    }
                                });


                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Image is not stored successfully ", Toast.LENGTH_SHORT);

            }
        });

               /* userAuth.createUserWithEmailAndPassword(user_email, user_password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    currentUserId = userAuth.getCurrentUser().getUid();
                                    databaseReference = FirebaseDatabase.getInstance().getReference("UserInfo");


                                    UserInformation userInformation = new UserInformation(user_firstname, user_lastname, user_email,
                                            user_dateOfBirth, gender, user_password, user_confirmPassword);


                                    FirebaseDatabase.getInstance().getReference("UserInfo")
                                            .child(currentUserId)
                                            .setValue(userInformation);


                                    Toast.makeText(Signup.this, "Registration successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(Signup.this, Login.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    loadingBar.dismiss();

                                } else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(Signup.this, "Error occured" + message, Toast.LENGTH_SHORT).show();
                                    loadingBar.dismiss();
                                }

                            }
                        });

            }
        });
*/
    }

}



