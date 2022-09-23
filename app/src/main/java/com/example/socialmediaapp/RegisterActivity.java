package com.example.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.socialmediaapp.Model.User;
import com.example.socialmediaapp.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        binding.register.setOnClickListener(view -> {
            String email = binding.email.getText().toString(), password = binding.password.getText().toString();
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        User user = new User(binding.userName.getText().toString(),binding.profession.getText().toString(),email,password);
                        String id = task.getResult().getUser().getUid();
                        database.getReference().child("Users").child(id).setValue(user);
                        Toast.makeText(RegisterActivity.this,"User data saved",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(RegisterActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.gotologin.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}