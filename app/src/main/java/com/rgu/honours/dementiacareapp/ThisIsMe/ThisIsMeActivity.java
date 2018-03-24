package com.rgu.honours.dementiacareapp.ThisIsMe;

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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Medication.EditMedication;
import com.rgu.honours.dementiacareapp.Medication.MedicationTabbedActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;

public class ThisIsMeActivity extends AppCompatActivity {

    //Text Fields
    private TextView fullName;
    private TextView preferredName;
    private TextView knowsBest;
    private TextView myBackground;
    private TextView myRoutine;
    private TextView mayUpsetMe;
    private TextView makesMeFeelBetter;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    private String patientId;
    private String patientName;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_this_is_me);

        //Initialising Text Fields
        fullName = findViewById(R.id.fullNameText);
        preferredName = findViewById(R.id.preferredNameText);
        knowsBest = findViewById(R.id.knowsMeBestText);
        myBackground = findViewById(R.id.myBackGroundText);
        myRoutine = findViewById(R.id.myRoutineText);
        mayUpsetMe = findViewById(R.id.mayUpsetMeText);
        makesMeFeelBetter = findViewById(R.id.makesMeFeelBetterText);

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        //Get the current user
        final FirebaseUser user = mAuth.getCurrentUser();
        //Assign that users ID
        String userId = user.getUid();
        //Get the ID of the patient from the Intent extra String
        patientId = getIntent().getStringExtra("patientID");
        patientName = getIntent().getStringExtra("patientName");


        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.thisIsMeDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(ThisIsMeActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("This Is Me"); //Set the title of the page

        NavigationView navigationView = findViewById(R.id.thisIsMe_navigation_view); //Navigation view from layout file
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

        //Populate text fields
        DatabaseReference patientDbRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("ThisIsMe");
        patientDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    fullName.setText(dataSnapshot.child("fullName").getValue().toString());
                    preferredName.setText(dataSnapshot.child("preferredName").getValue().toString());
                    knowsBest.setText(dataSnapshot.child("knowsBest").getValue().toString());
                    myBackground.setText(dataSnapshot.child("myBackground").getValue().toString());
                    myRoutine.setText(dataSnapshot.child("myRoutine").getValue().toString());
                    mayUpsetMe.setText(dataSnapshot.child("upsetMe").getValue().toString());
                    makesMeFeelBetter.setText(dataSnapshot.child("makeBetter").getValue().toString());
                }
                if(!dataSnapshot.hasChildren()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ThisIsMeActivity.this, R.style.AlertDialog);
                    builder.setMessage("There is no 'This is Me' information for this individual yet. Would you like to add this now?")
                            .setTitle("This is Me");
                    // Add the buttons
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(ThisIsMeActivity.this, EditThisIsMeActivity.class);
                            intent.putExtra("patientID", patientId);
                            intent.putExtra("patientName", patientName);
                            startActivity(intent);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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

    /**
     * Code to sign out a user.
     */
    private void signOut() {
        mAuth.signOut();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.dropdown, menu);
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
        if (item.getItemId() == R.id.editContentItem) {
            Intent intent = new Intent(getApplicationContext(), EditThisIsMeActivity.class);
            intent.putExtra("patientID", patientId);
            intent.putExtra("patientName", patientName);
            startActivity(intent);
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
