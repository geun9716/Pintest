package com.example.pintest1;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;


import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.pintest1.databinding.ActivityMainBinding;
import com.example.pintest1.navigation.AddPhotoActivity;
import com.example.pintest1.navigation.DetailViewFragment;
import com.example.pintest1.navigation.GridFragment;
import com.example.pintest1.navigation.AlarmFragment;
import com.example.pintest1.navigation.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkSelfPermission();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.progressBar.setVisibility(View.VISIBLE);

        // Bottom Navigation View
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);

    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //권한을 허용 했을 경우
        if(requestCode == 1){
            int length = permissions.length;
            for (int i = 0; i < length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) { // 동의
                    Log.d("MainActivity","권한 허용 : " + permissions[i]);
                }
            }
        }
    }
    public void checkSelfPermission() {
        String temp = "";
        //파일 읽기 권한 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        }

        if (TextUtils.isEmpty(temp) == false) {
            // 권한 요청
            ActivityCompat.requestPermissions(this, temp.trim().split(" "), 1);
        } else {
            // 모두 허용 상태
            Toast.makeText(this, "권한을 모두 허용", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_home:

                Fragment detailViewFragment = new DetailViewFragment();
                Bundle bundle_0 = new Bundle();
                bundle_0.putInt("ARG_NO", 0);

                detailViewFragment.setArguments(bundle_0);

                getFragmentManager().beginTransaction()
                        .replace(R.id.main_content, detailViewFragment)
                        .commit();

                return true;

            case R.id.action_search:

                Fragment gridFragment = new GridFragment();

                Bundle bundle_1 = new Bundle();
                bundle_1.putInt("ARG_NO", 1);

                gridFragment.setArguments(bundle_1);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, gridFragment)
                        .commit();

                return true;

            case R.id.action_add_photo:

                setToolbarDefault();

                startActivity(new Intent(MainActivity.this, AddPhotoActivity.class));

                return true;

            case R.id.action_favorite_alarm:


                Fragment alarmFragment = new AlarmFragment();

                Bundle bundle_3 = new Bundle();
                bundle_3.putInt("ARG_NO", 3);

                alarmFragment.setArguments(bundle_3);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_content, alarmFragment)
                        .commit();

                return true;

            case R.id.action_account:
                Fragment userFragment = new UserFragment();

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Bundle bundle = new Bundle();
                bundle.putString("destinationUid", uid);
                bundle.putInt("ARG_NO", 4);

                userFragment.setArguments(bundle);
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_content, userFragment)
                        .commit();

                return true;
        }

        return false;
    }
    public void setToolbarDefault() {

        binding.toolbarTitleImage.setVisibility(View.VISIBLE);
        binding.toolbarBtnBack.setVisibility(View.GONE);
        binding.toolbarUsername.setVisibility(View.GONE);
    }
}
