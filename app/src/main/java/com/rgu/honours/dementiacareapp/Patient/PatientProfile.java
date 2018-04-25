package com.rgu.honours.dementiacareapp.Patient;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.Events.EventsActivity;
import com.rgu.honours.dementiacareapp.Family.FamilyActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Medication.MedicationTabbedActivity;
import com.rgu.honours.dementiacareapp.R;
import com.rgu.honours.dementiacareapp.ThisIsMe.ThisIsMeActivity;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PatientProfile extends AppCompatActivity {

    //Layout views
    private TextView patientName;
    private ImageView patientImage;
    private Button thisIsMe, medicationButton, familyButton, eventsButton;
    private ProgressBar progressBar;
    private FloatingActionButton uploadPhoto;


    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    //Firebase Storage
    private StorageReference profilePhotoRef;
    private Uri imageUri;

    //Image picker
    private static final int GALLERY_INTENT = 2;

    //ID of logged in carer, and patient for profile
    private String userId;
    private String patientId;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        //Patient Name
        //patientName = findViewById(R.id.patientProfileName);
        //patientName.setText(getIntent().getStringExtra("patientName"));
        //Patient Profile Picture
        patientImage = findViewById(R.id.patientMainProfileImage);
        //Photo upload button
        uploadPhoto = findViewById(R.id.uploadPhoto);
        //thisIsMeButton
        thisIsMe = findViewById(R.id.thisIsMeButton);
        //medicationButton
        medicationButton = findViewById(R.id.medicationButton);
        //Family Button
        familyButton = findViewById(R.id.familyButton);
        //Events Button
        eventsButton = findViewById(R.id.eventsButton);
        //Progress Bar
        progressBar = findViewById(R.id.patientProfileProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        //Get the current user
        final FirebaseUser user = mAuth.getCurrentUser();
        //Assign that users ID
        userId = user.getUid();

        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.patientProfileDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(PatientProfile.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        NavigationView navigationView = findViewById(R.id.patient_profile_navigation_view); //Navigation view from layout file
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
                }
                return true;
            }
        });

        //Get the ID of the patient from the Intent extra String
        patientId = getIntent().getStringExtra("patientID");
        DatabaseReference patientNameRef = dbRef.child("Users").child(userId).child("Patients").child(patientId);
        patientNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString()); //Set the title of the page
                    thisIsMe.setText("This is " + dataSnapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Retrieve the profile picture based on the patient ID
        profilePhotoRef = FirebaseStorage.getInstance().getReference().child(userId).child(patientId).child("Profile Picture");
        //Code to populate the profile picture
        profilePhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {
                Picasso.with(PatientProfile.this)
                        .load(uri)
                        .into(patientImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }
                            @Override
                            public void onError() {
                                //Try again online if cache failed
                                Picasso.with(PatientProfile.this)
                                        .load(uri)
                                        .error(R.drawable.person_white)
                                        .into(patientImage, new Callback() {
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
                patientImage.setImageDrawable(getResources().getDrawable(R.drawable.person));
                progressBar.setVisibility(View.GONE);
            }
        });
        uploadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
        //Set an on click listener for the "This is me" Button
        thisIsMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ThisIsMeActivity.class);
                intent.putExtra("patientID", patientId);
                //intent.putExtra("patientName", patientName.getText().toString());
                startActivity(intent);
            }
        });
        //Set on click listener for medication Button
        medicationButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MedicationTabbedActivity.class);
                intent.putExtra("patientID", patientId);
                //intent.putExtra("patientName", patientName.getText().toString());
                startActivity(intent);
            }
        });

        //Set on click listener for family button
        familyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FamilyActivity.class);
                intent.putExtra("patientID", patientId);
                //intent.putExtra("patientName", patientName.getText().toString());
                startActivity(intent);
            }
        });

        //Set on click listener for events button
        eventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EventsActivity.class);
                intent.putExtra("patientID", patientId);
                //intent.putExtra("patientName", patientName.getText().toString());
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
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == R.id.deletePatient) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PatientProfile.this, R.style.AlertDialog);
            builder.setMessage("Are you sure you want to delete this person from your care profile?")
                    .setTitle("Delete Individual");
            // Add the buttons
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(getApplicationContext(), CareHomeActivity.class));
                    Toast.makeText(getApplicationContext(), "The individual has been successfully deleted from your care.", Toast.LENGTH_LONG).show();
                    dbRef.child("Users").child(userId).child("Patients").child(patientId).removeValue();
                    profilePhotoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
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
            AlertDialog.Builder builder = new AlertDialog.Builder(PatientProfile.this, R.style.AlertDialog);
            builder.setMessage("This page allows you to amend details about somebody in your care." +
                    "\n\nSet a profile photo by pressing the camera icon, or press any of the buttons to explore the different pages.")
                    .setTitle("Individual Profile Page");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.patient_profile_dropdown, menu);
        return true;
    }


    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            imageUri = data.getData();
            uploadProfilePicture();
        }
    }

    /**
     * Code to upload the profile picture to the realtime database
     */
    private void uploadProfilePicture() {
        if (imageUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            profilePhotoRef.delete();
            profilePhotoRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(PatientProfile.this, "Image uploaded.", Toast.LENGTH_LONG).show();
                            Picasso.with(getApplicationContext()).load(imageUri).into(patientImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PatientProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.setMessage("Uploading image...");
                }
            });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    private File getFile() {
        File folder = new File("sdcard/mobileCare");
        if (!folder.exists()) {
            folder.mkdir();
        }
        File image = new File(folder, "cam_image.jpg");
        return image;
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
