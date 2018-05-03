package com.rgu.honours.dementiacareapp.Medication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

public class AddMedicationActivity extends AppCompatActivity {

    //Text Fields
    private EditText medicationName;
    private EditText dosageType;

    //Checkboxes
    CheckBox morningCheck, afternoonCheck, eveningCheck, bedCheck, asRequiredCheck;

    //Button
    private Button addMedication;
    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    //ID of logged in carer, and patient for profile
    private String userId;
    private String patientId;
    private String patientName;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

        medicationName = findViewById(R.id.medicationName);
        dosageType = findViewById(R.id.dosageType);
        morningCheck = findViewById(R.id.morningCheck);
        afternoonCheck = findViewById(R.id.afternoonCheck);
        eveningCheck = findViewById(R.id.eveningCheck);
        bedCheck = findViewById(R.id.bedtimeCheck);
        asRequiredCheck = findViewById(R.id.asRequiredCheck);

        addMedication = findViewById(R.id.medicationSubmit);


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
        patientName = getIntent().getStringExtra("patientName");

        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.addMedicationDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(AddMedicationActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Add Medication"); //Set the title of the page

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


        addMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String medicationNameString = medicationName.getText().toString();
                String medicationDosageTypeString = dosageType.getText().toString();
                Boolean morningChecked = morningCheck.isChecked();
                Boolean afternoonChecked = afternoonCheck.isChecked();
                Boolean eveningChecked = eveningCheck.isChecked();
                Boolean bedChecked = bedCheck.isChecked();
                Boolean asRequiredChecked = asRequiredCheck.isChecked();
                if (TextUtils.isEmpty(medicationNameString)) {
                    medicationName.setError("Required!");
                    return;
                }
                if (TextUtils.isEmpty(medicationDosageTypeString)) {
                    dosageType.setError("Required!");
                    return;
                }
                if (!morningChecked && !afternoonChecked && !eveningChecked && !bedChecked && !asRequiredChecked) {
                    Toast toast = Toast.makeText(AddMedicationActivity.this, "Please select a medication frequency!", Toast.LENGTH_SHORT);
                    toast.getView().setBackgroundResource(R.color.red);
                    toast.show();
                    return;
                }
                DatabaseReference medRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
                String medicationId = medRef.push().getKey();
                Map newMedication = new HashMap();
                newMedication.put("id", medicationId);
                newMedication.put("name", medicationNameString);
                newMedication.put("dosageType", medicationDosageTypeString);
                newMedication.put("morning", morningChecked);
                newMedication.put("morningTaken", 0);
                newMedication.put("morningTakenTime", 0);
                newMedication.put("afternoon", afternoonChecked);
                newMedication.put("afternoonTaken", 0);
                newMedication.put("afternoonTakenTime", 0);
                newMedication.put("evening", eveningChecked);
                newMedication.put("eveningTaken", 0);
                newMedication.put("eveningTakenTime", 0);
                newMedication.put("bed", bedChecked);
                newMedication.put("bedTaken", 0);
                newMedication.put("bedTakenTime", 0);
                newMedication.put("asRequired", asRequiredChecked);
                medRef.child(medicationId).setValue(newMedication);
                Toast.makeText(AddMedicationActivity.this, "Content Saved!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MedicationTabbedActivity.class);
                intent.putExtra("patientID", patientId);
                startActivity(intent);
            }
        });


        /*
          Code to check a user is logged in.
          If they are not logged in, return them to the login page.
         */
        authListener = new FirebaseAuth.AuthStateListener()

        {
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
    private void signOut() {
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
        return mToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }


    /**
     * On Start is called when the activity restarts.
     */
    @Override
    public void onStart() {
        super.onStart();
        //Clear text fields
        medicationName.setText("");
        dosageType.setText("");
        //Clear Checkboxes
        morningCheck.setChecked(false);
        afternoonCheck.setChecked(false);
        eveningCheck.setChecked(false);
        bedCheck.setChecked(false);
        asRequiredCheck.setChecked(false);
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
