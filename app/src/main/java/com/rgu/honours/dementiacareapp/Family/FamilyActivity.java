package com.rgu.honours.dementiacareapp.Family;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Patient.AddPatientActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientModel;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FamilyActivity extends AppCompatActivity {

    //Initialising list of patients
    private RecyclerView familyList;
    private final ArrayList<FamilyModel> familyArrayList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;

    //Initialising Firebase Authorisation
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //String for the currently logged in user
    private String userId;
    private String patientId;
    private String patientName;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    //On Activity Creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);

        //Get Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        patientId = getIntent().getStringExtra("patientID");
        patientName = getIntent().getStringExtra("patientName");

        //Button to add Patient
        FloatingActionButton addFamily = findViewById(R.id.addFamily);
        //List of patients
        familyList = findViewById(R.id.familyView);

        //Reference to Realtime Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        //Get the currently logged in user
        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        /*
          CREATING NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.familyDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(FamilyActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Family Members"); //Set the title of the page
        NavigationView navigationView = findViewById(R.id.family_navigation_view); //Navigation view from layout file
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

        /*
          Check to see if a user is logged In.
          If no user is logged in, return to main page.
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

        /*
          Code to get a list of patients from the Realtime Database
         */
        DatabaseReference patientDbRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Family");
        patientDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    final FamilyModel familyMember = new FamilyModel();
                    familyMember.setName(ds.getValue(FamilyModel.class).getName());
                    familyMember.setPhoneNumber(ds.getValue(FamilyModel.class).getPhoneNumber());
                    familyArrayList.add(familyMember);
                    mLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                    familyList.setLayoutManager(mLayoutManager);
                    MyAdapter adapter = new MyAdapter(getApplicationContext(), familyArrayList);
                    familyList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
          On Click Listener for the "Add Patient" Button.
         */
        addFamily.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddFamily.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("patientName", patientName);
                startActivity(intent);
            }
        });

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
     * Code to sign a user out
     */
    private void signOut() {
        mAuth.signOut();
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
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final Context mContext;
        private final ArrayList<FamilyModel> family;

        MyAdapter(Context context, ArrayList<FamilyModel> list) {
            mContext = context;
            family = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.family_card, parent, false);
            return new ViewHolder(view, mContext, family);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FamilyModel family = familyArrayList.get(position);
            TextView familyMemberName = holder.familyMemberName;
            //final ImageView familyMemberImage = holder.familyMemberImage;
            familyMemberName.setText(family.getName());
/*            StorageReference familyImageRef = FirebaseStorage.getInstance().getReference().child(userId).child(patientId).child("Family");
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
            });*/
        }

        @Override
        public int getItemCount() {
            return familyArrayList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView familyMemberImage;
            final TextView familyMemberName;
            ArrayList<FamilyModel> familyList = new ArrayList<>();
            final Context context;

            public ViewHolder(View itemView, Context context, ArrayList<FamilyModel> familyList) {
                super(itemView);
                this.familyList = familyList;
                this.context = context;
                itemView.setOnClickListener(this);
                familyMemberImage = itemView.findViewById(R.id.familyMemberImage);
                familyMemberName = itemView.findViewById(R.id.familyMemberName);
            }

            @Override
            public void onClick(View view) {
/*                int position = getAdapterPosition();
                FamilyModel patient = this.familyList.get(position);
                Intent intent = new Intent(context, PatientProfile.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("patientName", patient.getName());
                this.context.startActivity(intent);*/
            }
        }

    }
}

