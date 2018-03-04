package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AddPatientActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference ref;
    private String carerId;
    private EditText patientName, patientAge, patientGender;
    private Button addPatient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        patientName = findViewById(R.id.addPatientName);
        patientAge = findViewById(R.id.addPatientAge);
        patientGender = findViewById(R.id.addPatientGender);
        addPatient = findViewById(R.id.addPatientButton);

        auth = FirebaseAuth.getInstance();
        final FirebaseUser carer = FirebaseAuth.getInstance().getCurrentUser();
        carerId = carer.getUid();

        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = patientName.getText().toString();
                String age = patientAge.getText().toString();
                String gender = patientGender.getText().toString();
                final DatabaseReference patient_db = FirebaseDatabase.getInstance().getReference().child("Users").child(carerId);
                String patientId = patient_db.push().getKey();
                Map newPatient = new HashMap();
                newPatient.put("id", patientId);
                newPatient.put("age", age);
                newPatient.put("gender", gender);
                newPatient.put("name", name);
                patient_db.child("Patients").child(patientId).setValue(newPatient);
                Intent intent = new Intent(getApplicationContext(), CareHomeActivity.class);
                startActivity(intent);
            }
        });

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        };


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(authListener != null){
            auth.removeAuthStateListener(authListener);
        }
    }
}
