package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;


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
    public void onClick(View view) {
        Intent i;
        switch(view.getId()){
            case R.id.logIn : i = new Intent(this, LogInActivity.class); startActivity(i); break;
            case R.id.signUp : i = new Intent(this, SignUpActivity.class); startActivity(i); break;
            default: break;
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        //mAuth.signOut();
        if(mAuth.getCurrentUser() != null){
           finish();
            startActivity(new Intent(this, CareHomeActivity.class));
        }
    }
}
