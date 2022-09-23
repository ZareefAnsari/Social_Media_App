package com.example.socialmediaapp;

import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.socialmediaapp.databinding.ActivityMainBinding;
import com.example.socialmediaapp.fragment.AddFragment;
import com.example.socialmediaapp.fragment.HomeFragment;
import com.example.socialmediaapp.fragment.NotificationFragment;
import com.example.socialmediaapp.fragment.ProfileFragment;
import com.example.socialmediaapp.fragment.SearchFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.iammert.library.readablebottombar.ReadableBottomBar;

public class MainActivity extends AppCompatActivity {
    private long backPressedTime;
    private Toast backToast;
    ActivityMainBinding binding;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        MainActivity.this.setTitle("My Profile");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        binding.toolbar.setVisibility(View.GONE);
        transaction.replace(R.id.container,new HomeFragment());
        transaction.commit();
        binding.readableBottomBar.setOnItemSelectListener(new ReadableBottomBar.ItemSelectListener() {
            @Override
            public void onItemSelected(int i) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (i){
                    case 0:
                        binding.toolbar.setVisibility(View.GONE);
                        transaction.replace(R.id.container,new HomeFragment());
                        break;
                        case 1:
                            binding.toolbar.setVisibility(View.GONE);
                            transaction.replace(R.id.container,new NotificationFragment());
                        break;
                        case 2:
                            binding.toolbar.setVisibility(View.GONE);
                            transaction.replace(R.id.container,new AddFragment());
                        break;
                        case 3:
                            binding.toolbar.setVisibility(View.GONE);
                            transaction.replace(R.id.container,new SearchFragment());
                        break;
                    case 4:
                        binding.toolbar.setVisibility(View.VISIBLE);
                        transaction.replace(R.id.container,new ProfileFragment());
                        break;
                }
                transaction.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_items,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                auth.signOut();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()){
            backToast.cancel();
            super.onBackPressed();
            finish();
        } else {
            backToast = Toast.makeText(getBaseContext(),"Press back again to get exit",Toast.LENGTH_SHORT);
            backToast.show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}