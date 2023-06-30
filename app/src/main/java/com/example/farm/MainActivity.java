package com.example.farm;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.farm.Fragment.CameraFragment;
import com.example.farm.Fragment.HomeFragment;
import com.example.farm.Fragment.HomeFragment2;
import com.example.farm.Fragment.MyInfoFragment;
import com.example.farm.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment fragment_home, fragment_camera, fragment_search, fragment_myInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        fragment_home = new HomeFragment2();
        fragment_camera = new CameraFragment();
        fragment_search = new SearchFragment();
        fragment_myInfo = new MyInfoFragment();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // 초기 Fragment설정
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_home).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_home).commitAllowingStateLoss();
                        return true;
                    case R.id.camera:
                        //getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_camera).commitAllowingStateLoss();
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivity(intent);
                        return true;
                    case R.id.search:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_search).commitAllowingStateLoss();
                        return true;
                    case R.id.my:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, fragment_myInfo).commitAllowingStateLoss();
                        return true;
                }
                return true;
            }
        });
    }

}