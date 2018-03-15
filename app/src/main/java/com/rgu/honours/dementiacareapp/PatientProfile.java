package com.rgu.honours.dementiacareapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class PatientProfile extends AppCompatActivity {

    //Layout views
    TextView patientName;
    ImageView patientImage;
    Button thisIsMe, medicationButton;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef, patientDbRef;

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
        patientName = findViewById(R.id.patientProfileName);
        patientName.setText(getIntent().getStringExtra("patientName"));
        //Patient Profile Picture
        patientImage = findViewById(R.id.patientMainProfileImage);
        //thisIsMeButton
        thisIsMe = findViewById(R.id.thisIsMeButton);
        //medicationButton
        medicationButton = findViewById(R.id.medicationButton);

        //Get an instance of Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Create a database reference
        dbRef = FirebaseDatabase.getInstance().getReference();

        //Get the current user
        final FirebaseUser user = mAuth.getCurrentUser();
        //Assign that users ID
        userId = user.getUid();

        /**
         * CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.patientProfileDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(PatientProfile.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Profile Page"); //Set the title of the page
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
        //Retrieve the profile picture based on the patient ID
        profilePhotoRef = FirebaseStorage.getInstance().getReference().child(userId).child(patientId).child("Profile Picture");
        //Code to populate the profile picture
        profilePhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(PatientProfile.this).load(uri).into(patientImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                patientImage.setImageDrawable(getResources().getDrawable(R.drawable.photo));
            }
        });
        //Set an on click listener if the user wishes to change their profile picture
        patientImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
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
                intent.putExtra("patientName", patientName.getText().toString());
                startActivity(intent);
            }
        });
        //Set on click listener for medication Button
        medicationButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MedicationActivity.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("patientName", patientName.getText().toString());
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
        if(item.getItemId() == R.id.deletePatient){
            AlertDialog.Builder builder = new AlertDialog.Builder(PatientProfile.this, R.style.AlertDialog);
            builder.setMessage("Are you sure you want to delete this person from your care profile?")
                    .setTitle("Delete Individual");
            // Add the buttons
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    startActivity(new Intent(getApplicationContext(), CareHomeActivity.class));
                    dbRef.child("Users").child(userId).child("Patients").child(patientId).removeValue();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.patient_profile_dropdown, menu);
        return true;
    }


    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){
            imageUri = data.getData();
            uploadProfilePicture();
        }
    }

    /**
     * Code to upload the profile picture to the realtime database
     */
    private void uploadProfilePicture(){
        if(imageUri != null){
            profilePhotoRef.delete();
            profilePhotoRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(PatientProfile.this, "Image uploaded.", Toast.LENGTH_LONG).show();
                            Picasso.with(getApplicationContext()).load(imageUri).into(patientImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PatientProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else{
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
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
