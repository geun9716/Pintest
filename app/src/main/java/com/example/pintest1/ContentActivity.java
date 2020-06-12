package com.example.pintest1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.pintest1.databinding.ItemDetailviewBinding;
import com.example.pintest1.model.AlarmDTO;
import com.example.pintest1.model.ContentDTO;
import com.example.pintest1.model.RoadDTO;
import com.example.pintest1.navigation.CommentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

public class ContentActivity extends AppCompatActivity {

    ContentDTO contentDTO;
    RoadDTO road;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        Intent intent = getIntent();
        contentDTO = (ContentDTO) intent.getSerializableExtra("Content");
        road = (RoadDTO) intent.getSerializableExtra("Road");
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview_content);
        rv.setLayoutManager(new GridLayoutManager(this,1));
        rv.setAdapter(new ContentActivity.ContentViewRecyclerAdapter());

    }
    private class ContentViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_detailview, parent, false);


            return new ContentActivity.ContentViewRecyclerAdapter.CustomViewHolder(view);
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{

            private ItemDetailviewBinding binding;

            CustomViewHolder(View itemView) {
                super(itemView);

                binding = DataBindingUtil.bind(itemView);
            }

            ItemDetailviewBinding getBinding() {

                return binding;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {

            if(road != null)
                contentDTO = road.getPin(position);

            int width = getResources().getDisplayMetrics().widthPixels / 3;
            final ItemDetailviewBinding binding = ((ContentActivity.ContentViewRecyclerAdapter.CustomViewHolder) holder).getBinding();

            Glide.with(ContentActivity.this)
                    .load(contentDTO.imageUrl)
                    .into(binding.detailviewitemImageviewContent);

            binding.detailviewitemProfileTextview.setText(contentDTO.userId);
            binding.detailviewitemFavoritecounterTextview.setText(Integer.toString(contentDTO.favoriteCount));
            binding.detailviewitemExplainTextview.setText(contentDTO.explain);

            if(contentDTO.favorites.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                //This is like status
                binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite_border);
            }
            else
            {
                //This is unlike status
                binding.detailviewitemFavoriteImageview.setImageResource(R.drawable.ic_favorite);
            }
        }
        @Override
        public int getItemCount() {
            if(road == null)
                return 1;
            else
                return road.getPins().size();
        }
    }
}
