package com.rgu.honours.dementiacareapp.Family;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;

import java.util.HashMap;
import java.util.Map;

public class AddFamily extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private String userId;
    private String patientId;
    private String patientName;
    private EditText familyName, familyContactNo, familyRelation;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_family);

        familyName = findViewById(R.id.addFamilyName);
        familyContactNo = findViewById(R.id.addFamilyNumber);
        familyRelation = findViewById(R.id.addFamilyRelation);
        Button addFamily = findViewById(R.id.addFamilyButton);

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        //Get the current user
        final FirebaseUser user = mAuth.getCurrentUser();
        //Assign that users ID
        userId = user.getUid();

        patientId = getIntent().getStringExtra("patientID");
        patientName = getIntent().getStringExtra("patientName");

        addFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = familyName.getText().toString();
                String contactNo = familyContactNo.getText().toString();
                String relation = familyRelation.getText().toString();
                final DatabaseReference patient_db = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Family");
                String id = patient_db.push().getKey();
                Map newFamily = new HashMap();
                newFamily.put("id", id);
                newFamily.put("name", name);
                newFamily.put("relation", relation);
                newFamily.put("contactNo", contactNo);
                patient_db.child(id).setValue(newFamily);
                Intent intent = new Intent(getApplicationContext(), FamilyActivity.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("patientName", patientName);
                startActivity(intent);
            }
        });

        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.addFamilyDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(AddFamily.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Add Family Member"); //Set the title of the page

        NavigationView navigationView = findViewById(R.id.add_family_navigation_view); //Navigation view from layout file
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() { //Setting on click listeners for menu items
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.care_home:
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        startActivity(new Intent(getApplicationContext(), CareHomeActivity.class));
                        break;
                    case R.id.patient_profile:
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        Intent intent = new Intent(getApplicationContext(), PatientProfile.class);
                        intent.putExtra("patientID", patientId);
                        intent.putExtra("patientName", patientName);
                        startActivity(intent);
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
        return mToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Code to sign out a user.
     */
    private void signOut() {
        mAuth.signOut();
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
