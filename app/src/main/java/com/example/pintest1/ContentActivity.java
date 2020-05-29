package com.example.pintest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.pintest1.databinding.ItemDetailviewBinding;
import com.example.pintest1.model.ContentDTO;
import com.example.pintest1.navigation.CommentActivity;

public class ContentActivity extends AppCompatActivity {

    ContentDTO contentDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        Intent intent = getIntent();
        contentDTO = (ContentDTO) intent.getSerializableExtra("Content");

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
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            int width = getResources().getDisplayMetrics().widthPixels / 3;
            final ItemDetailviewBinding binding = ((ContentActivity.ContentViewRecyclerAdapter.CustomViewHolder) holder).getBinding();

            Glide.with(ContentActivity.this)
                    .load(contentDTO.imageUrl)
                    .into(binding.detailviewitemImageviewContent);

            binding.detailviewitemProfileTextview.setText(contentDTO.userId);
            binding.detailviewitemFavoritecounterTextview.setText(Integer.toString(contentDTO.favoriteCount));
            binding.detailviewitemExplainTextview.setText(contentDTO.explain);

            binding.detailviewitemFavoriteImageview.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    //좋아요 버튼 눌렀을 경우 좋아요 이벤트

                }
            });
            binding.detailviewitemCommentImageview.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    //댓글 버튼 눌렀을 경우 댓글 표시

                }
            });
        }
        @Override
        public int getItemCount() {
            return 1;
        }
    }
}
