package com.supinternet.pixer;


import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    FrameLayout fragContainer;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FragmentManager fm;
    LinearLayout ll;
    private DatabaseReference mDatabase;

    private List<Post> postsList = new ArrayList<>();
    private RecyclerView recyclerView;
    private PostsAdapter mAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        fragContainer = (FrameLayout) v.findViewById(R.id.main_fragment_container);
        recyclerView = (RecyclerView) v.findViewById(R.id.my_posts_recycler_view);

        TextView userConnected = (TextView) v.findViewById(R.id.fragment_user_email);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();
        fm = getFragmentManager();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAdapter = new PostsAdapter(postsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);

        getPosts();
        mAdapter.notifyDataSetChanged();

        userConnected.setText(currentUser.getEmail());

        return v;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void getPosts() {
        Query postsQuery = mDatabase.child("posts")
                .orderByChild("uid")
                .equalTo(currentUser.getUid());
        postsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Post newPost = dataSnapshot.getValue(Post.class);

                if (newPost != null) {
                    postsList.remove(newPost);
                }
                mAdapter.notifyDataSetChanged();
            }


            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Post newPost = dataSnapshot.getValue(Post.class);

                if (newPost != null) {
                    postsList.add(0, newPost);
                }

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
