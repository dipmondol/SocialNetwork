package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private Button loginBtn, signUpBtn;
    private EditText userLoginEmail, userLoginPass;
    FirebaseAuth loginAuth;
    ProgressDialog loginDialog;
    private TextView forgetPassRecoverTv;
    private DatabaseReference UserRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("UserInfo");


        userLoginEmail = findViewById(R.id.userLoginEmailId);
        userLoginPass = findViewById(R.id.userLoginPassId);
        loginBtn = findViewById(R.id.loginId);
        signUpBtn = findViewById(R.id.signupId);

        forgetPassRecoverTv = findViewById(R.id.forgottenPassId);
        loginDialog = new ProgressDialog(this);

        FirebaseUser currentUser = loginAuth.getCurrentUser();
        if (currentUser != null) {
            sendUserToMainActivity();
        }


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginUserEmail = userLoginEmail.getText().toString().trim();
                String loginUserPassword = userLoginPass.getText().toString().trim();

                if (loginUserEmail.isEmpty()) {
                    userLoginEmail.setError("Enter Your E-mail address");
                    userLoginEmail.requestFocus();
                    return;
                }
                if (loginUserPassword.isEmpty()) {
                    userLoginPass.setError("Enter your password");
                    userLoginPass.requestFocus();
                    return;
                }
                loginDialog.setTitle("Login Authentication");
                loginDialog.setMessage("Checking your Credential");
                loginDialog.show();
                loginDialog.setCanceledOnTouchOutside(true);


                loginAuth.signInWithEmailAndPassword(loginUserEmail, loginUserPassword).addOnCompleteListener(Login.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    Intent intent = new Intent(Login.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    String message = task.getException().getMessage();
                                    Toast.makeText(Login.this, "Authentication failed!" + message, Toast.LENGTH_SHORT).show();
                                    loginDialog.dismiss();
                                }
                            }
                        });


            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });

        //Recover password
        forgetPassRecoverTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            ShowRecoverPassDialog();
            
            }
        });


    }

    private void ShowRecoverPassDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        //Views to set the dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);


        builder.setView(linearLayout);


        //button recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               String email = emailEt.getText().toString().trim();
               beginRecovery(email);

            }
        });
        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();

    }

    private void beginRecovery(String email) {
        loginDialog.setMessage("Sending Email..");
        loginDialog.show();

        loginAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loginDialog.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(Login.this, "Email sent", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(Login.this, "Failed..", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void sendUserToMainActivity() {
        Intent Intent = new Intent(Login.this, MainActivity.class);
        Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(Intent);

    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(Login.this, Signup.class);
        startActivity(registerIntent);
        finish();
    }

    public void checkUserExistence() {
        final String currentUserId = loginAuth.getCurrentUser().getUid();
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChild(currentUserId)) {

                    Toast.makeText(Login.this, "Create account first", Toast.LENGTH_SHORT).show();
                    Intent Intent = new Intent(Login.this, Signup.class);
                    Intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(Intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
