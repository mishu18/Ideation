package com.example.android.ideation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.android.ideation.Profile.ProfileActivity;
import com.example.android.ideation.Proposals.ProposalListActivity;
import com.example.android.ideation.activities.ChatRoomsActivity;
import com.example.android.ideation.activities.CreateImagePostActivity;
import com.example.android.ideation.activities.CreateNoImagePostActivity;
import com.example.android.ideation.activities.PostDetailActivity;
import com.example.android.ideation.activities.SearchActivity;
import com.example.android.ideation.adapters.PostsAdapter;
import com.example.android.ideation.bookCity.BookCityMain;
import com.example.android.ideation.listeners.OnPostClickListener;
import com.example.android.ideation.listeners.OnPostUserImageClickListener;
import com.example.android.ideation.loginsignup.loginActivity;
import com.example.android.ideation.model.Notification;
import com.example.android.ideation.model.PostModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.text.WordUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements OnPostClickListener, OnPostUserImageClickListener {

    private static final String TAG = "MainActivity";
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    CircleImageView drawerProfileImagView;
    NavigationView navigationView;
    TextView userCommunityTxtView, showAllPostsBtn, showMyCommunityPostsBtn, toolbarExhangeTxt, toolbarLearningTxt, emptyMsgCP, emptyMsgUI;
    CircleImageView profileImagePost;
    TextView drawerUserName, mainWelcomeMsg;
    ImageView notifIndicator;


    RecyclerView recyclerView;
    List<PostModel> mPosts, mTempPosts, mAllPosts;
    List<Notification> notifications;

    PostsAdapter mAdapter;
    LinearLayout goToProfile, logout, goToMyPosts, goToNotif,goToSettings,goToBookCity,goToMessages;


    LinearLayout emptyMsgLayout, createPostBtn, uploadImgBtn;

    DatabaseReference postDataRef, userInfoRef, notifDataRef;

    AVLoadingIndicatorView aviLoadingView;

    CardView mainHeader;
    CardView postSwitchBtn;
    int totalPostsLoaded = 0;

    ActionBarDrawerToggle toggle;
    boolean mToolBarNavigationListenerIsRegistered = false;
    boolean ifInPublicMode = true;
    boolean ifShowingMyPosts = false;
    boolean isViewingBook = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidThreeTen.init(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    private void initUI() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        showAllPostsBtn = findViewById(R.id.main_all_post_btn);
        showMyCommunityPostsBtn = findViewById(R.id.main_community_post_btn);
        emptyMsgLayout = findViewById(R.id.empty_msg);
        notifIndicator = findViewById(R.id.notification_indicator);
        createPostBtn = findViewById(R.id.main_create_post_btn);
        uploadImgBtn = findViewById(R.id.main_upload_image_btn);
        aviLoadingView = findViewById(R.id.avi);
        goToProfile = findViewById(R.id.drawer_porfile);
        goToMyPosts = findViewById(R.id.drawer_myPosts);
        goToNotif = findViewById(R.id.drawer_notif);
        goToSettings = findViewById(R.id.drawer_settings);
        goToBookCity = findViewById(R.id.drawer_bookcity);
        goToMessages = findViewById(R.id.drawer_messages);
        logout = findViewById(R.id.drawer_logout);
        mainHeader = findViewById(R.id.main_header);
        postSwitchBtn = findViewById(R.id.postSelectorLayout);
        drawerUserName = findViewById(R.id.drawer_user_name);
        mainWelcomeMsg = findViewById(R.id.main_welcome_txtView);
        profileImagePost = findViewById(R.id.main_user_img_imgView);
        drawerProfileImagView = navigationView.findViewById(R.id.drawer_profile_img);
        userCommunityTxtView = navigationView.findViewById(R.id.drawer_user_community);
        toolbarExhangeTxt = findViewById(R.id.exchange_txt);
        toolbarLearningTxt = findViewById(R.id.learning_txt);
        emptyMsgCP = findViewById(R.id.emptyMsgCreatePost);
        emptyMsgUI = findViewById(R.id.emptyMsgUploadImg);
        loadLoggedInUserDetails();

        emptyMsgCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, CreateNoImagePostActivity.class).putExtra("actionType", "newPost"));
            }
        });

        emptyMsgUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, CreateImagePostActivity.class).putExtra("actionType", "newPost"));
            }
        });

        goToMyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMyPosts(true);
            }
        });

        goToBookCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, BookCityMain.class));
            }
        });

        goToMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, ChatRoomsActivity.class));
            }
        });

        goToProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(com.example.android.ideation.MainActivity.this, ProfileActivity.class);
                i.putExtra("uid", com.example.android.ideation.Constants.getConstantUid());
                startActivity(i);
            }
        });



        goToSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, com.example.android.ideation.Settings.class));
            }
        });

        goToNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(com.example.android.ideation.MainActivity.this, ProposalListActivity.class);
                i.putExtra("mode","Notifications");
                i.putExtra("id", com.example.android.ideation.Constants.getConstantUid());
                startActivity(i);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                SharedPreferences prefs = getSharedPreferences("loginDetails", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("email", "none");
                editor.putString("password", "none");
                com.example.android.ideation.Constants.uCommunity = null;
                com.example.android.ideation.Constants.uName = "N/A";
                editor.apply();
                Intent i = new Intent(com.example.android.ideation.MainActivity.this, loginActivity.class);
                startActivity(i);
                finish();
            }
        });

        createPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, CreateNoImagePostActivity.class).putExtra("actionType", "newPost"));
            }
        });

        uploadImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(com.example.android.ideation.MainActivity.this, CreateImagePostActivity.class).putExtra("actionType", "newPost"));
            }
        });

        showAllPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ifInPublicMode = true;
                showAllPostsBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.postSelectorBtnColor));
                showAllPostsBtn.setTextColor(Color.WHITE);
                showMyCommunityPostsBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.btnUnselectedColor));
                showMyCommunityPostsBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGray));
                if (ifShowingMyPosts) {
                    mPosts.clear();
                    mPosts.addAll(mTempPosts);
                    prepareRecyclerView(true, true);
                } else {
                    mPosts.clear();
                    mPosts.addAll(mTempPosts);
                    prepareRecyclerView(true, false);
                }
            }
        });

        showMyCommunityPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ifInPublicMode = false;
                showMyCommunityPostsBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.postSelectorBtnColor));
                showMyCommunityPostsBtn.setTextColor(Color.WHITE);
                showAllPostsBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.btnUnselectedColor));
                showAllPostsBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.darkGray));
                if (!ifShowingMyPosts) {
                    prepareRecyclerView(false, false);
                } else {
                    prepareRecyclerView(false, true);
                }
            }
        });


        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        initRecyclerView();
        subscribeToPosts();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.posts_recylerview);
        mPosts = new ArrayList<>();
        mTempPosts = new ArrayList<>();
        mAllPosts = new ArrayList<>();
        mAdapter = new PostsAdapter(mPosts, getApplicationContext(), this, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    private void enableViews(boolean enable) {
        if (enable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toggle.setDrawerIndicatorEnabled(false);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            if (!mToolBarNavigationListenerIsRegistered) {
                toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleMyPosts(false);
                    }
                });
                mToolBarNavigationListenerIsRegistered = true;
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            toggle.setDrawerIndicatorEnabled(true);
            toggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }
    }


    private void loadLoggedInUserDetails() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages/" + com.example.android.ideation.Constants.getConstantUid());
        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(getApplicationContext())
                        .load(imageURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.main_user_profile_avatar)
                        .into(drawerProfileImagView);
                Glide.with(getApplicationContext())
                        .load(imageURL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.main_user_profile_avatar)
                        .into(profileImagePost);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });

        if (com.example.android.ideation.Constants.getuName() == null || com.example.android.ideation.Constants.getuName().equalsIgnoreCase("N/A")) {
            drawerUserName.setText("Welcome!");
            mainWelcomeMsg.setText("Welcome!");
        } else {
            drawerUserName.setText(WordUtils.capitalize(com.example.android.ideation.Constants.getuName()));
            mainWelcomeMsg.setText(WordUtils.capitalize("Welcome, " + com.example.android.ideation.Constants.getuName() + "!"));
        }

        if (com.example.android.ideation.Constants.uCommunity == null) {
            userCommunityTxtView.setVisibility(View.GONE);
        } else {
            userCommunityTxtView.setText(com.example.android.ideation.Constants.uCommunity);
        }
        updateDrawerProfileImageBorder(com.example.android.ideation.Constants.uCommunity);
    }

    private void loadPostsUserImages() {
        if (!mPosts.isEmpty()) {
            for (int i = 0; i < mPosts.size(); i++) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profileImages/" + mPosts.get(i).getUser_id());
                final int finalI = i;
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        String imageURL = uri.toString();
                        mPosts.get(finalI).setPost_user_posted_image(imageURL);
                        mAdapter.notifyImageLoaded(finalI);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                });
            }
        }
    }

    private void fetchPostUserInfo(final PostModel post, final long totalPosts) {
        userInfoRef = FirebaseDatabase.getInstance().getReference("User_Information");
        userInfoRef.child(post.getUser_id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("name")) {
                        post.setPost_user_posted_name(dataSnapshot.child("name").getValue().toString());
                    } else {
                        post.setPost_user_posted_name("(No Name)");
                    }
                    mPosts.add(post);
                    if (mPosts.size() == totalPosts) {
                        prepareRecyclerView(true, false);
                        loadPostsUserImages();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                prepareRecyclerView(true, false);
                Toast.makeText(getApplicationContext(), "Error While Loading Posts", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void toggleMyPosts(boolean show) {
        if (show) {
            toolbarExhangeTxt.setText("My Posts");
            toolbarLearningTxt.setVisibility(View.GONE);
            ifShowingMyPosts = true;
            if (ifInPublicMode) {
                mAllPosts.addAll(mPosts);
            } else {
                mAllPosts.addAll(mTempPosts);
            }
            mainHeader.setVisibility(View.GONE);
            enableViews(true);
            prepareRecyclerView(true, true);
        } else {
            mPosts.clear();
            mPosts.addAll(mAllPosts);
            mPosts.remove(0);
            mAllPosts.clear();
            mTempPosts.clear();
            toolbarExhangeTxt.setText("EXCHANGE");
            toolbarLearningTxt.setVisibility(View.VISIBLE);
            ifShowingMyPosts = false;
            mainHeader.setVisibility(View.VISIBLE);
            enableViews(false);
            prepareRecyclerView(true, false);
        }
    }

    @Override
    public void onBackPressed() {
        if (ifShowingMyPosts) {
            toggleMyPosts(false);
        } else {
            super.onBackPressed();
        }
    }

    private boolean checkIfPostCommunityEqualsMyCommunity(PostModel post) {
        for (String st : post.getTagged_communities()) {
            if (st.equalsIgnoreCase(com.example.android.ideation.Constants.uCommunity)) {
                return true;
            }
        }
        return false;
    }

    private void changeDisplayedItems(){
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter.notifyDataSetChanged();
    }

    private void prepareRecyclerView(boolean showOnlyPublicPosts, boolean showOnlyUserPosts) {
        hideProgressBar();
        if (!showOnlyUserPosts) {
            if (showOnlyPublicPosts) {
                if (mPosts.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyMsgLayout.setVisibility(View.VISIBLE);
                    postSwitchBtn.setVisibility(View.VISIBLE);
                } else {
                    PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null);
                    mPosts.add(0, post);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyMsgLayout.setVisibility(View.GONE);
                    postSwitchBtn.setVisibility(View.VISIBLE);
                    changeDisplayedItems();
                }
                mTempPosts.clear();
            } else {
                if (!mPosts.isEmpty()) {
                    showProgressBar();
                    mTempPosts.addAll(mPosts);
                    mTempPosts.remove(0);
                    mPosts.clear();
                    for (PostModel post : mTempPosts) {
                        if (!post.getPost_type().equalsIgnoreCase("NoMorePost")) {
                            if (checkIfPostCommunityEqualsMyCommunity(post)) {
                                mPosts.add(post);
                            }
                        }
                    }
                    if (mPosts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMsgLayout.setVisibility(View.VISIBLE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                    } else {
                        PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null);
                        mPosts.add(0,post);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMsgLayout.setVisibility(View.GONE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                        changeDisplayedItems();
                    }
                    hideProgressBar();
                }
            }
        } else {
            if (showOnlyPublicPosts) {
                if (!mAllPosts.isEmpty()) {
                    mTempPosts.clear();
                    mTempPosts.addAll(mAllPosts);
                    mTempPosts.remove(0);
                    mPosts.clear();
                    for (PostModel post : mTempPosts) {
                        if (!post.getPost_type().equalsIgnoreCase("NoMorePost")) {
                            if (post.getUser_id().equalsIgnoreCase(com.example.android.ideation.Constants.getConstantUid())) {
                                mPosts.add(post);
                            }
                        }
                    }
                    if (mPosts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMsgLayout.setVisibility(View.VISIBLE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                    } else {
                        PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null);
                        mPosts.add(0,post);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMsgLayout.setVisibility(View.GONE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                        changeDisplayedItems();
                    }
                    mTempPosts.clear();
                }
            } else {
                if (!mAllPosts.isEmpty()) {
                    showProgressBar();
                    mTempPosts.addAll(mAllPosts);
                    mTempPosts.remove(0);
                    mPosts.clear();
                    for (PostModel post : mTempPosts) {
                        if (!post.getPost_type().equalsIgnoreCase("NoMorePost")) {
                            if (checkIfPostCommunityEqualsMyCommunity(post) && post.getUser_id().equalsIgnoreCase(com.example.android.ideation.Constants.getConstantUid())) {
                                mPosts.add(post);
                            }
                        }
                    }
                    if (mPosts.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyMsgLayout.setVisibility(View.VISIBLE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                    } else {
                        PostModel post = new PostModel(null, null, null, null, "NoMorePost", null, null, null, null, null, null, null);
                        mPosts.add(0,post);
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyMsgLayout.setVisibility(View.GONE);
                        postSwitchBtn.setVisibility(View.VISIBLE);
                        changeDisplayedItems();
                    }
                    mTempPosts.clear();
                    hideProgressBar();
                }
            }
        }
    }


    private void showProgressBar() {
        recyclerView.setVisibility(View.GONE);
        emptyMsgLayout.setVisibility(View.GONE);
        aviLoadingView.show();
    }

    private void hideProgressBar() {
        aviLoadingView.hide();
    }

    private void subscribeToPosts() {
        mPosts.clear();
        mAllPosts.clear();
        mTempPosts.clear();
        totalPostsLoaded = 0;
        showProgressBar();
        postDataRef = FirebaseDatabase.getInstance().getReference("Posts_Table");
        postDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {
                        long totalPosts = dataSnapshot.getChildrenCount();
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            PostModel post = dsp.getValue(PostModel.class);
                            post.setPost_id(dsp.getKey());
                            fetchPostUserInfo(post, totalPosts);
                        }
                    } else {
                        prepareRecyclerView(true, false);
                    }
                } else {
                    prepareRecyclerView(true, false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error While Loading Posts", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void subscribeToNotifications(){
        if (notifications == null) {
            notifications = new ArrayList<>();
        }else{
            notifications.clear();
        }
        notifDataRef = FirebaseDatabase.getInstance().getReference("Notification_Table/Proposal/"+ com.example.android.ideation.Constants.getConstantUid());
        notifDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChildren()) {
                        boolean flag = true;
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            Notification notif = dsp.getValue(Notification.class);
                            Log.i("NotificationTest",notif.toString());
                            try {
                                if (notif.getRead_at().equals("")) {
                                    notifIndicator.setVisibility(View.VISIBLE);
                                    flag = false;
                                    break;
                                }else{
                                }
                            }catch (Exception e){

                            }
                        }
                        if (flag){
                            notifIndicator.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    private void updateDrawerProfileImageBorder(String community) {
        if (community != null) {
            if (community.equalsIgnoreCase("Architecture")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_arch));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_arch));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_arch), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Biosciences")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_bioSci));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_bioSci));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_bioSci), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Chemical Engineering")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_chemEng));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_chemEng));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_chemEng), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Chemistry")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_chem));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_chem));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_chem), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Civil Engineering")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_civil));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_civil));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_civil), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Computer Science")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_cs));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_cs));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_cs), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Department of Biotechnology")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_dobt));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_dobt));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_dobt), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Development Studies")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_devStd));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_devStd));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_devStd), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Earth Sciences")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_es));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_es));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_es), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Economics")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_economics));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_economics));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_economics), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Electrical and Computer Engineering")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_ece));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_ece));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_ece), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Environmental Sciences")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_env_s));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_env_s));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_env_s), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Health Informatics")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_health));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_health));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_health), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Humanities")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_hum));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_hum));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_hum), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Management Sciences")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_ms));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_ms));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_ms), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Mathematics")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_math));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_math));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_math), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Mechanical Engineering")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_me));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_me));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_me), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Meteorology")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_met));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_met));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_met), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Pharmacy")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_pharm));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_pharm));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_pharm), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Physics")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_physics));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_physics));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_physics), PorterDuff.Mode.SRC_ATOP);
            } else if (community.equalsIgnoreCase("Statistics")) {
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_stats));
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_stats));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_stats), PorterDuff.Mode.SRC_ATOP);
            } else {
                drawerProfileImagView.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_stats));
                profileImagePost.setBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.category_stats));
                userCommunityTxtView.getBackground().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.category_stats), PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    protected void onDestroy() {
        mTempPosts = null;
        mPosts = null;
        mAllPosts = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                gotoSearchActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void gotoSearchActivity() {
        startActivity(new Intent(com.example.android.ideation.MainActivity.this, SearchActivity.class).putExtra("type", "post").putExtra("posts", (Serializable) mPosts));
    }

    @Override
    public void showPostDetail(PostModel post) {
        Intent intent = new Intent(com.example.android.ideation.MainActivity.this, PostDetailActivity.class);
        intent.putExtra("PostObject", post);
        startActivity(intent);
    }

    @Override
    public void showProfile(String id) {
        Intent i = new Intent(com.example.android.ideation.MainActivity.this, ProfileActivity.class);
        i.putExtra("uid", id);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscribeToNotifications();
    }
}
