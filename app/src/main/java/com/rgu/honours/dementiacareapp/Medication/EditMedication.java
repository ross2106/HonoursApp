package com.rgu.honours.dementiacareapp.Medication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditMedication extends AppCompatActivity {

    //Text Fields
    private EditText editMedicationName;
    private EditText editDosageValue;
    private EditText editDosageTime;

    //Spinner
    private Spinner editDosageType;
    private ArrayAdapter<CharSequence> editSpinnerValues;
    private String editDosageTypeValue;

    //Button
    private Button editMedication;


    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    //ID of logged in carer, and patient for profile
    private String userId;
    private String patientId;
    private String medicationId;
    private String patientName;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_medication);

        editMedicationName = findViewById(R.id.editMedicationName);
        editDosageValue = findViewById(R.id.editMedicationDosageValue);
        editDosageTime = findViewById(R.id.editMedicationTime);
        editMedication = findViewById(R.id.editMedicationSubmit);

        //Dropdown
        editDosageType = findViewById(R.id.editDosageType);
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
        patientName = getIntent().getStringExtra("patientName");

        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.editMedicationDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(EditMedication.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Edit Medication"); //Set the title of the page
        NavigationView navigationView = findViewById(R.id.editMedication_navigation_view); //Navigation view from layout file
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

        DatabaseReference medicationRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication").child(medicationId);
        medicationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    editMedicationName.setText(dataSnapshot.child("name").getValue().toString());
                    editDosageValue.setText(dataSnapshot.child("dosageValue").getValue().toString());
                    editDosageTime.setText(timeParse((Long) dataSnapshot.child("time").getValue()));
                    editDosageTypeValue = dataSnapshot.child("dosageType").getValue().toString();
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        editMedication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference medicationRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication").child(medicationId);
                String medicationNameString = editMedicationName.getText().toString();
                String dosageValueString = editDosageValue.getText().toString();
                String dosageValueTypeString = editDosageTypeValue;
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Long medicationTime = null, morningStartTime = null, morningFinishTime = null, afternoonStartTime = null, afternoonFinishTime = null, eveningStartTime = null, eveningFinishTime = null;
                String category = "";
                try {
                    medicationTime = dateFormat.parse(editDosageTime.getText().toString()).getTime();
                    morningStartTime = dateFormat.parse("00:00").getTime();
                    morningFinishTime = dateFormat.parse("11:59").getTime();
                    afternoonStartTime = dateFormat.parse("12:00").getTime();
                    afternoonFinishTime = dateFormat.parse("16:59").getTime();
                    eveningStartTime = dateFormat.parse("17:00").getTime();
                    eveningFinishTime = dateFormat.parse("23:59").getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (medicationTime >= morningStartTime && medicationTime <= morningFinishTime) {
                    category = "Morning";
                }
                if (medicationTime >= afternoonStartTime && medicationTime <= afternoonFinishTime) {
                    category = "Afternoon";
                }
                if (medicationTime >= eveningStartTime && medicationTime <= eveningFinishTime)
                {
                    category = "Evening";
                }
                Map editMedication = new HashMap();
                editMedication.put("id", medicationId);
                editMedication.put("name", medicationNameString);
                editMedication.put("dosageValue", dosageValueString);
                editMedication.put("dosageType", dosageValueTypeString);
                editMedication.put("time", medicationTime);
                editMedication.put("category", category);
                medicationRef.updateChildren(editMedication);
                Toast.makeText(EditMedication.this, "Content Edited!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MedicationTabbedActivity.class);
                intent.putExtra("patientID", patientId);
                startActivity(intent);
            }
        });


        /*
          Code to check a user is logged in.
          If they are not logged in, return them to the login page.
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

    private String timeParse(Long time) {
        return new SimpleDateFormat("HH:mm").format(new Date(time));
    }

    /**
     * Code to sign out a user.
     */
    private void signOut() {
        mAuth.signOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.edit_medication_dropdown, menu);
        return true;
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
        if (item.getItemId() == R.id.deleteMedication) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditMedication.this, R.style.AlertDialog);
            builder.setMessage("Are you sure you want to delete this medication?")
                    .setTitle("Delete Medication");
            // Add the buttons
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(EditMedication.this, MedicationTabbedActivity.class);
                    intent.putExtra("patientID", patientId);
                    startActivity(intent);
                    dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication").child(medicationId).removeValue();
                    finish();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    dialog.cancel();
                }
            });
            AlertDialog dialog = builder.show();
        }
        return super.onOptionsItemSelected(item);
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
