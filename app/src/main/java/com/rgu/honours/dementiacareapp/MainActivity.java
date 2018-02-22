package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button welcomeLogIn, welcomeSignUp;

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
    }

    @Override
    public void onClick(View view) {
        Intent i;

        switch(view.getId()){
            case R.id.logIn : i = new Intent(this, LogInActivity.class); startActivity(i); break;
            default: break;

        }



    }
}
