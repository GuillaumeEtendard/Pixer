package com.supinternet.pixer;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Post {

    public String id;
    public String uid;
    public String author;
    public String content;
    public String imageName;
    public String pubDate;
    public User user;
    public int likeCounter = 0;
    public Map<String, Boolean> likes = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String id, String uid, String author, String content, String imageName, User user) {
        this.id = id;
        this.uid = uid;
        this.author = author;
        this.content = content;
        this.imageName = imageName;
        this.user = user;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("uid", uid);
        result.put("author", author);
        result.put("content", content);
        result.put("imageName", imageName);
        result.put("likeCounter", likeCounter);
        result.put("likes", likes);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);
        System.out.println(new Date());
        String newstring = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        result.put("pubDate", newstring);

        return result;
    }

}
