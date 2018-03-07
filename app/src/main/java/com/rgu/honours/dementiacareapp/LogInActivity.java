package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;


/**
 * Created by ross1 on 22/02/2018.
 */

public class LogInActivity extends AppCompatActivity {

    private Button logIn, forgottenRegister, passwordReset;
    private EditText username, password;
    private TextInputLayout username_layout, password_layout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Get Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            startActivity(new Intent(LogInActivity.this, CareHomeActivity.class));
            finish();
        }
        //Set view now
        setContentView(R.layout.log_in_layout);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        username_layout = (TextInputLayout) findViewById(R.id.input_layout_username);
        password_layout = (TextInputLayout) findViewById(R.id.password_input_layout);

        logIn = (Button) findViewById(R.id.confirmLogin);
        forgottenRegister = (Button) findViewById(R.id.forgottenSignUp);
        passwordReset = (Button) findViewById(R.id.resetPassword);

        mAuth = FirebaseAuth.getInstance();
        forgottenRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SignUpActivity.class); startActivity(i);
            }
        });

        passwordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), PasswordResetActivity.class); startActivity(i);
            }
        });

        logIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String email = username.getText().toString();
                final String pass = password.getText().toString();

                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplicationContext(), "Enter email!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(pass)){
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                //authenticate user
                mAuth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    if(password.length() < 6){
                                        password.setError(getString(R.string.minimum_password));
                                    } else{
                                        Toast.makeText(LogInActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                } else{
                                    Toast.makeText(LogInActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LogInActivity.this, CareHomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });

    }

    @Override
    public void onStart(){
        super.onStart();
        if(mAuth.getCurrentUser() != null){
            finish();
            startActivity(new Intent(this, CareHomeActivity.class));
        }
    }
}
