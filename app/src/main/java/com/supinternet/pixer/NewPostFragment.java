package com.supinternet.pixer;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewPostFragment extends Fragment {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mTextContent;
    private Button mSubmitButton;

    Button chooseImg;
    ImageView imgView;
    int PICK_IMAGE_REQUEST = 111;
    Uri filePath;
    ProgressDialog progressDialog;
    Boolean uploadSuccess = false;

    //creating reference to firebase storage
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pixer-65d1e.appspot.com/");

    private FirebaseUser currentUser;

    public NewPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_new_post, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mTextContent = (EditText) v.findViewById(R.id.title_post_field);
        mSubmitButton = (Button) v.findViewById(R.id.submit_post_button);


        chooseImg = (Button) v.findViewById(R.id.chooseImg);
        imgView = (ImageView) v.findViewById(R.id.imgView);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading....");

        chooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitPost();
            }
        });

        return v;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);

                //Setting image to ImageView
                imgView.setImageBitmap(bitmap);

                uploadImage(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void submitPost() {
        final String content = mTextContent.getText().toString();

        if (filePath == null) {
            chooseImg.setError("Required");
            return;
        }
        // Title & filePath are required
        if (TextUtils.isEmpty(content)) {
            mTextContent.setError("Required");
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(getContext(), "Posting...", Toast.LENGTH_SHORT).show();

        currentUser = mAuth.getCurrentUser();
        final String userId = currentUser.getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user == null) {
                            // User is null, error out
                            Toast.makeText(getContext(),
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Upload image

                            uploadSuccess = true;

                            System.out.println("upload success" + uploadSuccess);

                            // Write new post
                            User newUser = new User(user.getUid(), user.getEmail());
                            if (uploadSuccess) {
                                System.out.println("segment" + filePath.getLastPathSegment());
                                writeNewPost(userId, user.getEmail(), content, filePath.getLastPathSegment(), newUser);
                            } else {
                                Toast.makeText(getContext(),
                                        "Uploading error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        Fragment newFragment = new HomeFragment();

                        getFragmentManager().beginTransaction().replace(
                                R.id.main_fragment_container, newFragment)
                                .commit();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        setEditingEnabled(true);
                    }
                });
    }

    private void setEditingEnabled(boolean enabled) {
        mTextContent.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    private void writeNewPost(String userId, String username, String content, String imageName, User user) {
        // Create new post at /user-posts/$userid/$postid
        String key = mDatabase.child("posts").push().getKey();
        String id = key;
        Post post = new Post(id, userId, username, content, imageName, user);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/posts/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }

    private Boolean uploadImage(Uri fileUri) {
        progressDialog.show();

        final StorageReference photoRef = storageRef.child("images")
                .child(mAuth.getCurrentUser().getUid())
                .child(fileUri.getLastPathSegment());

        uploadSuccess = true;
        // Upload file to Firebase Storage
        photoRef.putFile(fileUri)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        uploadSuccess = false;
                        progressDialog.dismiss();
                    }
                });
        return uploadSuccess;
    }
}
