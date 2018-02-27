package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * Created by ross1 on 22/02/2018.
 */

public class LogInActivity extends AppCompatActivity {

    private Button logIn;
    private EditText username, password;
    private TextView forgottenRegister;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Firebase Auth
        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null){
            startActivity(new Intent(LogInActivity.this, CareHomeActivity.class));
            finish();
        }
        //Set view now
        setContentView(R.layout.log_in_layout);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        logIn = (Button) findViewById(R.id.confirmLogin);

        auth = FirebaseAuth.getInstance();
        forgottenRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SignUpActivity.class); startActivity(i);
            }
        });

    }
}
