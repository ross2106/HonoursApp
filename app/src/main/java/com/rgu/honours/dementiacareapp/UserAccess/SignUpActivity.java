package com.rgu.honours.dementiacareapp.UserAccess;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.R;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailText, passwordText, name;

    private FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Start Auth
        mAuth = FirebaseAuth.getInstance();
        //End Auth

        //Button
        Button signUpButton = findViewById(R.id.confirmSignUp);

        //Form fields
        emailText = findViewById(R.id.signUpUsername);
        passwordText = findViewById(R.id.signUpPassword);
        name = findViewById(R.id.name);

        //Sign up on click listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(SignUpActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setMessage("Registering....");
                final String email = emailText.getText().toString();
                String password = passwordText.getText().toString();
                final String nameText = name.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    emailText.setError("Required!");
                    progressDialog.dismiss();
                    return;
                }
                if(!isValidEmail(email)){
                    emailText.setError("Not a valid email format!");
                    progressDialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    passwordText.setError("Required!");
                    progressDialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(nameText)) {
                    name.setError("Required!");
                    progressDialog.dismiss();
                    return;
                }
                if (password.length() < 6) {
                    passwordText.setError("Password too short, enter minimum 6 characters!");
                    progressDialog.dismiss();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressDialog.dismiss();
                                    //Sign in success
                                    Log.d(TAG, "createUserWithEmail: success");
                                    Toast.makeText(SignUpActivity.this, "Sign up successful! You have now logged in.",
                                            Toast.LENGTH_SHORT).show();
                                    String user_id = mAuth.getCurrentUser().getUid();
                                    DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                                    Map newUser = new HashMap();
                                    newUser.put("email", email);
                                    newUser.put("name", nameText);
                                    current_user_db.setValue(newUser);
                                    Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                                    startActivity(intent);
                                } else if (!task.isSuccessful()) {
                                    progressDialog.dismiss();
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



    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                startActivity(new Intent(this, MainActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        emailText.setText("");
        passwordText.setText("");
        name.setText("");
        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, CareHomeActivity.class));
        }
    }

}