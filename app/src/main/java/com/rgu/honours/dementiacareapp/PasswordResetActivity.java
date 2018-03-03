package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button btnReset, btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        emailInput = (EditText) findViewById(R.id.resetEmail);
        btnBack = (Button) findViewById(R.id.backButton);
        btnReset = (Button) findViewById(R.id.resetPasswordButton);

        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString();
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(getApplication(), "Enter your registered email", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(PasswordResetActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else{
                                    Toast.makeText(PasswordResetActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
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
