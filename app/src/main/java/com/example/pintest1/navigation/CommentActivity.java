package com.example.pintest1.navigation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pintest1.R;
import com.example.pintest1.databinding.ActivityCommentBinding;
import com.example.pintest1.databinding.ItemCommentviewBinding;
import com.example.pintest1.model.ContentDTO;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Nullable;

import static com.example.pintest1.util.StatusCode.FRAGMENT_ARG;

public class CommentActivity extends AppCompatActivity {
    private ActivityCommentBinding binding;

    private String contentUid;

    private RecyclerView commentRecyclerView;

    private FirebaseUser user;
    private String destinationUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_comment);

        user = FirebaseAuth.getInstance().getCurrentUser();

        destinationUid = getIntent().getStringExtra("destinationUid");

        contentUid = getIntent().getStringExtra("contentUid");

        commentRecyclerView = (RecyclerView) findViewById(R.id.comment_recyclerview);
        commentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerView.setAdapter(new CommentRecyclerViewAdapter());


        binding.commentBtnSend.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ContentDTO.Comment comment = new ContentDTO.Comment();

                        comment.userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        comment.comment = binding.commentEditMessage.getText().toString();
                        comment.uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        comment.timestamp = new SimpleDateFormat("yyMMdd_hhmmss")
                                .format(new Date(System.currentTimeMillis()));

                        FirebaseFirestore.getInstance()
                                .collection("images")
                                .document(contentUid)
                                .collection("comment")
                                .document()
                                .set(comment);

                        binding.commentEditMessage.setText("");

                        // Comment 입력 시 commentAlarm을 호출
                    }
                }
        );


    }

    private class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private ArrayList<ContentDTO.Comment> comments;

        CommentRecyclerViewAdapter() {
            comments = new ArrayList<>();
            FirebaseFirestore.getInstance()
                    .collection("images")
                    .document(contentUid)
                    .collection("comment")
                    .orderBy("timestamp")
                    .addSnapshotListener(
                            new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                    comments.clear();
                                    if(queryDocumentSnapshots == null) return ;
                                    for(DocumentSnapshot document:queryDocumentSnapshots.getDocuments()){
                                        comments.add(document.toObject(ContentDTO.Comment.class));
                                    }
                                    notifyDataSetChanged();
                                }
                            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commentview,parent,false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
            final ItemCommentviewBinding ibinding = ((CustomViewHolder) holder).getBinding();
            final int finalPosition = position;
            //comment userID
            ibinding.commentviewitemTextviewProfile.setText(comments.get(position).userId);


            ibinding.commentviewitemImageviewProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    finish();
                }
            });
            //comment Profile image
            FirebaseFirestore.getInstance().collection("profileImages").document(comments.get(position).uid).addSnapshotListener(
                    new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            if(documentSnapshot == null) return;
                            if(documentSnapshot.getData() != null)
                            {
                                String url = documentSnapshot.getData().get(comments.get(finalPosition).uid).toString();
                                Glide.with(holder.itemView).load(url).apply(new RequestOptions().circleCrop()).into(ibinding.commentviewitemImageviewProfile);
                            }
                        }
                    }
            );

            //comment text
            ibinding.commentviewitemTextviewComment.setText(comments.get(position).comment);

        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {

            private ItemCommentviewBinding binding;

            public CustomViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }

            ItemCommentviewBinding getBinding(){
                return binding;
            }
        }
    }
}

