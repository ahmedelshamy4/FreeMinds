package com.example.ahmed.freeminds;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ahmed.freeminds.model.BlogPost;
import com.example.ahmed.freeminds.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.koushikdutta.ion.Ion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TimeZone;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.myViewHolder> {
    Date timestamp;

    Context context;
    List<BlogPost> blogPosts;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    public BlogRecyclerAdapter(Context context, List<BlogPost> blogPosts) {
        this.context = context;
        this.blogPosts = blogPosts;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_row, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final myViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        final String blog_post_id = blogPosts.get(position).blogPostId;
        final String currentUserId = mAuth.getCurrentUser().getUid();

        timestamp = blogPosts.get(position).getTimestamp();
        String des_data = blogPosts.get(position).getDesc();
        String blog_image_thumb = blogPosts.get(position).getImage_thumb();
        final String blog_image = blogPosts.get(position).getImage_url();
        String user_id = blogPosts.get(position).getUser_id();

        firebaseFirestore.collection(Constants.USER_COLLECTION).document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    holder.setUserDescription(task.getResult().getString(Constants.USER_NAME), task.getResult().getString(Constants.USER_IMAGE));
                } else {
                    Toast.makeText(context, "Image Upload error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
        if (timestamp != null) {
            holder.setTimeStamp();
        }
        holder.setDescText(des_data);
        holder.setBlogImagePost(blog_image, blog_image_thumb);

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Likes).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            int count = documentSnapshots.size();
                            holder.updateLikeCounts(count);
                        } else {
                            holder.updateLikeCounts(0);
                        }
                    }
                }
            });
            firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Comment).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (documentSnapshots != null) {
                        if (!documentSnapshots.isEmpty()) {
                            int count = documentSnapshots.size();
                            holder.updateCommentCounts(count);
                        } else {
                            holder.updateCommentCounts(0);
                        }
                    }
                }
            });
        }

        if (mAuth.getCurrentUser() != null) {
            firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Likes).document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                    if (documentSnapshot != null) {
                        if (documentSnapshot.exists()) {
                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like_accent));
                        } else {
                            holder.blogLikeBtn.setImageDrawable(context.getDrawable(R.mipmap.action_like__gray));
                        }
                    }
                }
            });
        }
        holder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Likes).document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());
                                firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Likes).document(currentUserId).set(likesMap);
                            } else {
                                firebaseFirestore.collection(Constants.USER_Posts + blog_post_id + Constants.USER_Likes).document(currentUserId).delete();

                            }
                        } else {
                            Toast.makeText(context, "ERROR" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
        holder.blogCommentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra(Constants.COMMENTS_ACTIVITY_key, blog_post_id);
                context.startActivity(commentIntent);
            }
        });
        holder.blogCommentCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra(Constants.COMMENTS_ACTIVITY_key, blog_post_id);
                context.startActivity(commentIntent);
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, blog_post_id, Toast.LENGTH_SHORT).show();
            }
        });
    }//end of bindView

    @Override
    public int getItemCount() {
        return blogPosts.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {
        View mView;

        private ImageView userImage;
        private TextView userName;
        private TextView userDate;
        private ImageView blog_post_image;
        private TextView userDesc;
        private ImageView blogLikeBtn;
        private TextView blogLikeCount;
        private TextView blogCommentCount;
        private ImageView blogCommentIcon;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            blogCommentCount = mView.findViewById(R.id.blog_comment_count);
            blogCommentIcon = mView.findViewById(R.id.blog_comment_icon);
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
        }

        public void setDescText(String desc) {
            userDesc = mView.findViewById(R.id.blog_desc);
            userDesc.setText(desc);
        }

        public void setUserDescription(String userNameString, String userImageString) {
            userName = mView.findViewById(R.id.blog_user_name);
            userName.setText(userNameString);
            userImage = mView.findViewById(R.id.blog_user_image);
            Glide.with(context).load(userImageString).into(userImage);
        }

        public void setBlogImagePost(String blog_image, String blog_image_thumb) {
            blog_post_image = mView.findViewById(R.id.blog_image);
            scaleImage(blog_post_image);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.placeholder);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(blog_image)
                    .thumbnail(Glide.with(context).load(blog_image_thumb))
                    .into(blog_post_image);

        }

        public void setTimeStamp() {
            userDate = mView.findViewById(R.id.blog_date);
            SimpleDateFormat date = new SimpleDateFormat("d LLLL yyyy", Locale.getDefault());
            date.setTimeZone(TimeZone.getDefault());

            if (timestamp != null) {

                String date_time = date.format(timestamp.getTime());
                userDate.setText(date_time + " (" + getTimeAgo(timestamp, context) + ") ");

            }
        }

        public void updateLikeCounts(int count) {
            blogLikeCount = mView.findViewById(R.id.blog_like_count);
            if (count == 0) {
                blogLikeCount.setText(" " + count + "Likes");
            } else if (count == 1) {
                blogLikeCount.setText(" " + count + "Like");
            } else {
                blogLikeCount.setText(" " + count + "Likes");

            }
        }

        public void updateCommentCounts(int count) {
            if (count == 0) {
                blogCommentCount.setText(" " + count + "Comments");
            } else if (count == 1) {
                blogCommentCount.setText(" " + count + "Comment");

            } else {
                blogCommentCount.setText(" " + count + "Comments");
            }
        }
    }

    private void scaleImage(ImageView view) throws NoSuchElementException {
        // Get bitmap from the the ImageView.
        Bitmap bitmap = null;

        try {
            Drawable drawing = view.getDrawable();
            bitmap = ((BitmapDrawable) drawing).getBitmap();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("No drawable on given view");
        } catch (ClassCastException e) {
            // Check bitmap is Ion drawable
            bitmap = Ion.with(view).getBitmap();
        }

        // Get current dimensions AND the desired bounding box
        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(350);
        /*Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));*/

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
       /* Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));*/

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
       /* Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));*/

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        /*Log.i("Test", "done");*/
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    public static String getTimeAgo(Date date, Context ctx) {

        if (date == null) {
            return null;
        }

        long time = date.getTime();

        Date curDate = currentDate();
        long now = curDate.getTime();
        if (time > now || time <= 0) {
            return null;
        }

        int dim = getTimeDistanceInMinutes(time);

        String timeAgo = null;

        if (dim == 0) {
            timeAgo = ctx.getResources().getString(R.string.date_util_term_less) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_minute);
        } else if (dim == 1) {
            return "1 " + ctx.getResources().getString(R.string.date_util_unit_minute);
        } else if (dim >= 2 && dim <= 44) {
            timeAgo = dim + " " + ctx.getResources().getString(R.string.date_util_unit_minutes);
        } else if (dim >= 45 && dim <= 89) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_an) + " " + ctx.getResources().getString(R.string.date_util_unit_hour);
        } else if (dim >= 90 && dim <= 1439) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 60)) + " " + ctx.getResources().getString(R.string.date_util_unit_hours);
        } else if (dim >= 1440 && dim <= 2519) {
            timeAgo = "1 " + ctx.getResources().getString(R.string.date_util_unit_day);
        } else if (dim >= 2520 && dim <= 43199) {
            timeAgo = (Math.round(dim / 1440)) + " " + ctx.getResources().getString(R.string.date_util_unit_days);
        } else if (dim >= 43200 && dim <= 86399) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_month);
        } else if (dim >= 86400 && dim <= 525599) {
            timeAgo = (Math.round(dim / 43200)) + " " + ctx.getResources().getString(R.string.date_util_unit_months);
        } else if (dim >= 525600 && dim <= 655199) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_year);
        } else if (dim >= 655200 && dim <= 914399) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_over) + " " + ctx.getResources().getString(R.string.date_util_term_a) + " " + ctx.getResources().getString(R.string.date_util_unit_year);
        } else if (dim >= 914400 && dim <= 1051199) {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_almost) + " 2 " + ctx.getResources().getString(R.string.date_util_unit_years);
        } else {
            timeAgo = ctx.getResources().getString(R.string.date_util_prefix_about) + " " + (Math.round(dim / 525600)) + " " + ctx.getResources().getString(R.string.date_util_unit_years);
        }

        return timeAgo + " " + ctx.getResources().getString(R.string.date_util_suffix);
    }

    private static int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);

    }
}
