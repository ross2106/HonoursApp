package com.rgu.honours.dementiacareapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

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
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MedicationActivity extends AppCompatActivity {

    //Initialising list of patients
    ArrayList<MedicationModel> medicationArrayList = new ArrayList<>();
    //private RecyclerView medicationList;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef, patientDbRef;

    //ID of logged in carer, and patient for profile
    private String userId;
    private String patientId;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication);

        final RecyclerView medicationList = (RecyclerView) findViewById(R.id.medicationMorningView);

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

        /**
         * CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.medicationDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(MedicationActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        if(getSupportActionBar() != null){
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
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    MedicationModel medication = new MedicationModel();
                    medication.setName(ds.getValue(MedicationModel.class).getName());
                    medication.setDosageValue(ds.getValue(MedicationModel.class).getDosageValue());
                    medication.setDosageType(ds.getValue(MedicationModel.class).getDosageType());
                    medication.setTime(ds.getValue(MedicationModel.class).getTime());
                    medicationArrayList.add(medication);
                    MyAdapter adapter = new MyAdapter(getApplicationContext(), medicationArrayList);
                    medicationList.setAdapter(adapter);
                    mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    medicationList.setLayoutManager(mLayoutManager);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
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
            startActivity(intent);
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

    /**
     * Code for the Patient List Adapter.
     * This code populates the list of patients.
     */
    public class MyAdapter extends RecyclerView.Adapter<MedicationActivity.MyAdapter.ViewHolder> {

        private Context mContext;
        private ArrayList<MedicationModel> medicationList;

        MyAdapter(Context context, ArrayList<MedicationModel> list) {
            mContext = context;
            medicationList = list;
        }

        @Override
        public MedicationActivity.MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.medication_card, parent, false);
            MedicationActivity.MyAdapter.ViewHolder viewHolder = new MedicationActivity.MyAdapter.ViewHolder(view, mContext, medicationList);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MedicationActivity.MyAdapter.ViewHolder holder, int position) {
            MedicationModel medication = medicationList.get(position);
            TextView medicationName = holder.medicationName;
            TextView medicationDosage = holder.medicationDosage;
            TextView medicationTime = holder.medicationTime;
            medicationName.setText(medication.getName());
            medicationDosage.setText(medication.getDosageValue() + " " + medication.getDosageType());
            medicationTime.setText(medication.getTime());
        }

        @Override
        public int getItemCount() {
            return medicationList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            TextView medicationName, medicationDosage, medicationTime;
            CheckBox checkbox;
            ArrayList<MedicationModel> medicationList = new ArrayList<MedicationModel>();
            Context context;

            public ViewHolder(final View itemView, Context context, ArrayList<MedicationModel> medicationList) {
                super(itemView);
                this.medicationList = medicationList;
                this.context = context;
                itemView.setOnClickListener(this);
                medicationName = itemView.findViewById(R.id.medicationName);
                medicationDosage = itemView.findViewById(R.id.medicationDosage);
                medicationTime = itemView.findViewById(R.id.medicationTime);
                checkbox = (CheckBox) itemView.findViewById(R.id.medicationTaken);
                checkbox.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        itemView.setBackgroundColor(Color.parseColor("#43A047"));
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
                startActivity(intent);
            }
        }

    }
}
