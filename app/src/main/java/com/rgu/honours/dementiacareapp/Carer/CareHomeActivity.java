package com.rgu.honours.dementiacareapp.Carer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Patient.AddPatientActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientModel;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CareHomeActivity extends AppCompatActivity {

    //Initialising list of patients
    private RecyclerView patientListView;
    private final ArrayList<PatientModel> patientArrayList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;
    //private int noOfColumns;

    //Initialising Firebase Authorisation
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference dbRef;

    //String for the currently logged in user
    private String userId;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    //On Activity Creation
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_home);

        //Get Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        /*
          CREATING NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.careHomeDrawerLayout); //Drawer from layout file
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

        //Button to add Patient
        FloatingActionButton addPatient = findViewById(R.id.addPatient);
        //List of patients
        patientListView = findViewById(R.id.patientView);

        //Reference to Realtime Database
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        //Get the currently logged in user
        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

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
        DatabaseReference patientDbRef = dbRef.child("Users").child(userId).child("Patients");
        patientDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                patientArrayList.clear();
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    final PatientModel patient = new PatientModel();
                    patient.setId(ds.getValue(PatientModel.class).getId());
                    patient.setName(ds.getValue(PatientModel.class).getName());
                    patient.setAge(ds.getValue(PatientModel.class).getAge());
                    patient.setGender(ds.getValue(PatientModel.class).getGender());
                    patientArrayList.add(patient);
                    mLayoutManager = new GridLayoutManager(getApplicationContext(), adjustGrid());
                    patientListView.setLayoutManager(mLayoutManager);
                    MyAdapter adapter = new MyAdapter(CareHomeActivity.this, patientArrayList);
                    patientListView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
          On Click Listener for the "Add Patient" Button.
         */
        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), AddPatientActivity.class);
                startActivity(i);
            }
        });

    }

    //Adjust the list view based off of the screen orientation and size
    private int adjustGrid() {
        String sizeName = "";
        int noOfColumns = 0;
        boolean landscape = getApplicationContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean largeScreen = false;
        boolean normalScreen = false;
        boolean xlargeScreen = false;
        int screenLayout = getApplicationContext().getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;
        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                sizeName = "small"; //Small screen size
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                sizeName = "normal"; //Normal screen size
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                sizeName = "large"; //Large screen size
                break;
            case 4: // Configuration.SCREENLAYOUT_SIZE_XLARGE is API >= 9
                sizeName = "xlarge";
                break;
            default:
                sizeName = "undefined";
                break;
        }
        //Change the number of columns based off of the screensize and the orientation
        if (sizeName.equals("normal")) {
            normalScreen = true;
        }
        if (sizeName.equals("large")) {
            largeScreen = true;
        }
        if (sizeName.equals("xlarge")) {
            xlargeScreen = true;
        }
        if (landscape && normalScreen) {
            noOfColumns = 3;
        }
        if (landscape && largeScreen) {
            noOfColumns = 3;
        }
        if (landscape && xlargeScreen) {
            noOfColumns = 6;
        }
        if (!landscape && normalScreen) {
            noOfColumns = 2;
        }
        if (!landscape && largeScreen) {
            noOfColumns = 2;
        }
        if (!landscape && xlargeScreen) {
            noOfColumns = 4;
        }
        return noOfColumns;
    }

    //Dropdown menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.care_home_dropdown, menu);
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
        //Delete profile selected
        if (item.getItemId() == R.id.deleteProfile) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CareHomeActivity.this, R.style.AlertDialog);
            builder.setMessage("Warning: This will delete your login and all associated data!")
                    .setTitle("Are you sure you want to delete your carer profile?")
                    .setIcon(R.drawable.warning);
            // Add the buttons
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Carer profile deleted.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    dbRef.child("Users").child(userId).removeValue();
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
        if (item.getItemId() == R.id.moreInfo) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CareHomeActivity.this, R.style.AlertDialog);
            builder.setMessage("This page allows you to view all of the individuals in your care. " +
                    "\n\nPress the icon in the bottom right of the screen in order to add somebody to your care. Once you have added an individual, click on their icon to go to their profile page.")
                    .setTitle("Carer Home Page");
            // Add the buttons
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
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
     * This code populates the list of patients with their image and description
     */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final Context mContext;
        private final ArrayList<PatientModel> patients;

        MyAdapter(Context context, ArrayList<PatientModel> list) {
            mContext = context;
            patients = list;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.patient_card, parent, false);
            return new ViewHolder(view, mContext, patients);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            PatientModel patient = patients.get(position);
            TextView patientName = holder.patientName;
            final ProgressBar progressBar = holder.progressBar;
            final ImageView patientProfileImage = holder.patientImage;
            patientName.setText(patient.getName() + ", " + patient.getAge());
            progressBar.setVisibility(View.VISIBLE);
            StorageReference patientImageRef = FirebaseStorage.getInstance().getReference().child(userId).child(patient.getId()).child("Profile Picture");
            patientImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(final Uri uri) {
                    Picasso.with(CareHomeActivity.this)
                            .load(uri)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(patientProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {
                                }
                                @Override
                                public void onError() {
                                    //Try again online if cache failed
                                    Picasso.with(CareHomeActivity.this)
                                            .load(uri)
                                            .error(R.drawable.person_white)
                                            .into(patientProfileImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }
                                                @Override
                                                public void onError() {
                                                    Log.v("Picasso", "Could not fetch image");
                                                }
                                            });
                                }
                            });
                    progressBar.setVisibility(View.GONE);
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    patientProfileImage.setImageDrawable(getResources().getDrawable(R.drawable.person));
                    progressBar.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return patients.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final ImageView patientImage;
            final TextView patientName;
            final ProgressBar progressBar;
            ArrayList<PatientModel> patientList;
            final Context context;

            public ViewHolder(View itemView, Context context, ArrayList<PatientModel> patientList) {
                super(itemView);
                this.patientList = patientList;
                this.context = context;
                itemView.setOnClickListener(this);
                patientImage = itemView.findViewById(R.id.patientProfileImage);
                patientName = itemView.findViewById(R.id.patientName);
                progressBar = itemView.findViewById(R.id.patientImageProgress);
            }

            //Called when you click on an individuals card
            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                PatientModel patient = this.patientList.get(position);
                Intent intent = new Intent(CareHomeActivity.this, PatientProfile.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("patientID", patient.getId());
                intent.putExtra("patientName", patient.getName());
                startActivity(intent);
            }
        }

    }
}


