package com.example.ahmed.freeminds;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ahmed.freeminds.model.Comments;
import com.example.ahmed.freeminds.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {
    ImageView userImage, imageSend;
    EditText editTextInput;
    RecyclerView recyclerView;
    CommentsRecyclerAdapter adapter;
    List<Comments> commentsList;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private String blog_post_id, current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        blog_post_id = getIntent().getStringExtra(Constants.COMMENTS_ACTIVITY_key);
//load more comments
        firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Comment).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (documentSnapshots != null) {
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String commentId = doc.getDocument().getId();
                            Comments comments = doc.getDocument().toObject(Comments.class);
                            commentsList.add(comments);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        //load image user...
        firebaseFirestore.collection(Constants.USER_COLLECTION).document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Glide.with(CommentsActivity.this)
                            .load(task.getResult().getString(Constants.USER_IMAGE))
                            .into(userImage);
                } else {
                    Toast.makeText(CommentsActivity.this, "ERROR" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });///

        initViews();
        onClicks();
    }

    private void onClicks() {
        imageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment_text = editTextInput.getText().toString();
                if (!TextUtils.isEmpty(comment_text)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(Constants.USER_Comment_Message, comment_text);
                    map.put(Constants.USER_Comment_User_id, current_user_id);
                    map.put(Constants.USER_Comment_Timestamp, FieldValue.serverTimestamp());

                    firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Comment).add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                editTextInput.setText("");
                                Toast.makeText(CommentsActivity.this, "Comment Successfully posted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CommentsActivity.this, "Error Posting Comment :", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(CommentsActivity.this, "Error Posting Comment :", Toast.LENGTH_SHORT).show();

                        }
                    });
                } else {
                    Toast.makeText(CommentsActivity.this, "Please Enter a comment", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void initViews() {
        userImage = findViewById(R.id.comment_current_user);
        editTextInput = findViewById(R.id.comment_field);
        imageSend = findViewById(R.id.comment_post_btn);
        recyclerView = findViewById(R.id.comment_list);
        commentsList = new ArrayList<>();
        adapter = new CommentsRecyclerAdapter(CommentsActivity.this, commentsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }


}
