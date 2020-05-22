package com.example.pintest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pintest1.model.ContentDTO;
import com.example.pintest1.model.RoadDTO;
import com.example.pintest1.navigation.UserFragment;
import com.example.pintest1.databinding.ItemListBinding;

import java.util.ArrayList;

public class MakeRoadActivity extends AppCompatActivity {

    ItemListBinding binding;
    private RoadDTO road;
    private ArrayList<ContentDTO> contentDTOs = new ArrayList<ContentDTO>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_road);

        Intent intent = getIntent();
        road = (RoadDTO) intent.getSerializableExtra("Road");

        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview_makeroad);
        rv.setLayoutManager(new GridLayoutManager(this,2));
        rv.setAdapter(new MakeRoadViewRecyclerAdapter());
    }
    private class MakeRoadViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

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

            binding = ((CustomViewHolder) holder).getBinding();

            Glide.with(MakeRoadActivity.this)
                    .load(road.getPin(position).imageUrl)
                    .into(binding.listItemImage);

            binding.listItemText.setText(road.getPin(position).getText());

            binding.listItemCheckbox.setChecked(false);
        }

        @Override
        public int getItemCount() {
            return road.getCountOfPins();
        }
    }

}
