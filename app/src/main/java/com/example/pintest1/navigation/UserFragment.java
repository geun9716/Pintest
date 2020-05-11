package com.example.pintest1.navigation;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pintest1.R;

public class UserFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user,container, false);

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

        return view;
    }
}
