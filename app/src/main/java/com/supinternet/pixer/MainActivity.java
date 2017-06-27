package com.supinternet.pixer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    Button signInButton;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = (Button) findViewById(R.id.signin_button);
        mAuth = FirebaseAuth.getInstance();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        System.out.println(mAuth);

        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                Intent intent = new Intent(this, PostsActivity.class);
                startActivity(intent);
            }
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.signin_button) {
            Intent intent = new Intent(this, EmailPasswordActivity.class);
            startActivity(intent);
        }
    }
}