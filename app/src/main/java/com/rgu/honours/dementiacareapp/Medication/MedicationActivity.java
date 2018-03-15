package com.rgu.honours.dementiacareapp.Medication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MedicationActivity extends AppCompatActivity {

    //Initialising list of patients
    private final ArrayList<MedicationModel> medicationArrayList = new ArrayList<>();
    //private RecyclerView medicationList;
    private RecyclerView.LayoutManager mLayoutManager;

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
        setContentView(R.layout.activity_medication);

        final RecyclerView medicationList = findViewById(R.id.medicationMorningView);

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

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
        mDrawerLayout = findViewById(R.id.medicationDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(MedicationActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
            getSupportActionBar().setTitle("Medication"); //Set the title of the page
        }
        NavigationView navigationView = findViewById(R.id.medication_navigation_view); //Navigation view from layout file
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

        DatabaseReference medicationRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
        medicationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                medicationArrayList.clear();
                int counter = 0;
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    final MedicationModel medication = new MedicationModel();
                    medication.setName(ds.getValue(MedicationModel.class).getName());
                    medication.setDosageValue(ds.getValue(MedicationModel.class).getDosageValue());
                    medication.setDosageType(ds.getValue(MedicationModel.class).getDosageType());
                    medication.setTime(ds.getValue(MedicationModel.class).getTime());
                    medication.setId(ds.getValue(MedicationModel.class).getId());
                    medication.setTaken(ds.getValue(MedicationModel.class).getTaken());
                    medicationArrayList.add(medication);
                    for (int i = 0; i < medicationArrayList.size(); i++) {
                        if (medicationArrayList.get(i).getTime() > medicationArrayList.get(counter).getTime()) {
                            Collections.swap(medicationArrayList, i, counter);
                        }
                    }
                    counter++;
                    mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    medicationList.setLayoutManager(mLayoutManager);
                    MyAdapter adapter = new MyAdapter(getApplicationContext(), medicationArrayList);
                    medicationList.setAdapter(adapter);

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
        mMenuInflater.inflate(R.menu.medication_dropdown, menu);
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
        if (item.getItemId() == R.id.addMedication) {
            Intent intent = new Intent(getApplicationContext(), AddMedicationActivity.class);
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

    /**
     * Code for the Patient List Adapter.
     * This code populates the list of patients.
     */
    public class MyAdapter extends RecyclerView.Adapter<MedicationActivity.MyAdapter.ViewHolder> {

        private final Context mContext;
        private final ArrayList<MedicationModel> medicationList;

        MyAdapter(Context context, ArrayList<MedicationModel> list) {
            mContext = context;
            medicationList = list;
        }

        @Override
        public MedicationActivity.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.medication_card, parent, false);
            return new ViewHolder(view, mContext, medicationList);
        }

        @Override
        public void onBindViewHolder(final MedicationActivity.MyAdapter.ViewHolder holder, int position) {
            final MedicationModel medication = medicationList.get(position);
            TextView medicationName = holder.medicationName;
            TextView medicationDosage = holder.medicationDosage;
            TextView medicationTime = holder.medicationTime;
            final CheckBox checkBox = holder.checkbox;
            medicationName.setText(medication.getName());
            medicationDosage.setText(medication.getDosageValue() + " " + medication.getDosageType());
            medicationTime.setText(timeParse(medication.getTime()));
            if (medication.getTaken() == 1) {
                checkBox.setChecked(true);
            }
            final DatabaseReference medRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        medRef.child(medication.getId()).child("taken").setValue(1);
                    }
                    if (!isChecked) {
                        medRef.child(medication.getId()).child("taken").setValue(0);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return medicationList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView medicationName;
            final TextView medicationDosage;
            final TextView medicationTime;
            final CheckBox checkbox;
            ArrayList<MedicationModel> medicationList = new ArrayList<>();

            public ViewHolder(final View itemView, Context context, final ArrayList<MedicationModel> medicationList) {
                super(itemView);
                this.medicationList = medicationList;
                itemView.setOnClickListener(this);
                medicationName = itemView.findViewById(R.id.medicationName);
                medicationDosage = itemView.findViewById(R.id.medicationDosage);
                medicationTime = itemView.findViewById(R.id.medicationTime);
                checkbox = itemView.findViewById(R.id.medicationTaken);
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            itemView.setBackgroundColor(Color.parseColor("#43A047"));
                        }
                        if (!isChecked) {
                            itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                });
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                MedicationModel medication = medicationList.get(position);
                Intent intent = new Intent(MedicationActivity.this, EditMedication.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("medicationID", medication.getId());
                intent.putExtra("patientName", patientName);
                startActivity(intent);
            }
        }
    }
}
