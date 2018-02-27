package com.rgu.honours.dementiacareapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    //UI Elements
    private Button signUpButton;
    private EditText email, confirmEmail, password, confirmPassword;
    private String emailString, confirmEmailString, passwordString, confirmPasswordString;

    private FirebaseAuth auth;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Start Auth
        auth = FirebaseAuth.getInstance();
        //End Auth

        //Button
        signUpButton = findViewById(R.id.confirmSignUp);

        //Form fields
        //email = findViewById(R.id.signUpUsername);
        confirmEmail = (EditText) findViewById(R.id.signUpConfirmUsername);
        //password = findViewById(R.id.signUpPassword);
        confirmPassword = (EditText) findViewById(R.id.signUpConfirmPassword);

        //Form text
        //emailString = email.getText().toString();
        //confirmEmailString = confirmEmail.getText().toString();
        //passwordString = password.getText().toString();
        //confirmPasswordString = confirmPassword.getText().toString();


        //Sign up on click listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = confirmEmail.getText().toString();
                String password = confirmPassword.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //Sign in success
                                    Log.d(TAG, "createUserWithEmail: success");
                                    Toast.makeText(SignUpActivity.this, "Sign up successful!",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), CareHomeActivity.class);
                                    startActivity(intent);
                                } else {
                                    //if sign in fails, display a message to user
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}