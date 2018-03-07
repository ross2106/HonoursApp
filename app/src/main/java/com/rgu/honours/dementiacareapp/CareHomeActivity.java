package com.rgu.honours.dementiacareapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

public class CareHomeActivity extends AppCompatActivity {

    private RecyclerView patientListView;
    ArrayList<PatientInfo> patientArrayList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private String userId;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_home);


        // *************************************************
        // Navigation Drawer
        // *************************************************

        mDrawerLayout = (DrawerLayout) findViewById(R.id.careHomeDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(CareHomeActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Carer Home Page"); //Set the title of the page
        NavigationView navigationView = findViewById(R.id.care_home_navigation_view); //Navigation view from layout file
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() { //Setting on click listeners for menu items
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.log_out:
                        item.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        signOut();
                }
                return true;
            }
        });

        // *************************************************
        // *************************************************

        Button addPatient = (Button) findViewById(R.id.addPatient);
        patientListView = (RecyclerView) findViewById(R.id.patientView);

        mAuth = FirebaseAuth.getInstance();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        DatabaseReference patientDbRef = dbRef.child("Users").child(userId).child("Patients");
        patientDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    final PatientInfo patient = new PatientInfo();
                    patient.setId(ds.getValue(PatientInfo.class).getId());
                    patient.setName(ds.getValue(PatientInfo.class).getName());
                    patient.setAge(ds.getValue(PatientInfo.class).getAge());
                    patientArrayList.add(patient);
                    mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                    patientListView.setLayoutManager(mLayoutManager);
                    MyAdapter adapter = new MyAdapter(getApplicationContext(), patientArrayList);
                    patientListView.setAdapter(adapter);
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

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void signOut() {
        mAuth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
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

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private Context mContext;
        private ArrayList<PatientInfo> patients;


        MyAdapter(Context context, ArrayList<PatientInfo> list) {
            mContext = context;
            patients = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.patient_card, parent, false);
            ViewHolder viewHolder = new ViewHolder(view, mContext, patients);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            PatientInfo patient = patients.get(position);

            TextView patientName = holder.patientName;
            TextView patientAge = holder.patientAge;
            final ImageView patientProfileImage = holder.patientImage;

            patientName.setText(patient.getName());
            patientAge.setText(patient.getAge());
            StorageReference patientImageRef = FirebaseStorage.getInstance().getReference().child(userId).child(patient.getId()).child("Profile Picture");
            patientImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.with(CareHomeActivity.this).load(uri).into(patientProfileImage);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    patientProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.person));
                }
            });
        }

        @Override
        public int getItemCount() {
            return patients.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView patientImage;
            TextView patientName, patientAge;
            ArrayList<PatientInfo> patientList = new ArrayList<PatientInfo>();
            Context context;

            public ViewHolder(View itemView, Context context, ArrayList<PatientInfo> patientList) {
                super(itemView);
                this.patientList = patientList;
                this.context = context;
                itemView.setOnClickListener(this);
                patientImage = itemView.findViewById(R.id.patientProfileImage);
                patientName = itemView.findViewById(R.id.patientName);
                patientAge = itemView.findViewById(R.id.patientAge);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                PatientInfo patient = this.patientList.get(position);
                Intent intent = new Intent(context, PatientProfile.class);
                intent.putExtra("patientID", patient.getId());
                this.context.startActivity(intent);
            }
        }

    }
}


