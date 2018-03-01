package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
    private ListView patientList;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference ref;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_home);

        signOut = (Button) findViewById(R.id.signOut);
        addPatient = (Button) findViewById(R.id.addPatient);
        hiMessage = (TextView) findViewById(R.id.hiMessage);
        patientList = (ListView) findViewById(R.id.patientList);

        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = auth.getCurrentUser();
        userId = user.getUid();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    CarerInfo carerInfo = new CarerInfo();
                    carerInfo.setName(ds.child(userId).getValue(CarerInfo.class).getName()); //set carer name
                    //Set text field for carers name
                    hiMessage.append("Hi " + carerInfo.getName() + "!");

/*                    if(ds.child(userId).child("Patients").exists()){
                        ArrayList<String> childArray = new ArrayList<>();
                        PatientInfo patientInfo = new PatientInfo();
                        patientInfo.setName(ds.child(userId).child("Patients").getChildren()
                        patientInfo.setName("test");
                        patientInfo.setAge("TEST");
                        patientInfo.setGender("MALE");
                        //patientInfo.setName(ds.child(userId).child("Patients").getValue(PatientInfo.class).getAge());
                        //patientInfo.setName(ds.child(userId).child("Patients").getValue(PatientInfo.class).getGender());

                        ArrayList<String> array = new ArrayList<>();
                        array.add(patientInfo.getName());
                        array.add(patientInfo.getGender());
                        array.add(patientInfo.getAge());
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, array);
                        patientList.setAdapter(adapter);
                    }*/
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



        addPatient.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddPatientActivity.class); startActivity(i);
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
        auth.signOut();
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
