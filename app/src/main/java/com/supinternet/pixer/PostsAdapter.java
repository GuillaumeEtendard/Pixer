package com.supinternet.pixer;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.MyViewHolder> {
    private List<Post> postsList;
    private Boolean isLike;
    private int likeCount;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    private DatabaseReference mDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView postImage;
        public TextView postContent, postAuthor, postLikeCount, postDate;
        public ImageButton likeButton;

        public MyViewHolder(View view) {
            super(view);
            postImage = (ImageView) view.findViewById(R.id.post_image);
            postContent = (TextView) view.findViewById(R.id.post_content);
            postAuthor = (TextView) view.findViewById(R.id.post_author);
            postLikeCount = (TextView) view.findViewById(R.id.like_counter);
            likeButton = (ImageButton) view.findViewById(R.id.like_button);
            postDate = (TextView) view.findViewById(R.id.post_date);
        }
    }


    public PostsAdapter(List<Post> moviesList) {
        this.postsList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Post post = postsList.get(position);
        holder.postContent.setText(post.content);

        //SimpleDateFormat sf = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
        //Date date = new Date(Long.parseLong(post.pubDate));
        holder.postDate.setText(post.pubDate);
        holder.postAuthor.setText(post.author);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        String url = "gs://pixer-65d1e.appspot.com/images/" + post.uid + "/" + post.imageName;
        StorageReference storageRef = storage.getReferenceFromUrl(url);

        Glide.with(holder.postImage.getContext())
                .using(new FirebaseImageLoader())
                .load(storageRef)
                .into(holder.postImage);

        isLike = post.likes.containsKey(getUid());


        if (isLike) {
            holder.likeButton.setImageResource(R.drawable.ic_favorite_black_24dp);
        } else {
            holder.likeButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
        }
        holder.postLikeCount.setText(String.valueOf(post.likeCounter));

        final MyViewHolder holderFinal = holder;
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View starView) {
                // Need to write to both places the post is stored
                DatabaseReference globalPostRef = mDatabase.child("posts").child(post.id);

                // Run two transactions
                globalPostRef.runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        Post p = mutableData.getValue(Post.class);
                        if (p == null) {
                            System.out.println("likes null" + mutableData);
                            return Transaction.success(mutableData);
                        }
                        System.out.println("likes post " + p);
                        if (p.likes.containsKey(getUid())) {
                            System.out.println("unlikes count : " + p.likeCounter);
                            // Unlike post
                            p.likeCounter = p.likeCounter - 1;
                            p.likes.remove(getUid());
                            isLike = false;
                        } else {
                            System.out.println("likes count : " + p.likeCounter);
                            // Like post
                            p.likeCounter = p.likeCounter + 1;
                            p.likes.put(getUid(), true);
                            isLike = true;
                        }
                        System.out.println("bool f : " + isLike);
                        likeCount = p.likeCounter;

                        mutableData.setValue(p);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        holderFinal.postLikeCount.setText(String.valueOf(likeCount));
                        System.out.println("like bool " + isLike);
                        if (isLike) {
                            holderFinal.likeButton.setImageResource(R.drawable.ic_favorite_black_24dp);
                        } else {
                            holderFinal.likeButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        }
                        System.out.println("like count " + likeCount);
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });
                System.out.println("likes clicked" + likeCount);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

}