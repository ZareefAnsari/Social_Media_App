package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.Model.SavedPosts;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.SavedPostRvSampleBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SavedPostsAdapter extends RecyclerView.Adapter<SavedPostsAdapter.viewHolder> {
    ArrayList<SavedPosts>list;
    Context context;

    public SavedPostsAdapter(ArrayList<SavedPosts> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.saved_post_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        SavedPosts savedPosts = list.get(position);
        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(savedPosts.getPostId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Post post = snapshot.getValue(Post.class);
                Picasso.get()
                        .load(post.getPostImage())
                        .placeholder(R.drawable.picture)
                        .into(holder.binding.savedPost);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        SavedPostRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SavedPostRvSampleBinding.bind(itemView);
        }
    }
}
