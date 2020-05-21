package com.example.pintest1.navigation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pintest1.*;
import com.example.pintest1.databinding.ItemDetailviewBinding;
import com.example.pintest1.model.ContentDTO;
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

            firestore.collection("images").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            contentDTOs.clear();
                            contentUidList.clear();
                            if (queryDocumentSnapshots == null) return ;
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

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

            final int finalPosition = position;
            final ItemDetailviewBinding binding = ((CustomViewHolder) holder).getBinding();

            //Profile Image
            binding.detailviewitemProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment fragment = new UserFragment();

                    Bundle bundle = new Bundle();
                    bundle.putString("destinationUid", contentDTOs.get(finalPosition).uid);
                    bundle.putString("userId",contentDTOs.get(finalPosition).userId);
                    bundle.putInt("ARG_NO",5);

                    fragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_content, fragment).commit();
                }
            });

            // 유저 아이디
            binding.detailviewitemProfileTextview.setText(contentDTOs.get(position).userId);

            // 가운데 이미지
            Glide.with(holder.itemView).load(contentDTOs.get(position).imageUrl).into(binding.detailviewitemImageviewContent);

            // 설명 텍스트
            binding.detailviewitemExplainTextview.setText(contentDTOs.get(position).explain);

            //좋아요
            binding.detailviewitemFavoritecounterTextview.setText("Likes " + contentDTOs.get(position).favoriteCount);

            //This code is when the button is clicked
            binding.detailviewitemFavoriteImageview.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    favoriteEvent(finalPosition);
                    return false;
                }
            });


            if(contentDTOs.get(position).favorites.containsKey(FirebaseAuth.getInstance().getCurrentUser().getUid())){
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

        private void favoriteEvent(int position){
            final int finalPosition = position;
            final DocumentReference tsDoc = firestore.collection("images").document(contentUidList.get(position));
            firestore.runTransaction(new Transaction.Function<Handler>() {

                @Nullable
                @Override
                public Handler apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    ContentDTO contentDTO = transaction.get(tsDoc).toObject(ContentDTO.class);

                    if(contentDTO.favorites.containsKey(uid)){
                        //When the button is clicked
                        contentDTO.favoriteCount = contentDTO.favoriteCount-1;
                        contentDTO.favorites.remove(uid);
                    }
                    else{
                        //When the button is not clicked
                        contentDTO.favoriteCount = contentDTO.favoriteCount+1;
                        contentDTO.favorites.put(uid,true);
                    }
                    transaction.set(tsDoc,contentDTO);

                    return null;
                }
            });


        }
    }
}