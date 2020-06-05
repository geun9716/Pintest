package com.example.pintest1.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pintest1.ContentActivity;
import com.example.pintest1.LoginActivity;
import com.example.pintest1.MainActivity;
import com.example.pintest1.MakeRoadActivity;
import com.example.pintest1.R;
import com.example.pintest1.databinding.ActivityMainBinding;
import com.example.pintest1.databinding.FragmentUserBinding;
import com.example.pintest1.databinding.ItemRoadBinding;
import com.example.pintest1.model.AlarmDTO;
import com.example.pintest1.model.ContentDTO;
import com.example.pintest1.model.FollowDTO;
import com.example.pintest1.model.RoadDTO;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.pintest1.util.StatusCode.PICK_PROFILE_FROM_ALBUM;

public class UserFragment extends Fragment {

    private ArrayList<RoadDTO> roadDTOs;
    private ArrayList<ContentDTO> contentDTOs;
    private ActivityMainBinding binding;
    private Context context;

    // Data Binding
    private FragmentUserBinding userbinding;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    FirebaseAuth.AuthStateListener authListener;

    //private String destinationUid;
    private String uid;
    private String currentUserUid;

    // Activity
    private MainActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {

            activity = (MainActivity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user,container, false);
        context = container.getContext();
        userbinding = FragmentUserBinding.bind(view);
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        uid = getArguments().getString("destinationUid");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                // User is signed out
                if (user == null) {

                    Toast.makeText(activity, getString(R.string.signout_success), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(activity, LoginActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }
            }
        };

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
        userbinding.addRoad.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MakeRoadActivity.class);
                RoadDTO road = new RoadDTO(contentDTOs);
                intent.putExtra("Road", road);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //userbinding = FragmentUserBinding.bind(getView());
        binding.ivMenu.setVisibility(View.VISIBLE);

