package com.example.socialmediaapp.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.devlomi.circularstatusview.CircularStatusView;
import com.example.socialmediaapp.Adapter.PostAdapter;
import com.example.socialmediaapp.Adapter.StoryAdapter;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.Model.Story;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.Model.UserStories;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class HomeFragment extends Fragment {
    Context context;
    ShimmerRecyclerView storyRv;
    ShimmerRecyclerView dashboardRv;
    ArrayList<Story> list;
    ArrayList<Post> postlist;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    RoundedImageView addStoryImage;
    ActivityResultLauncher<String> galleryLauncher;
    ProgressDialog dialog;
    CircularStatusView statusCircle;
    CircleImageView profileImage;
    RoundedImageView sampleStoryImage;
    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        dashboardRv = view.findViewById(R.id.dashboardRv);
        dashboardRv.showShimmerAdapter();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Story Uploading");
        dialog.setMessage("Please wait....");
        dialog.setCancelable(false);

//        Story Recycler View
        storyRv = view.findViewById(R.id.storyRv);
        storyRv.showShimmerAdapter();
        list = new ArrayList<>();

        StoryAdapter adapter = new StoryAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        storyRv.setLayoutManager(linearLayoutManager);

        database.getReference()
                .child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()){
                        Story story = new Story();
                        story.setStoryBy(storySnapshot.getKey());
                        story.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));
                        ArrayList<UserStories> stories = new ArrayList<>();
                        for (DataSnapshot dataSnapshot : storySnapshot.child("userStories").getChildren()){
                            UserStories userStories = dataSnapshot.getValue(UserStories.class);
                            stories.add(userStories);
                        }
                        story.setStories(stories);
                        list.add(story);
                    }
                    storyRv.setAdapter(adapter);
                    storyRv.hideShimmerAdapter();
                    adapter.notifyDataSetChanged();
                } else {
                    storyRv.hideShimmerAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //setting the status circle and profile image

        statusCircle = view.findViewById(R.id.status_circle);
        profileImage = view.findViewById(R.id.profile_image);
        sampleStoryImage = view.findViewById(R.id.postImg);
        database.getReference()
                .child("Users")
                .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    Picasso.get()
                            .load(user.getProfilePhoto())
                            .placeholder(R.drawable.picture)
                            .into(profileImage);
                    Picasso.get()
                            .load(user.getProfilePhoto())
                            .placeholder(R.drawable.picture)
                            .into(sampleStoryImage);
                    if (!(user.getStoryCount()==0)){
                        statusCircle.setPortionsCount(user.getStoryCount());
                        statusCircle.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.getReference().child("stories")
                        .child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Story story = new Story();
                                    story.setStoryBy(auth.getUid());
                                    story.setStoryAt(snapshot.child("postedBy").getValue(Long.class));
                                    ArrayList<UserStories> stories = new ArrayList<>();
                                    for (DataSnapshot dataSnapshot : snapshot.child("userStories").getChildren()) {
                                        UserStories userStories = dataSnapshot.getValue(UserStories.class);
                                        stories.add(userStories);
                                    }
                                    story.setStories(stories);
                                    ArrayList<MyStory> myStories = new ArrayList<>();

                                    for (UserStories storries : story.getStories()) {
                                        myStories.add(new MyStory(
                                                storries.getImage()
                                        ));
                                    }
                                    new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                                            .setStoriesList(myStories) // Required
                                            .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                                            .setSubtitleText("") // Default is Hidden
                                            .setStoryClickListeners(new StoryClickListeners() {
                                                @Override
                                                public void onDescriptionClickListener(int position) {
                                                    //your action
                                                }

                                                @Override
                                                public void onTitleIconClickListener(int position) {
                                                    //your action
                                                }
                                            }) // Optional Listeners
                                            .build() // Must be called before calling show method
                                            .show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

//        Dashboard Recycler View

        postlist = new ArrayList<>();

        PostAdapter postAdapter = new PostAdapter(postlist,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        dashboardRv.setLayoutManager(layoutManager);
        dashboardRv.setNestedScrollingEnabled(false);


        database.getReference().child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postlist.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postlist.add(post);
                }
                dashboardRv.setAdapter(postAdapter);
                dashboardRv.hideShimmerAdapter();
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addStoryImage = view.findViewById(R.id.postImg);
        addStoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryLauncher.launch("image/*");
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                addStoryImage.setImageURI(result);

                dialog.show();
                final StorageReference reference = storage.getReference()
                        .child("stories")
                        .child(FirebaseAuth.getInstance().getUid())
                        .child(new Date().getTime() + "");
                reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Story story = new Story();
                                story.setStoryAt(new Date().getTime());

                                database.getReference()
                                        .child("stories")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .child("postedBy")
                                        .setValue(story.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        UserStories stories = new UserStories(uri.toString(), story.getStoryAt());

                                        database.getReference()
                                                .child("stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("userStories")
                                                .push()
                                                .setValue(stories).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });



        return view;
    }
}