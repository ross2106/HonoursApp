package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
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

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference dbRef;
    private String userId;
    private EditText patientName, patientAge, patientGender;
    private Button addPatient;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        patientName = (EditText) findViewById(R.id.addPatientName);
        patientAge = (EditText) findViewById(R.id.addPatientAge);
        patientGender = (EditText) findViewById(R.id.addPatientGender);
        addPatient = (Button) findViewById(R.id.addPatientButton);

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        dbRef = FirebaseDatabase.getInstance().getReference();

        //Get the current user
        final FirebaseUser user = mAuth.getCurrentUser();
        //Assign that users ID
        userId = user.getUid();

        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = patientName.getText().toString();
                String age = patientAge.getText().toString();
                String gender = patientGender.getText().toString();
                final DatabaseReference patient_db = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
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

        /**
         * CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.addPatientDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(AddPatientActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("This Is Me"); //Set the title of the page

        NavigationView navigationView = findViewById(R.id.add_patient_navigation_view); //Navigation view from layout file
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() { //Setting on click listeners for menu items
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.care_home:
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), CareHomeActivity.class));
                        break;
                    case R.id.log_out:
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        signOut();
                        break;
                }
                return true;
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

    /**
     * Code for the Navigation drawer "hamburger". Opens the drawer.
     *
     * @param item The Item in the menu
     * @return Returns the selected Items
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Code to sign out a user.
     */
    public void signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop(){
        super.onStop();
        if(authListener != null){
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
