package com.example.pintest1.navigation;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pintest1.*;
import com.example.pintest1.databinding.ItemDetailviewBinding;
import com.example.pintest1.model.ContentDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class DetailViewFragment extends Fragment {

    private FirebaseUser user;
    private FirebaseFirestore firestore;

    public DetailViewFragment() {
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        firestore = FirebaseFirestore.getInstance();

        View view = inflater.inflate(R.layout.fragment_detailview, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detailviewfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new DetailRecyclerViewAdapter());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ((MainActivity) getActivity()).getBinding().progressBar.setVisibility(View.GONE);
    }

    private class DetailRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<ContentDTO> contentDTOs;
        private ArrayList<String> contentUidList;

        DetailRecyclerViewAdapter() {

            contentDTOs = new ArrayList<>();
            contentUidList = new ArrayList<>();

            firestore.collection("images").orderBy("timestamp").addSnapshotListener(
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            contentDTOs.clear();
                            contentUidList.clear();
                            for(DocumentSnapshot document:queryDocumentSnapshots.getDocuments()){
                                contentDTOs.add(document.toObject(ContentDTO.class));
                                contentUidList.add(document.getId());
                            }
                            notifyDataSetChanged();
                        }
                    });


        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_detailview, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            final int finalPosition = position;
            final ItemDetailviewBinding binding = ((CustomViewHolder) holder).getBinding();


            // 유저 아이디
            binding.detailviewitemProfileTextview.setText(contentDTOs.get(position).userId);

            // 가운데 이미지
            Glide.with(holder.itemView.getContext()).load("https://firebasestorage.googleapis.com/v0/b/pintest1-589d7.appspot.com/o?name=images%2Fimage%3A46&uploadType=resumable&upload_id=AAANsUkQ1PC0xz6vvvro0uN_7tXtGuwNPw_dTf1HDtXpTqM_F1iGomwfKVVb7yFz3GWMBR_rPc3BFCu0ozXz11V9nTI&upload_protocol=resumable").into(binding.detailviewitemImageviewContent);

            // 설명 텍스트
            binding.detailviewitemExplainTextview.setText(contentDTOs.get(position).explain);

            //좋아요
            binding.detailviewitemFavoritecounterTextview.setText("Likes " + contentDTOs.get(position).favoriteCount);


        }

        @Override
        public int getItemCount() {

            return contentDTOs.size();
        }



        private class CustomViewHolder extends RecyclerView.ViewHolder {

            private ItemDetailviewBinding binding;

            CustomViewHolder(View itemView) {
                super(itemView);

                binding = DataBindingUtil.bind(itemView);
            }

            ItemDetailviewBinding getBinding() {

                return binding;
            }
        }
    }
}