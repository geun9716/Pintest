package com.example.pintest1.navigation;

import android.Manifest;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.example.pintest1.R;
import com.example.pintest1.databinding.ActivityAddPhotoBinding;
import com.example.pintest1.model.ContentDTO;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.pintest1.util.StatusCode.PICK_IMAGE_FROM_ALBUM;

public class AddPhotoActivity extends AppCompatActivity implements View.OnClickListener {

    // Data Binding
    private Uri contentUri;
    private ActivityAddPhotoBinding binding;
    private String photoUrl;

    // Firebase Storage, Database, Auth
    private FirebaseStorage firebaseStorage;
    //private FirebaseDatabase firebaseDatabase;
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_photo);

        //ImageView Button EditText 찾아오고 버튼 세팅하기
        binding.addphotoBtnUpload.setOnClickListener(this);

        //권한 요청 하는 부분
        ActivityCompat.requestPermissions
                        (this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        //앨범 오픈
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM);

        // Firebase storage
        firebaseStorage = FirebaseStorage.getInstance();

        // Firebase Database
        // firebaseDatabase = FirebaseDatabase.getInstance();
        db = FirebaseFirestore.getInstance();

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        binding.addphotoImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 앨범에서 사진 선택시 호출 되는 부분
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == RESULT_OK) {

            contentUri = data.getData();

            //이미지뷰에 이미지 세팅
            binding.addphotoImage.setImageURI(data.getData());
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.addphoto_btn_upload && contentUri != null) {

            binding.progressBar.setVisibility(View.VISIBLE);


            /*File file = new File(photoUrl);
            Uri contentUri = Uri.fromFile(file);*/

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG__"+timeStamp + "__.jpg";


            StorageReference storageRef =
                    firebaseStorage.getReferenceFromUrl("gs://pintest1-589d7.appspot.com").child("images").child(imageFileName);
            UploadTask uploadTask = storageRef.putFile(contentUri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            binding.progressBar.setVisibility(View.GONE);

                            Toast.makeText(AddPhotoActivity.this, getString(R.string.upload_success),
                                    Toast.LENGTH_SHORT).show();

                            //시간 생성
                            final Date date = new Date();
                            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            final ContentDTO contentDTO = new ContentDTO();


                            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //이미지 주소
                                    contentDTO.imageUrl = uri.toString();
                                    //유저의 UID
                                    contentDTO.uid = firebaseAuth.getCurrentUser().getUid();
                                    //게시물의 설명
                                    contentDTO.explain = binding.addphotoEditExplain.getText().toString();
                                    //유저의 아이디
                                    contentDTO.userId = firebaseAuth.getCurrentUser().getEmail();
                                    //게시물 업로드 시간
                                    contentDTO.timestamp = simpleDateFormat.format(date);

                                    //게시물을 데이터를 생성 및 엑티비티 종료
                                    db.collection("images").document().set(contentDTO);

                                    setResult(RESULT_OK);
                                }
                            });
                            //디비에 바인딩 할 위치 생성 및 컬렉션(테이블)에 데이터 집합 생성
                            //DatabaseReference images = firebaseDatabase.getReference().child("images").push();

                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            binding.progressBar.setVisibility(View.GONE);

                            Toast.makeText(AddPhotoActivity.this, getString(R.string.upload_fail),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
