package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button welcomeLogIn, welcomeSignUp;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //defining buttons
        welcomeLogIn = (Button) findViewById(R.id.logIn);
        welcomeSignUp = (Button) findViewById(R.id.signUp);

        //Add click listener to button
        welcomeLogIn.setOnClickListener(this);
        welcomeSignUp.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart(){
        super.onStart();
        //Check if user is signed in (non-null) and update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onClick(View view) {
        Intent i;

        switch(view.getId()){
            case R.id.logIn : i = new Intent(this, LogInActivity.class); startActivity(i); break;
            case R.id.signUp : i = new Intent(this, SignUpActivity.class); startActivity(i); break;
            default: break;
        }
    }
}
