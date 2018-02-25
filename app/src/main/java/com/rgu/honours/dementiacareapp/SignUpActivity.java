package com.rgu.honours.dementiacareapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class SignUpActivity extends AppCompatActivity {

    DynamoDBMapper dynamoDBMapper;
    private Button signUp;
    private EditText email, confirmEmail, password, confirmPassword;
    private String emailString, confirmEmailString, passwordString, confirmPasswordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        AWSMobileClient.getInstance().initialize(this).execute();

        signUp = (Button) findViewById(R.id.confirmSignUp);
        email = (EditText) findViewById(R.id.signUpUsername);
        confirmEmail = (EditText) findViewById(R.id.signUpConfirmUsername);
        password = (EditText) findViewById(R.id.signUpPassword);
        confirmPassword = (EditText) findViewById(R.id.signUpConfirmPassword);

        emailString = email.getText().toString();
        confirmEmailString = confirmEmail.getText().toString();
        passwordString = password.getText().toString();
        confirmPasswordString = confirmPassword.getText().toString();

        AWSMobileClient.getInstance().initialize(this).execute();
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final UsersDO userItem = new UsersDO();

                userItem.setUserId("ross");
                userItem.setEmail(confirmEmailString);
                userItem.setPassword(confirmPasswordString);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        dynamoDBMapper.save(userItem);
                        //item saved

                    }
                }).start();

                Intent i;
                switch(view.getId()){
                    case R.id.signUp : i = new Intent(getApplicationContext(), LogInActivity.class); startActivity(i); break;
                    default: break;
                }
            }
        });
    }


}