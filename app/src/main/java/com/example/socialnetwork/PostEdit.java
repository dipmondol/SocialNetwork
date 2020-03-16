package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostEdit extends AppCompatActivity {

    private ImageView postEditImage;
    private EditText postEditText;
    private Button postBtn,deleteBtn;
    private String PostKey, currentUserId, databaseUserId, description, image;
    private DatabaseReference editPostRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_edit);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        PostKey = getIntent().getExtras().get("PostKey").toString();
        editPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        postEditImage = findViewById(R.id.postEditImageViewId);
        postEditText = findViewById(R.id.postTextId);
        postBtn = findViewById(R.id.postEditBtnId);
        deleteBtn = findViewById(R.id.postDeleteBtnId);

        deleteBtn.setVisibility(View.INVISIBLE);
        postBtn.setVisibility(View.INVISIBLE);

        editPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                description = dataSnapshot.child("description").getValue().toString();
                image = dataSnapshot.child("postImage").getValue().toString();
                databaseUserId = dataSnapshot.child("uid").getValue().toString();

                postEditText.setText(description);
                Picasso.with(PostEdit.this)
                        .load(image)
                        .into(postEditImage);
                if (currentUserId.equals(databaseUserId)) {

                    postBtn.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.VISIBLE);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
