package com.rgu.honours.dementiacareapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * Created by ross1 on 22/02/2018.
 */

public class LogInActivity extends AppCompatActivity {
    DynamoDBMapper dynamoDBMapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_layout);

        AWSMobileClient.getInstance().initialize(this).execute();
        // Instantiate a AmazonDynamoDBMapperClient
/*        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                .build();*/
    }
}
