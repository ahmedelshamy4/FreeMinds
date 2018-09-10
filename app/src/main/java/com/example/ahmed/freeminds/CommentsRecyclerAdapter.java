package com.example.ahmed.freeminds;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ahmed.freeminds.model.Comments;
import com.example.ahmed.freeminds.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Comment;

import java.util.List;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.myViewHolder> {
    Context context;
    List<Comments> comments;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    public CommentsRecyclerAdapter(Context context, List<Comments> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final myViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        if (mAuth != null) {
            firebaseFirestore.collection(Constants.USER_COLLECTION).document(comments.get(position).getUser_id()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                holder.setUserDescription(task.getResult().getString("name"), task.getResult().getString("image"));
                            } else {
                                Toast.makeText(context, "ERROR" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        String commentMessage = comments.get(position).getMessage();
        holder.setComment_message(commentMessage);
    }

    @Override
    public int getItemCount() {
        if (comments != null) {

            return comments.size();

        } else {

            return 0;

        }
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageView comment_image;
        TextView comment_username;
        TextView comment_message;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setComment_message(String message) {

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setUserDescription(String userNameString, String userImageString) {
            comment_username = mView.findViewById(R.id.comment_username);
            comment_image = mView.findViewById(R.id.comment_image);

            comment_username.setText(userNameString);
            Glide.with(context).load(userImageString).into(comment_image);
        }
    }
}
