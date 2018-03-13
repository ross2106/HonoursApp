package com.rgu.honours.dementiacareapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ross1 on 12/03/2018.
 */

public class MedicationMorning extends android.support.v4.app.Fragment {


    //Initialising list of patients
    ArrayList<MedicationModel> medicationArrayList = new ArrayList<>();
    //private RecyclerView medicationList;
    private RecyclerView.LayoutManager mLayoutManager;

    //Initialising Firebase Authorisation
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //String for the currently logged in user
    private String userId;
    private String patientId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medication_morning_fragment, container, false);
        final RecyclerView medicationList = (RecyclerView) view.findViewById(R.id.medicationMorningView);

        //Get Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        //Reference to Realtime Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        //Get the currently logged in user
        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();
        //Get the ID of the patient from the Intent extra String
        patientId = getActivity().getIntent().getStringExtra("patientID");

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
                    MedicationAdapter adapter = new MedicationAdapter(medicationArrayList);
                    medicationList.setAdapter(adapter);
                    mLayoutManager = new LinearLayoutManager(getActivity());
                    medicationList.setLayoutManager(mLayoutManager);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        /**
         * Check to see if a user is logged In.
         * If no user is logged in, return to main page.
         */
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                }
            }
        };


        return view;
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
