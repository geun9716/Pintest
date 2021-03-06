package com.example.pintest1.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.pintest1.ContentActivity;
import com.example.pintest1.MakeRoadActivity;
import com.example.pintest1.R;
import com.example.pintest1.databinding.ItemListBinding;
import com.example.pintest1.databinding.ItemRoadBinding;
import com.example.pintest1.model.AlarmDTO;
import com.example.pintest1.model.ContentDTO;
import com.example.pintest1.model.RoadDTO;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class RoadFragment extends Fragment {
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        View view = inflater.inflate(R.layout.fragment_road, container, false);


        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.roadfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new RoadFragment.RoadViewRecyclerAdapter());

        return view;
    }

    private class RoadViewRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        ArrayList<RoadDTO> roadDTOs;

        RoadViewRecyclerAdapter(){
            roadDTOs = new ArrayList<RoadDTO>();
            FirebaseFirestore.getInstance().collection("Roads")
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

            return new CustomViewHolder(view);
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder{

            private ItemRoadBinding binding;

            CustomViewHolder(View itemView) {
                super(itemView);

                binding = DataBindingUtil.bind(itemView);
            }

            ItemRoadBinding getBinding() {

                return binding;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            RoadDTO road = roadDTOs.get(position);
            final ItemRoadBinding binding = ((CustomViewHolder) holder).getBinding();

            Glide.with(holder.itemView).load(road.getImageURL(0)).into(binding.roadImage1);

            if(road.getPins().size() > 1)
                Glide.with(holder.itemView).load(road.getImageURL(1)).into(binding.roadImage2);
            if(road.getPins().size() > 2)
                Glide.with(holder.itemView).load(road.getImageURL(2)).into(binding.roadImage3);

            binding.roadView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ContentActivity.class);
                    intent.putExtra("Road", roadDTOs.get(position));
                    startActivity(intent);
                }
            });

        }
        @Override
        public int getItemCount() {
            return roadDTOs.size();
        }
    }

}