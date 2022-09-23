package com.example.socialmediaapp.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.socialmediaapp.Adapter.FollowersAdapter;
import com.example.socialmediaapp.Adapter.SavedPostsAdapter;
import com.example.socialmediaapp.CallActivity;
import com.example.socialmediaapp.Model.Follow;
import com.example.socialmediaapp.Model.SavedPosts;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    ArrayList<Follow>friendList;
    ArrayList<SavedPosts>savedPostsList;
    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    ProgressDialog dialog;

    public ProfileFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         binding = FragmentProfileBinding.inflate(inflater,container,false);
         database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot snapshot) {
                 if (snapshot.exists()){
                     User user = snapshot.getValue(User.class);
                     assert user != null;
                     Picasso.get()
                             .load(user.getCoverPhoto())
                             .placeholder(R.drawable.picture)
                             .into(binding.backImage);
                     Picasso.get()
                             .load(user.getProfilePhoto())
                             .placeholder(R.drawable.picture)
                             .into(binding.profileImage);
                     binding.profileName.setText(user.getName());
                     binding.profession.setText(user.getProfession());
                     binding.followers.setText(user.getFollowersCount()+"");
                     binding.postsCount.setText(user.getPosts()+"");
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError error) {

             }
         });

         // making a phone call
        binding.callView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CallActivity.class);
                getActivity().startActivity(intent);
            }
        });

        // adding friend
        binding.addFriendView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fr = getFragmentManager().beginTransaction();
                fr.replace(R.id.container,new SearchFragment());
                fr.commit();

            }
        });

//        saved posts list
        savedPostsList = new ArrayList<>();
        SavedPostsAdapter postsAdapter = new SavedPostsAdapter(savedPostsList,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.savedPostsRv.setLayoutManager(linearLayoutManager);
        binding.savedPostsRv.setNestedScrollingEnabled(false);
        binding.savedPostsRv.setAdapter(postsAdapter);
        database.getReference().child("Users")
                .child(auth.getUid())
                .child("savedPosts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    binding.savedpost.setVisibility(View.GONE);
                } else {
                    savedPostsList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        SavedPosts savedPosts = dataSnapshot.getValue(SavedPosts.class);
                        savedPostsList.add(savedPosts);
                    }
                    binding.savedpost.setVisibility(View.VISIBLE);
                    postsAdapter.notifyDataSetChanged();
                }
            }

                @Override
                public void onCancelled (@NonNull DatabaseError error){

                }
        });

        // friends list
         friendList = new ArrayList<>();
        FollowersAdapter adapter = new FollowersAdapter(friendList,getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.friendsRv.setLayoutManager(layoutManager);
        binding.friendsRv.setNestedScrollingEnabled(false);
        binding.friendsRv.setAdapter(adapter);
        database.getReference().child("Users")
                .child(auth.getUid())
                .child("followers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    friendList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Follow follow = dataSnapshot.getValue(Follow.class);
                        friendList.add(follow);
                    }
                    binding.myFollowers.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                } else {
                    binding.myFollowers.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.changeCoverPhoto.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,11);

        });

        binding.editImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,22);

        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        assert data != null;
        if (requestCode == 11){
            if (data.getData()!=null){
                Uri uri = data.getData();
                binding.backImage.setImageURI(uri);
                final StorageReference reference = storage.getReference().child("cover_photo").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                reference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getContext(), "cover photo updated", Toast.LENGTH_SHORT).show();
                    reference.getDownloadUrl().addOnSuccessListener(uri1 -> database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("coverPhoto").setValue(uri1.toString()));
                });
            }
        } else {
            if (data.getData()!=null){
                Uri uri = data.getData();
                binding.profileImage.setImageURI(uri);
                final StorageReference reference = storage.getReference().child("profilePhoto").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
                reference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getContext(), "profile photo updated", Toast.LENGTH_SHORT).show();
                    reference.getDownloadUrl().addOnSuccessListener(uri12 -> database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).child("profilePhoto").setValue(uri12.toString()));

                });
            }
        }

    }
}