        if(getArguments() != null){
            uid = getArguments().getString("destinationUid");

            //MyPage
            if(uid == currentUserUid){

                userbinding.accountBtnFollowSignout.setText(R.string.signout);
                userbinding.accountBtnFollowSignout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SignOut();
                    }
                });
                activity.setToolbarDefault();
            }
            else    //Other User Page
            {
                binding.toolbarTitleImage.setVisibility(View.GONE);
                binding.toolbarBtnBack.setVisibility(View.VISIBLE);
                binding.toolbarUsername.setVisibility(View.VISIBLE);

                binding.toolbarUsername.setText(getArguments().getString("userId"));
                binding.toolbarBtnBack.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
                    }
                });

                userbinding.accountBtnFollowSignout.setText(R.string.follow);
                userbinding.accountBtnFollowSignout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestFollow();
                    }
                });
            }
        }
        //Profile Image Click Listener
        if(getArguments() != null) {
            uid = getArguments().getString("destinationUid");
            //MyPage
            if (uid == currentUserUid) {
                userbinding.accountIvProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        photoPickerIntent.setType("image/*");
                        activity.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM);
                    }
                });
            }
        }

        getProfileImage();
        getFollower();
        getFollowing();

        userbinding.accountRecyclerviewPin.setLayoutManager(new GridLayoutManager(getActivity(),3));
        userbinding.accountRecyclerviewPin.setAdapter(new UserFragmentRecyclerViewAdapter());

        userbinding.accountRecyclerviewRoad.setLayoutManager(new LinearLayoutManager(getActivity()));
        userbinding.accountRecyclerviewRoad.setAdapter(new UserFragmentRoadAdapter());
    }


    private class UserFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        UserFragmentRecyclerViewAdapter(){
            contentDTOs = new ArrayList<ContentDTO>();
            firestore.collection("images").whereEqualTo("uid",uid).addSnapshotListener(
                            new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    contentDTOs.clear();
                    if (queryDocumentSnapshots == null) return ;
                    for(DocumentSnapshot document:queryDocumentSnapshots.getDocuments()){
                        contentDTOs.add(document.toObject(ContentDTO.class));
                    }
                    userbinding.accountTvPostCount.setText(String.valueOf(contentDTOs.size()));
                    notifyDataSetChanged();
                }
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            int width = getResources().getDisplayMetrics().widthPixels / 3;

            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width,width));

            return new CustomViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            ImageView imageView = ((CustomViewHolder) holder).imageView;
            imageView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ContentActivity.class);
                    ContentDTO contentDTO = contentDTOs.get(position);
                    intent.putExtra("Content", contentDTO);
                    startActivity(intent);
                }
            });

            Glide.with(holder.itemView.getContext()).load(contentDTOs.get(position).imageUrl)
                    .apply(new RequestOptions().centerCrop()).into(imageView);
        }

        @Override
        public int getItemCount() {
            return contentDTOs.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            CustomViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }

    private class UserFragmentRoadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        UserFragmentRoadAdapter(){
            roadDTOs = new ArrayList<RoadDTO>();
            FirebaseFirestore.getInstance().collection("Roads")
                    .whereEqualTo("uId", uid)
//                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener(
                            new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                    roadDTOs.clear();
                                    if(queryDocumentSnapshots == null) return ;
                                    for(DocumentSnapshot documentSnapshot: queryDocumentSnapshots.getDocuments())
                                    {
                                            roadDTOs.add(documentSnapshot.toObject(RoadDTO.class));
                                    }
                                    notifyDataSetChanged();
                                }
                            }
                    );

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_road, parent, false);

            return new UserFragment.UserFragmentRoadAdapter.CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            RoadDTO road = roadDTOs.get(position);

            final ItemRoadBinding binding = ((UserFragment.UserFragmentRoadAdapter.CustomViewHolder) holder).getBinding();

            Glide.with(holder.itemView).load(road.getPin(0).imageUrl).into(binding.roadImage1);

            if (road.getPins().size() > 1)
                Glide.with(holder.itemView).load(road.getPin(1).imageUrl).into(binding.roadImage2);
            if (road.getPins().size() > 2)
                Glide.with(holder.itemView).load(road.getPin(2).imageUrl).into(binding.roadImage3);

        }

        @Override
        public int getItemCount() {
            return roadDTOs.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ItemRoadBinding binding;

            CustomViewHolder(View itemView) {
                super(itemView);

                binding = DataBindingUtil.bind(itemView);
            }

            ItemRoadBinding getBinding() {

                return binding;
            }
        }
    }

    /*
    * Get - Profile Image, Follower Count, Following Count
    */
    void getProfileImage(){
        firestore.collection("profileImages").document(uid).addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot == null) return;
                        if(documentSnapshot.getData() != null)
                        {
                            if(documentSnapshot.getData() != null){
                                String url = documentSnapshot.getData().get(uid).toString();
                                Glide.with(activity).load(url).apply(new RequestOptions().circleCrop()).into(userbinding.accountIvProfile);
                            }
                        }
                    }
                }
        );
    }
    void getFollower() {
        firestore.collection("users").document(uid).addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot == null) return;

                        FollowDTO followDTO = documentSnapshot.toObject(FollowDTO.class);
                        try{
                            userbinding.accountTvFollowerCount.setText(Integer.toString(followDTO.followerCount));
                            if(followDTO.followers.containsKey(currentUserUid)){
                                userbinding.accountBtnFollowSignout.setText(R.string.follow_cancel);
                                userbinding.accountBtnFollowSignout.getBackground()
                                        .setColorFilter(ContextCompat.getColor(activity, R.color.colorLightGray), PorterDuff.Mode.MULTIPLY);
                            } else{
                                if(!uid.equals(currentUserUid)){
                                    userbinding.accountBtnFollowSignout.setText(getString(R.string.follow));
                                    userbinding.accountBtnFollowSignout
                                            .getBackground().setColorFilter(null);
                                }
                            }

                        } catch (Exception ex){
                            ex.printStackTrace();
                        }

                    }
                }
        );
    }
    void getFollowing() {
        firestore.collection("users").document(uid).addSnapshotListener(
                new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot == null) return;

                        FollowDTO followDTO = documentSnapshot.toObject(FollowDTO.class);
                        try{
                            userbinding.accountTvFollowingCount.setText(Integer.toString(followDTO.followingCount));
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
        );
    }

    /*
     * Request Follower, Follow Alarm
     */

    public void requestFollow() {
        final DocumentReference tsDocFollowing = firestore.collection("users").document(currentUserUid);
        firestore.runTransaction(
                new Transaction.Function<Handler>() {
                    @Nullable
                    @Override
                    public Handler apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        FollowDTO followDTO = transaction.get(tsDocFollowing).toObject(FollowDTO.class);
                        if (followDTO == null) {
                            followDTO = new FollowDTO();
                            followDTO.followingCount = 1;
                            followDTO.followings.put(uid, true);
                            followerAlarm(uid);
                            transaction.set(tsDocFollowing, followDTO);
                            return null;
                        }

                        if (followDTO.followings.containsKey(uid)) {
                            //It remove following third person when a third person
                            followDTO.followingCount = followDTO.followingCount - 1;
                            followDTO.followings.remove(uid);
                        } else {
                            //It add following third person when a third person do not follow me
                            followDTO.followingCount = followDTO.followingCount + 1;
                            followDTO.followings.put(uid, true);
                            followerAlarm(uid);
                        }
                        transaction.set(tsDocFollowing, followDTO);
                        return null;
                    }
                });
        //Save data to Third person
        final DocumentReference tsDocFollower = firestore.collection("users").document(uid);
        firestore.runTransaction(
                new Transaction.Function<Handler>() {
                    @Nullable
                    @Override
                    public Handler apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        FollowDTO followDTO = transaction.get(tsDocFollower).toObject(FollowDTO.class);
                        if (followDTO == null) {
                            followDTO = new FollowDTO();
                            followDTO.followerCount = 1;
                            followDTO.followers.put(currentUserUid, true);

                            transaction.set(tsDocFollower, followDTO);
                            return null;
                        }

                        if (followDTO.followers.containsKey(currentUserUid)) {
                            //It remove follower third person when a third person follow me
                            followDTO.followerCount = followDTO.followerCount - 1;
                            followDTO.followers.remove(currentUserUid);
                        } else {
                            //It add follower third person when a third person do not follow me
                            followDTO.followerCount = followDTO.followerCount + 1;
                            followDTO.followers.put(currentUserUid, true);
                        }
                        transaction.set(tsDocFollower, followDTO);
                        return null;
                    }
                });
    }
    private void followerAlarm(String destinationUid) {

        AlarmDTO alarmDTO = new AlarmDTO();

        alarmDTO.destinationUid = destinationUid;
        alarmDTO.userId = firebaseAuth.getCurrentUser().getEmail();
        alarmDTO.uid = firebaseAuth.getCurrentUser().getUid();
        alarmDTO.kind = 2;
        alarmDTO.timestamp = System.currentTimeMillis();

        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO);
    }



    private void SignOut() {
        if(firebaseAuth.getCurrentUser().getProviders() != null && firebaseAuth.getCurrentUser().equals("google.com")){
            googleSignOut();
        }
        else {
            firebaseAuth.signOut();
        }
    }

    private void googleSignOut() {
        // GoogleSignInOptions 개체 구성
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the options specified by gso.
        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage((FragmentActivity) activity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        // hideProgressDialog();
                        Toast.makeText(activity, getString(R.string.signout_fail), Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();
        googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(@Nullable Bundle bundle) {

                firebaseAuth.signOut();
                if (googleApiClient.isConnected()) {

                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {

                        @Override
                        public void onResult(@NonNull Status status) {

                            if (!status.isSuccess()) {

                                // hideProgressDialog();
                                Toast.makeText(activity, getString(R.string.signout_fail), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {

                // hideProgressDialog();
                Toast.makeText(activity, getString(R.string.signout_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        firebaseAuth.removeAuthStateListener(authListener);
    }

}
