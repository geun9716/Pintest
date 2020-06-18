package com.example.pintest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pintest1.model.ContentDTO;
import com.example.pintest1.model.RoadDTO;
import com.example.pintest1.navigation.AddPhotoActivity;
import com.example.pintest1.navigation.UserFragment;
import com.example.pintest1.databinding.ItemListBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MakeRoadActivity extends AppCompatActivity {
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore db;
    ItemListBinding binding;
    FirebaseAuth auth;
    String uid;
    private RoadDTO road;
    private ArrayList<ContentDTO> pins;
    private ArrayList<String> pIDs;
    private ArrayList<String> imageURLs;
    private ArrayList<ContentDTO> contentDTOs ;
    private ArrayList<String> contentUidList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_road);
        View decorView = getWindow().getDecorView();


        pins = new ArrayList<ContentDTO>();
        pIDs = new ArrayList<String>();
        imageURLs = new ArrayList<String>();

        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        uid = getIntent().getStringExtra("destinationUid");
//        road = (RoadDTO) intent.getSerializableExtra("Road");

        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview_makeroad);
        rv.setLayoutManager(new GridLayoutManager(this,2));
        rv.setAdapter(new MakeRoadViewRecyclerAdapter());

        Button button = (Button) findViewById(R.id.btn_make_road);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(contentDTOs.isEmpty()){
                    return;
                }
                RoadDTO newRoad = new RoadDTO(pins, pIDs, imageURLs);
                newRoad.setuId(auth.getCurrentUser().getUid());
                newRoad.setuserId(auth.getCurrentUser().getEmail());
                newRoad.setTimestamp();
                db = FirebaseFirestore.getInstance();
                db.collection("Roads").document().set(newRoad);
                finish();
            }
        });

    }
    private class MakeRoadViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        MakeRoadViewRecyclerAdapter(){
            contentDTOs = new ArrayList<ContentDTO>();
            contentUidList = new ArrayList<String>();

            FirebaseFirestore.getInstance().collection("images")
                    .whereEqualTo("uid",uid).get().addOnSuccessListener(
                    new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (queryDocumentSnapshots == null) return ;
                            for(DocumentSnapshot document:queryDocumentSnapshots.getDocuments()){
                                contentDTOs.add(document.toObject(ContentDTO.class));
                                contentUidList.add(document.getId());
                            }
                            notifyDataSetChanged();
                        }
                    }
            );
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_list, parent, false);

            return new CustomViewHolder(view);
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{

            private ItemListBinding binding;

            CustomViewHolder(View itemView) {
                super(itemView);

                binding = DataBindingUtil.bind(itemView);
            }

            ItemListBinding getBinding() {

                return binding;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final ItemListBinding mybinding = ((CustomViewHolder) holder).getBinding();
            final int finalPosition = position;

            Glide.with(MakeRoadActivity.this)
                    .load(contentDTOs.get(finalPosition).imageUrl)
                    .into(mybinding.listItemImage);

            mybinding.listItemText.setText(contentDTOs.get(finalPosition).explain);

            mybinding.listItemCheckbox.setChecked(false);

            mybinding.listItemImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mybinding.listItemCheckbox.isChecked() == false) {
                        pins.add(contentDTOs.get(finalPosition));
                        pIDs.add(contentUidList.get(finalPosition));
                        imageURLs.add(contentDTOs.get(finalPosition).imageUrl);
                        mybinding.listItemCheckbox.setChecked(true);
                    }
                    else {
                        pins.remove(contentDTOs.get(finalPosition));
                        pIDs.remove(contentUidList.get(finalPosition));
                        imageURLs.remove(contentDTOs.get(finalPosition).imageUrl);
                        mybinding.listItemCheckbox.setChecked(false);
                    }
                }
            });
            mybinding.listItemCheckbox.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (mybinding.listItemCheckbox.isChecked() == true) {
                        pins.add(contentDTOs.get(finalPosition));
                        pIDs.add(contentUidList.get(finalPosition));
                        imageURLs.add(contentDTOs.get(finalPosition).imageUrl);
                    }
                    else {
                        pins.remove(contentDTOs.get(finalPosition));
                        pIDs.remove(contentUidList.get(finalPosition));
                        imageURLs.remove(contentDTOs.get(finalPosition).imageUrl);
                    }
                }
            });
        }
        @Override
        public int getItemCount() {
            return contentDTOs.size();
        }
    }

}