package com.rgu.honours.dementiacareapp.Family;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;
import com.squareup.picasso.Picasso;

public class FamilyProfileActivity extends AppCompatActivity {

    //Layout views
    private TextView familyMemberName;
    private ImageView familyImage;
    private Button callButton;

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
    private String patientName;
    private String familyId;
    private String phoneNo;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_profile);

        //Family Member Name
        familyMemberName = findViewById(R.id.familyProfileName);
        //Family Member Profile Picture
        familyImage = findViewById(R.id.familyProfilePicture);
        //Call button
        callButton = findViewById(R.id.callButton);

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
        //Get the patients name to pass onto new Intent
        patientName = getIntent().getStringExtra("patientName");

        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.familyProfileDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(FamilyProfileActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Family Member Profile"); //Set the title of the page
        NavigationView navigationView = findViewById(R.id.family_profile_navigation_view); //Navigation view from layout file
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

        //Get the ID of the family member from the Intent extra
        familyId = getIntent().getStringExtra("familyID");
        final DatabaseReference familyDbRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Family").child(familyId);
        familyDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    familyMemberName.setText(dataSnapshot.getValue(FamilyModel.class).getName());
                    phoneNo = dataSnapshot.getValue(FamilyModel.class).getContactNo();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Retrieve the profile picture based on the patient ID
        profilePhotoRef = FirebaseStorage.getInstance().getReference().child(userId).child(patientId).child("Family").child(familyId);
        //Code to populate the profile picture
        profilePhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(FamilyProfileActivity.this).load(uri).into(familyImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                familyImage.setImageDrawable(getResources().getDrawable(R.drawable.photo));
            }
        });
        //Set an on click listener if the user wishes to change their profile picture
        familyImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });
        //Set an on click listener to call the family member
        callButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                dialPhoneNumber(phoneNo);
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

    private void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
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
        if (item.getItemId() == R.id.deleteFamily) {
            AlertDialog.Builder builder = new AlertDialog.Builder(FamilyProfileActivity.this, R.style.AlertDialog);
            builder.setMessage("Are you sure you want to delete this family member?")
                    .setTitle("Delete Family Member");
            // Add the buttons
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(FamilyProfileActivity.this, FamilyActivity.class);
                    intent.putExtra("patientID", patientId);
                    startActivity(intent);
                    dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Family").child(familyId).removeValue();
                    Toast.makeText(getApplicationContext(), "The family member has been successfully deleted.", Toast.LENGTH_LONG).show();
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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mMenuInflater = getMenuInflater();
        mMenuInflater.inflate(R.menu.family_profile_dropdown, menu);
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
                            Toast.makeText(FamilyProfileActivity.this, "Image uploaded.", Toast.LENGTH_LONG).show();
                            Picasso.with(getApplicationContext()).load(imageUri).into(familyImage);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(FamilyProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
