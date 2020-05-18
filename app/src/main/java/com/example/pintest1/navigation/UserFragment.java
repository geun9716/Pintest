package com.example.pintest1.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.pintest1.LoginActivity;
import com.example.pintest1.MainActivity;
import com.example.pintest1.R;
import com.example.pintest1.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class UserFragment extends Fragment {
    private ActivityMainBinding binding;
    Context context;
    FirebaseAuth firebaseAuth;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user,container, false);
        context = container.getContext();

        firebaseAuth = FirebaseAuth.getInstance();

        TabHost tabHost = (TabHost) view.findViewById(R.id.th_PinOrRoad);
        tabHost.setup();

        TabHost.TabSpec ts1 =tabHost.newTabSpec("Tab Spec 1");
        ts1.setContent(R.id.MyPin);
        ts1.setIndicator("My Pin");
        tabHost.addTab(ts1);

        TabHost.TabSpec ts2 = tabHost.newTabSpec("Tab Spec 2");
        ts2.setContent(R.id.MyRoad);
        ts2.setIndicator("My Road");
        tabHost.addTab(ts2);

        binding = ((MainActivity) getActivity()).getBinding();
        binding.ivMenu.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.iv_menu){
//                    Toast.makeText(context, "Menu Selected", Toast.LENGTH_SHORT).show();
                    firebaseAuth.signOut();
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });


        return view;
    }

/*    public void onClick(View v) {
        if(v.getId() == R.id.iv_menu){

            Toast.makeText(context , "menu Selected", Toast.LENGTH_SHORT).show();

        }
    }*/
}
