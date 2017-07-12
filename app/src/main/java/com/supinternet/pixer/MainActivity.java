package com.supinternet.pixer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends BaseActivity {

    Button signInButton;
    public BottomNavigationView bottomNavigationView;
    TextView userConnected;
    private FirebaseAuth mAuth;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReferenceFromUrl("gs://pixer-65d1e.appspot.com/");
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    FrameLayout fragContainer;
    LinearLayout ll;

    private FragmentManager fm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        //signInButton = (Button) findViewById(R.id.signin_button);
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();


        fragContainer = (FrameLayout) findViewById(R.id.main_fragment_container);

        currentUser = mAuth.getCurrentUser();

        fm = getFragmentManager();

        if (currentUser != null) {
            if(currentUser.isEmailVerified()){
                changeFragment(0, "HOME_FRAGMENT");
            }
        }


        bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_home:
                                System.out.println("item " + item.getItemId());
                                changeFragment(0, "HOME_FRAGMENT");
                                break;
                            case R.id.action_profile:
                                System.out.println("item " + item.getItemId());
                                changeFragment(1, "PROFILE_FRAGMENT");
                                break;
                            case R.id.action_post:
                                System.out.println("item " + item.getItemId());
                                changeFragment(2, "NEW_POST_FRAGMENT");
                                break;
                        }
                        return true;
                    }
                }
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        System.out.println(mAuth);

        if (currentUser == null || !currentUser.isEmailVerified()) {
            Intent intent = new Intent(this, EmailPasswordActivity.class);
            startActivity(intent);
        }
    }

    private void changeFragment(int position, String tag) {

        Fragment newFragment = null;

        if (position == 0) {
            newFragment = new HomeFragment();
        } else if (position % 2 != 0) {
            newFragment = new ProfileFragment();
        } else {
            newFragment = new NewPostFragment();
        }

        System.out.println(newFragment);
        Fragment tagFragment = getFragmentManager().findFragmentByTag(tag);
        if(tagFragment == null) {
            getFragmentManager().beginTransaction().replace(
                    R.id.main_fragment_container, newFragment, tag)
                    .commit();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_button:
                mAuth.signOut();
                Intent intent = new Intent(this, EmailPasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }
}