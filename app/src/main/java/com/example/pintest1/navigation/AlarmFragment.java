package com.example.pintest1.navigation;


import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.example.pintest1.R;
import com.example.pintest1.databinding.ItemAlarmBinding;
import com.example.pintest1.model.AlarmDTO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class AlarmFragment extends Fragment {

    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm,container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.alarmfragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new AlarmRecyclerviewAdapter());

        return view;
    }
    private class AlarmRecyclerviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        ArrayList<AlarmDTO> alarmDTOs;

        public AlarmRecyclerviewAdapter(){
            alarmDTOs = new ArrayList<AlarmDTO>();
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance()
                    .collection("alarms")
                    .whereEqualTo("destinationUid", uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            alarmDTOs.clear();
                            if (queryDocumentSnapshots == null) return ;
                            for(DocumentSnapshot document:queryDocumentSnapshots.getDocuments()){
                                alarmDTOs.add(document.toObject(AlarmDTO.class));
                            }
                            notifyDataSetChanged();
                        }
                    }
            );
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
            return new CustomViewHolder(view);
        }
        private class CustomViewHolder extends RecyclerView.ViewHolder{
            private ItemAlarmBinding binding;

            public CustomViewHolder(@NonNull View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            public ItemAlarmBinding getBinding() {
                return binding;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
            final ItemAlarmBinding binding = ((CustomViewHolder) holder).getBinding();
            final int finalPosition = position;

            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOs.get(position).uid).addSnapshotListener(
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            if(documentSnapshot == null) return;
                            if(documentSnapshot.getData() != null)
                            {
                                String url = documentSnapshot.getData().get(alarmDTOs.get(finalPosition).uid).toString();
                                Glide.with(holder.itemView).load(url).apply(new RequestOptions().circleCrop()).into(binding.alramitemImageviewProfile);
                            }
                        }
                    }
            );

            binding.alramitemTextviewProfile.setText(alarmDTOs.get(position).userId);

            switch (alarmDTOs.get(position).kind) {

                case 0:
                    String str_0 = getString(R.string.alarm_favorite);
                    binding.alramitemTextviewComment.setText(str_0);
                    break;

                case 1:
                    String str_1 = getString(R.string.alarm_comment) + alarmDTOs.get(position).message;
                    binding.alramitemTextviewComment.setText(str_1);
                    break;

                case 2:
                    String str_2 = getString(R.string.alarm_follow);
                    binding.alramitemTextviewComment.setText(str_2);
                    break;
            }

        }

        @Override
        public int getItemCount() {
            return alarmDTOs.size();
        }
    }
}
