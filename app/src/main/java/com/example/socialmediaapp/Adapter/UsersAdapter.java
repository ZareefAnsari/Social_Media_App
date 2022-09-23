package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.circularstatusview.CircularStatusView;
import com.example.socialmediaapp.Model.Follow;
import com.example.socialmediaapp.Model.Notification;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolder> {
    ArrayList<User> list;
    public Context context;

    public UsersAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User model = list.get(position);
        Picasso.get()
                .load(model.getProfilePhoto())
                .placeholder(R.drawable.picture)
                .into(holder.profilePhoto);
        holder.profilePhoto.setVisibility(View.VISIBLE);
        holder.name.setText(model.getName());
        holder.profession.setText(model.getProfession());
        holder.statusView.setPortionsCount(model.getStoryCount());
        holder.name.setVisibility(View.VISIBLE);
        holder.profession.setVisibility(View.VISIBLE);
        holder.statusView.setVisibility(View.VISIBLE);


        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(model.getUserId())
                .child("followers")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    holder.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.follow_active_btn));
                    holder.followBtn.setText("Following");
                    holder.followBtn.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    holder.followBtn.setEnabled(false);
                    holder.followBtn.setVisibility(View.VISIBLE);
                }
                else {
                    holder.followBtn.setVisibility(View.VISIBLE);
                    holder.followBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Follow follow = new Follow();
                            follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                            follow.setFollowedAt(new Date().getTime());

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(model.getUserId())
                                    .child("followers")
                                    .child(FirebaseAuth.getInstance().getUid())
                                    .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(model.getUserId())
                                            .child("followersCount")
                                            .setValue(model.getFollowersCount() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            holder.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context,R.drawable.follow_active_btn));
                                            holder.followBtn.setText("Following");
                                            holder.followBtn.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                                            holder.followBtn.setEnabled(false);
                                            Toast.makeText(context, "You followed " + model.getName(), Toast.LENGTH_SHORT).show();

                                            Notification notification = new Notification();
                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                            notification.setNotificationAt(new Date().getTime());
                                            notification.setType("follow");

                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("notification")
                                                    .child(model.getUserId())
                                                    .push()
                                                    .setValue(notification);
                                        }
                                    });
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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder {
        ImageView profilePhoto;
        TextView name, profession;
        Button followBtn;
        CircularStatusView statusView;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            profilePhoto = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.user_name);
            profession = itemView.findViewById(R.id.profession);
            followBtn = itemView.findViewById(R.id.followBtn);
            statusView = itemView.findViewById(R.id.status_circle);
        }
    }
}
