package com.example.socialmediaapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.socialmediaapp.CommentActivity;
import com.example.socialmediaapp.Model.Notification;
import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.databinding.NotificationSampleBinding;
import com.example.socialmediaapp.fragment.SearchFragment;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder> {

    ArrayList<Notification> notificationList;
    Context context;

    public NotificationAdapter(ArrayList<Notification> notificationList, Context context) {
        this.notificationList = notificationList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Notification notificationModel = notificationList.get(position);
        String type = notificationModel.getType();
        Boolean checkOpen = notificationModel.isCheckOpen();
        if (checkOpen){
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notificationModel.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get()
                                .load(user.getProfilePhoto())
                                .placeholder(R.drawable.picture)
                                .into(holder.binding.notificationImage);

                        if (type.equals("like")){
                            holder.binding.notification.setText(Html.fromHtml("<b> "+user.getName()+"</b>" + " liked your post."));
                        }else if (type.equals("comment")){
                            holder.binding.notification.setText(Html.fromHtml("<b> "+user.getName()+"</b>" + " commented on your post."));
                        }else {
                            holder.binding.notification.setText(Html.fromHtml("<b> "+user.getName()+"</b>" + " started following you."));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.binding.openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!type.equals("follow")) {
//                    FirebaseDatabase.getInstance().getReference()
//                            .child("notification")
//                            .child(notificationModel.getPostedBy())
//                            .child(notificationModel.getNotificationID())
//                            .child("checkOpen")
//                            .setValue(true);
//                    holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    FirebaseDatabase.getInstance().getReference()
                            .child("notification")
                            .child(notificationModel.getPostedBy())
                            .child(notificationModel.getNotificationID()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Notification notification = snapshot.getValue(Notification.class);
                            Intent intent = new Intent(context, CommentActivity.class);
                            intent.putExtra("postId", notification.getPostID());
                            intent.putExtra("postedBy", notification.getPostedBy());
                            context.startActivity(intent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }

                    });
                }else{
                    AppCompatActivity activity = (AppCompatActivity) view.getContext();
                    Fragment myFragment = new SearchFragment();
                    activity.getSupportFragmentManager().beginTransaction().replace(R.id.container, myFragment).addToBackStack(null).commit();
                }

            }
        });
        String time = TimeAgo.using(notificationModel.getNotificationAt());
        holder.binding.time.setText(time);


    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        NotificationSampleBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = NotificationSampleBinding.bind(itemView);
        }
    }
}
