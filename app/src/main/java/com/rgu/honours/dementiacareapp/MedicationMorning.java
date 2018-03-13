package com.rgu.honours.dementiacareapp;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ross1 on 12/03/2018.
 */

public class MedicationMorning extends android.support.v4.app.Fragment {

    //Initialising list of patients
    private RecyclerView medicationList;
    private RecyclerView.LayoutManager mLayoutManager;

    //Initialising Firebase Authorisation
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //String for the currently logged in user
    private String userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medication_morning_fragment, container, false);

        //Get Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        //List of patients
        medicationList = (RecyclerView) getView().findViewById(R.id.medicationMorningView);

        //Reference to Realtime Database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.keepSynced(true);

        //Get the currently logged in user
        final FirebaseUser user = mAuth.getCurrentUser();
        userId = user.getUid();

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
