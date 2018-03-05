package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        patientName = findViewById(R.id.patientProfileName);
        patientImage = findViewById(R.id.patientMainProfileImage);

        mAuth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference();

        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

        patientId = getIntent().getStringExtra("patientID");
        profilePhotoRef = FirebaseStorage.getInstance().getReference().child(userId).child(patientId).child("Profile Picture");
        profilePhotoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(PatientProfile.this).load(uri).into(patientImage);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                patientImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_a_photo_black_24dp));
            }
        });
        patientImage.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        });

        patientDbRef = dbRef.child("Users").child(userId).child("Patients").child(patientId);
        patientDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PatientInfo patient = new PatientInfo();
                patient.setName(dataSnapshot.getValue(PatientInfo.class).getName());
                patientName.setText(patient.getName());
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_INTENT && resultCode == RESULT_OK){
            imageUri = data.getData();
            uploadProfilePicture();
        }
    }

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

    @Override
    protected void onResume() {
        super.onResume();
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
}
