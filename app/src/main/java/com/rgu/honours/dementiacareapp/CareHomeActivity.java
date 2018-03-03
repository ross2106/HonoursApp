package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class CareHomeActivity extends AppCompatActivity {

    private Button signOut, addPatient;
    private TextView hiMessage;
    private ListView patientListView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference dbRef, patientDbRef, carerDbRef;
    private String userId;

    ArrayList<String> patientArrayList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_home);

        signOut = (Button) findViewById(R.id.signOut);
        addPatient = (Button) findViewById(R.id.addPatient);
        hiMessage = (TextView) findViewById(R.id.hiMessage);
        patientListView = (ListView) findViewById(R.id.patientList);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();

        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        carerDbRef = dbRef.child("Users");
        carerDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CarerInfo carer = new CarerInfo(); //Create carer object
                carer.setName(dataSnapshot.child(userId).getValue(CarerInfo.class).getName()); //Set the carer object name
                String carerName = carer.getName(); //Get the name of the carer
                //Set text field for carers name
                hiMessage.setText("Hi " + carerName + "!"); //Produce the greeting
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        patientDbRef = dbRef.child("Users").child(userId).child("Patients");
        patientDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    PatientInfo patient = new PatientInfo();
                    patient.setName(ds.getValue(PatientInfo.class).getName());
                    patientArrayList.add(patient.getName());
                    arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, patientArrayList);
                    patientListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            String patientId = ds.getKey();
                            Intent intent = new Intent(CareHomeActivity.this, PatientProfile.class);
                            intent.putExtra("PATIENT_ID", patientId);
                            startActivity(intent);
                        }
                    });
                    patientListView.setAdapter(arrayAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

        addPatient.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddPatientActivity.class);
                startActivity(i);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

    }

    public void signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
