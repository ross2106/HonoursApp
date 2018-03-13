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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditMedication extends AppCompatActivity {

    //Text Fields
    EditText editMedicationName, editDosageValue, editDosageTime;

    //Spinner
    Spinner editDosageType;
    ArrayAdapter<CharSequence> editSpinnerValues;
    String editDosageTypeValue;

    //Button
    Button editMedication;


    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    //ID of logged in carer, and patient for profile
    private String userId;
    private String patientId;
    private String medicationId;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        editMedicationName = (EditText) findViewById(R.id.editMedicationName);
        editDosageValue = (EditText) findViewById(R.id.editMedicationDosageValue);
        editDosageTime = (EditText) findViewById(R.id.editMedicationTime);
        editMedication = (Button) findViewById(R.id.medicationSubmit);

        //Dropdown
        editDosageType = (Spinner) findViewById(R.id.editDosageType);
        editSpinnerValues = ArrayAdapter.createFromResource(this, R.array.dosageType, android.R.layout.simple_spinner_item);
        editSpinnerValues.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editDosageType.setAdapter(editSpinnerValues);
        editDosageType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimary));
                editDosageTypeValue = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        dbRef = FirebaseDatabase.getInstance().getReference();

        //Get the current user
        final FirebaseUser user = mAuth.getCurrentUser();
        //Assign that users ID
        userId = user.getUid();
        //Get the ID of the patient from the Intent extra String
        patientId = getIntent().getStringExtra("patientID");
        medicationId = getIntent().getStringExtra("medicationID");

        /**
         * CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.addMedicationDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(EditMedication.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Edit Medication"); //Set the title of the page

        NavigationView navigationView = findViewById(R.id.addMedication_navigation_view); //Navigation view from layout file
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

        DatabaseReference medicationRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication").child(medicationId);
        medicationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editMedicationName.setText(dataSnapshot.child("name").getValue().toString());
                editDosageValue.setText(dataSnapshot.child("dosageValue").getValue().toString());
                editDosageTime.setText(dataSnapshot.child("time").getValue().toString());
                editDosageValue.setText(dataSnapshot.child("dosageType").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        editMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference patientDb = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
                String medicationNameString = editMedicationName.getText().toString();
                String dosageValueString = editDosageValue.getText().toString();
                String dosageValueTypeString = editDosageTypeValue;
                String medicationTimeString = editDosageTime.getText().toString();
                String medicationId = patientDb.push().getKey();
                Map newMedication = new HashMap();
                newMedication.put("name", medicationNameString);
                newMedication.put("dosageValue", dosageValueString);
                newMedication.put("dosageType", dosageValueTypeString);
                newMedication.put("time", medicationTimeString);
                patientDb.child(medicationId).setValue(newMedication);
                Toast.makeText(EditMedication.this, "Content Edited!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MedicationActivity.class);
                intent.putExtra("patientID", patientId);
                startActivity(intent);
            }
        });


        /**
         * Code to check a user is logged in.
         * If they are not logged in, return them to the login page.
         */
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
     * Code to sign out a user.
     */
    public void signOut() {
        mAuth.signOut();
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
     * When a user navigates back to this page, this code is called.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * On Start is called when the activity restarts.
     */
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    /**
     * When a user navigates away from this activity, this code is called.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }
}
