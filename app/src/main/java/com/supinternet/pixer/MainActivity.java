package com.supinternet.pixer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button signInButton, signInFacebookButton, signInGoogleButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        signInButton = (Button) findViewById(R.id.signin_button);
        signInFacebookButton = (Button) findViewById(R.id.signin_facebook_button);
        signInGoogleButton = (Button) findViewById(R.id.signin_google_button);

    }

    public void onClick(View v) {

        if (v.getId() == R.id.signin_button) {
            Intent intent = new Intent(this, EmailPasswordActivity.class);
            startActivity(intent);

        } else if (v.getId() == R.id.signin_google_button) {
            Intent intent = new Intent(this, GoogleSignInActivity.class);
            startActivity(intent);
        }
    }
}