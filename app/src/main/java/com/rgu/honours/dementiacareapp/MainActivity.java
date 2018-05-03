package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.UserAccess.LogInActivity;
import com.rgu.honours.dementiacareapp.UserAccess.SignUpActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set page title
        getSupportActionBar().setTitle("Mobile Care");

        //defining buttons
        Button welcomeLogIn = findViewById(R.id.logIn);
        Button welcomeSignUp = findViewById(R.id.signUp);

        //Add click listener to button
        welcomeLogIn.setOnClickListener(this);
        welcomeSignUp.setOnClickListener(this);

        //Authentication instances
        mAuth = FirebaseAuth.getInstance();
    }

    //Button click listeners
    @Override
    public void onClick(View view) {
        Intent i;
        switch (view.getId()) {
            case R.id.logIn: //Go to the login page
                i = new Intent(this, LogInActivity.class);
                startActivity(i);
                break;
            case R.id.signUp: //Go to the sign up page
                i = new Intent(this, SignUpActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if a user is logged in on start
        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, CareHomeActivity.class));
        }
    }
}
