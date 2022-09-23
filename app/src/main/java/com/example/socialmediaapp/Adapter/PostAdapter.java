package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.CommentActivity;
import com.example.socialmediaapp.Model.Notification;
import com.example.socialmediaapp.Model.Post;
import com.example.socialmediaapp.Model.SavedPosts;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.DashboardRvSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    ArrayList<Post>list;
    Context context;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Post model = list.get(position);
        Picasso.get()
                .load(model.getPostImage())
                .placeholder(R.drawable.picture)
                .into(holder.binding.postImg);
        holder.binding.like.setText(model.getPostLike()+"");
        holder.binding.comment.setText(model.getCommentCount()+"");

        String description = model.getPostDescription();
        if (description.equals("")){
            holder.binding.postDes.setVisibility(View.GONE);
        }else {
            holder.binding.postDes.setText(model.getPostDescription());
            holder.binding.postDes.setVisibility(View.VISIBLE);
        }
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(model.getPostedBy()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Picasso.get()
                        .load(user.getProfilePhoto())
                        .placeholder(R.drawable.picture)
                        .into(holder.binding.profileImage);
                holder.binding.userName.setText(user.getName());
                holder.binding.about.setText(user.getProfession());
                if (!(user.getStoryCount() == 0)) {
                    holder.binding.statusCircle.setPortionsCount(user.getStoryCount());
                    holder.binding.statusCircle.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // like setting
        FirebaseDatabase.getInstance().getReference()
                .child("posts")
                .child(model.getPostId())
                .child("likes")
                .child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart, 0, 0, 0);
                        }else {
                            holder.binding.like.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("posts")
                                            .child(model.getPostId())
                                            .child("likes")
                                            .child(FirebaseAuth.getInstance().getUid())
                                            .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("posts")
                                                    .child(model.getPostId())
                                                    .child("postLike")
                                                    .setValue(model.getPostLike()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Notification notification = new Notification();
                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                    notification.setNotificationAt(new Date().getTime());
                                                    notification.setPostID(model.getPostId());
                                                    notification.setPostedBy(model.getPostedBy());
                                                    notification.setType("like");

                                                    if (!model.getPostedBy().equals(FirebaseAuth.getInstance().getUid())){
                                                        FirebaseDatabase.getInstance().getReference()
                                                            .child("notification")
                                                            .child(model.getPostedBy())
                                                            .push()
                                                            .setValue(notification);}
                                                }
                                            });
                                            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart, 0, 0, 0);
                                            holder.binding.like.setText(model.getPostLike()+"");
                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

//         saving the post
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getUid())
                .child("savedPosts")
                .child(model.getPostId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.binding.save.setImageResource(R.drawable.save1);
                } else {
                    holder.binding.save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SavedPosts savedPosts = new SavedPosts();
                            savedPosts.setPostId(model.getPostId());
                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .child("savedPosts")
                                    .child(model.getPostId())
                                    .setValue(savedPosts).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    holder.binding.save.setImageResource(R.drawable.save1);
                                }
                            });
                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//         comment setting
        holder.binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId",model.getPostId());
                intent.putExtra("postedBy",model.getPostedBy());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        DashboardRvSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DashboardRvSampleBinding.bind(itemView);
        }
    }
}
