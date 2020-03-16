package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.socialnetwork.Model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mtoolBar;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageView postCreate;
    private FirebaseAuth mAuth;
    private DatabaseReference UserRef, postRef, editPostRef;

    private CircleImageView navProfileImage;
    private TextView navUserName, navUserEmail;

    String PostKey, currentUserId, databaseUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("UserInfo");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        editPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child("uid");


        //set home toolbar with title
        mtoolBar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolBar);
        getSupportActionBar().setTitle("Home");

        //Drawer open & close
        drawerLayout = findViewById(R.id.drawerLayoutId);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,
                drawerLayout, R.string.drawer_open,
                R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = findViewById(R.id.navViewId);
        postList = findViewById(R.id.usersPostListId);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        //Set navigation drawer Header
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = navView.findViewById(R.id.userProfileImageId);
        navUserName = navView.findViewById(R.id.userNameId);
        navUserEmail = navView.findViewById(R.id.userEmailId);
        postCreate = findViewById(R.id.postCreateBtnId);


        postCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, post_create.class);
                startActivity(intent);
            }
        });

        DisplayAllUsersPost();


        if (navUserEmail == null) {
            navUserEmail.setText("android@gmail.com");

        }
        if (navUserName == null) {
            navUserName.setText("UserName");
        } else {
            UserRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("imageUri")) {
                            String image = dataSnapshot.child("imageUri").getValue().toString();
                            Picasso.with(MainActivity.this)
                                    .load(image)
                                    .placeholder(R.drawable.profile)
                                    .into(navProfileImage);
                        }
                        if (dataSnapshot.hasChild("userName")) {
                            String userName = dataSnapshot.child("userName").getValue().toString();
                            navUserName.setText(userName);

                        }
                        if (dataSnapshot.hasChild("email")) {
                            String email = dataSnapshot.child("email").getValue().toString();
                            navUserEmail.setText(email);

                        } else {
                            Toast.makeText(MainActivity.this, "Profile name do not exists....", Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void DisplayAllUsersPost() {

        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (Posts.class, R.layout.all_post_layout,
                                PostsViewHolder.class, postRef) {
                    @Override
                    protected void populateViewHolder(final PostsViewHolder postsViewHolder, Posts posts, int i) {


                        final String PostKey = getRef(i).getKey();
                        postsViewHolder.setUserName(posts.getUserName());
                        postsViewHolder.setCurrentTime(posts.getCurrentTime());
                        postsViewHolder.setCurrentDate(posts.getCurrentDate());
                        postsViewHolder.setDescription(posts.getDescription());
                        postsViewHolder.setImageUri(getApplicationContext(), posts.getImageUri());
                        postsViewHolder.setpostImage(getApplicationContext(), posts.getpostImage());


                        postsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                editPostRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        currentUserId = mAuth.getCurrentUser().getUid();
                                        FirebaseDatabase.getInstance().getReference("UserInfo")
                                                .child(currentUserId);

                                            if (dataSnapshot.hasChild("uid")) {
                                                databaseUserId = dataSnapshot.child("uid").getValue().toString();
                                                if (currentUserId.equals(databaseUserId)) {
                                                    Intent intent = new Intent(MainActivity.this, PostEdit.class);
                                                    intent.putExtra("PostKey", PostKey);
                                                    startActivity(intent);


                                                }
                                                else {
                                                    Intent intent = new Intent(MainActivity.this, PostShowInAnotherActivity.class);

                                                    startActivity(intent);
                                                }

                                            }
                                        }




                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });


                    }
                };

        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setUserName(String userName) {
            TextView postUserName = mView.findViewById(R.id.postUserNameId);
            postUserName.setText(userName);

        }

        public void setImageUri(Context context, String imageUri) {

            CircleImageView image = mView.findViewById(R.id.postProfileImageId);
            Picasso.with(context)
                    .load(imageUri)
                    .placeholder(R.drawable.profile)
                    .into(image);


        }

        public void setCurrentTime(String currentTime) {
            TextView time = mView.findViewById(R.id.postTimeId);
            time.setText(currentTime);
        }

        public void setCurrentDate(String currentDate) {
            TextView date = mView.findViewById(R.id.postDateId);
            date.setText("" + currentDate);
        }

        public void setDescription(String description) {
            TextView postDescription = mView.findViewById(R.id.postDescriptionId);
            postDescription.setText(description);
        }

        public void setpostImage(Context context, String postImage) {
            ImageView usersPostImage = mView.findViewById(R.id.postImageId);
            Picasso.with(context)
                    .load(postImage)
                    .into(usersPostImage);

        }
    }

    @Override
    protected void onStart() {

        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            sendUserToLoginActivity();
        } else {

        }
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_PostId:
                Intent intent = new Intent(MainActivity.this, post_create.class);
                startActivity(intent);
                break;

            case R.id.nav_ProfileId:
                startActivity(new Intent(MainActivity.this, Profile_Edit.class));
                break;

            case R.id.nav_homeId:
                Toast.makeText(this, "Home Clicked", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_friendsId:
                Toast.makeText(this, "Friends Clicked", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_FindFriendsId:
                Toast.makeText(this, "Find Friends Clicked", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_MessagesId:
                Toast.makeText(this, "Messages Clicked", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_SettingsId:
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_LONG).show();
                break;

            case R.id.nav_LogOutId:
                mAuth.signOut();
                startActivity(new Intent(MainActivity.this, Login.class));
                break;

        }

    }
}
