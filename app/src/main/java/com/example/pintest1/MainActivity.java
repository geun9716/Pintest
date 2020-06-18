package com.example.pintest1;

import android.Manifest;


import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pintest1.databinding.ActivityMainBinding;
import com.example.pintest1.navigation.AddPhotoActivity;
import com.example.pintest1.navigation.DetailViewFragment;
import com.example.pintest1.navigation.GridFragment;
import com.example.pintest1.navigation.AlarmFragment;
import com.example.pintest1.navigation.RoadFragment;
import com.example.pintest1.navigation.UserFragment;
import com.example.pintest1.navigation.arsFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.pintest1.util.StatusCode.FRAGMENT_ARG;
import static com.example.pintest1.util.StatusCode.PICK_IMAGE_FROM_ALBUM;
import static com.example.pintest1.util.StatusCode.PICK_PROFILE_FROM_ALBUM;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private ActivityMainBinding binding;

    private Uri photoPath;
    Fragment arsViewFragment = new arsFragment();

    private Scene scene;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.progressBar.setVisibility(View.VISIBLE);

        // Bottom Navigation View
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
        binding.bottomNavigation.setSelectedItemId(R.id.action_home);



    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_home:
                setToolbarDefault();

                /*Fragment detailViewFragment = new DetailViewFragment();
                Bundle bundle_0 = new Bundle();
                bundle_0.putInt(FRAGMENT_ARG, 0);

                detailViewFragment.setArguments(bundle_0);*/

                Bundle bundle_0 = new Bundle();
                bundle_0.putInt(FRAGMENT_ARG, 0);

                arsViewFragment.setArguments(bundle_0);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, arsViewFragment).commit();

                return true;

            case R.id.action_search:
                setToolbarDefault();
                Fragment roadFragment = new RoadFragment();

                Bundle bundle_1 = new Bundle();
                bundle_1.putInt(FRAGMENT_ARG, 1);

                roadFragment.setArguments(bundle_1);

                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, roadFragment).commit();

                return true;

            case R.id.action_add_photo:

                setToolbarDefault();

                startActivity(new Intent(MainActivity.this, AddPhotoActivity.class));

                return true;

            case R.id.action_favorite_alarm:
                setToolbarDefault();
                Fragment alarmFragment = new AlarmFragment();

                Bundle bundle_3 = new Bundle();
                bundle_3.putInt(FRAGMENT_ARG, 3);

                alarmFragment.setArguments(bundle_3);

                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, alarmFragment).commit();

                return true;

            case R.id.action_account:

                Fragment userFragment = new UserFragment();

                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Bundle bundle_4 = new Bundle();
                bundle_4.putString("destinationUid", uid);
                bundle_4.putInt(FRAGMENT_ARG, 4);

                userFragment.setArguments(bundle_4);
                getSupportFragmentManager().beginTransaction().replace(R.id.main_content, userFragment).commit();

                return true;
        }

        return false;
    }

    public void setToolbarDefault() {

        binding.toolbarTitleImage.setVisibility(View.VISIBLE);
        binding.ivMenu.setVisibility(View.GONE);
        binding.toolbarBtnBack.setVisibility(View.GONE);
        binding.toolbarUsername.setVisibility(View.GONE);

    }

    public ActivityMainBinding getBinding() {
        return binding;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == RESULT_OK) {
            photoPath = data.getData();
            final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            StorageReference storageRef =
                    FirebaseStorage.getInstance().getReferenceFromUrl("gs://pintest1-589d7.appspot.com").child("userProfileimages").child(uid);
            UploadTask uploadTask = storageRef.putFile(photoPath);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    binding.progressBar.setVisibility(View.GONE);

                    final Map<String, Object> map = new HashMap<String, Object>();

                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            map.put(uid, uri.toString());
                            FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map);
                        }
                    });
                }
            });
        }
        else if(requestCode ==PICK_IMAGE_FROM_ALBUM &&resultCode ==RESULT_OK)
        {
            binding.bottomNavigation.setSelectedItemId(R.id.action_account);
        }
    }
}
