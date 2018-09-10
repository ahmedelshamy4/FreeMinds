package com.example.ahmed.freeminds;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ahmed.freeminds.authenticate.ChangePasswordActivity;
import com.example.ahmed.freeminds.authenticate.LoginActivity;
import com.example.ahmed.freeminds.model.BlogPost;
import com.example.ahmed.freeminds.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    BlogRecyclerAdapter adapter;
    RecyclerView recyclerView;
    List<BlogPost> blogPosts;
    DocumentSnapshot lastVisible;
    private boolean isFirstPageFirstLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        blogPosts = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerList);
        adapter = new BlogRecyclerAdapter(this, blogPosts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (mAuth.getCurrentUser() != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {
                        loadMorePosts();
                    }
                }
            });

            Query firstQuery = firebaseFirestore.collection(Constants.Query_Posts)
                    .orderBy(Constants.Time_Stamp, Query.Direction.DESCENDING)
                    .limit(5);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        if (isFirstPageFirstLoad) {
                            lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                            blogPosts.clear();
                        }
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostID = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostID);
                                if (isFirstPageFirstLoad) {
                                    blogPosts.add(blogPost);
                                } else {
                                    blogPosts.add(0, blogPost);
                                }
                                adapter.notifyDataSetChanged();


                            }
                        }

                        isFirstPageFirstLoad = false;
                    }
                }
            });

        }
    }

    private void loadMorePosts() {
        if (mAuth.getCurrentUser() != null) {
            Query nextQuery = firebaseFirestore.collection(Constants.Query_Posts)
                    .orderBy(Constants.Time_Stamp, Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(5);
            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {
                        lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostID = doc.getDocument().getId();
                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostID);
                                blogPosts.add(blogPost);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout: {
                LogOut();
                return true;
            }
            case R.id.action_chnage_pass: {
                startActivity(new Intent(this, ChangePasswordActivity.class));
                return true;
            }
            case R.id.action_settings: {
                startActivity(new Intent(this, SetupActivity.class));
                return true;
            }
            case R.id.action_add: {
                startActivity(new Intent(this, PostActivity.class));
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }

    }


    private void LogOut() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure ?")
                .setIcon(R.mipmap.ic_logo)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        finish();
                    }
                }).show();
    }
}

