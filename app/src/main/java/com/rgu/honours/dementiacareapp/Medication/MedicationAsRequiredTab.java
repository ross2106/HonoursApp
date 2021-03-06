package com.rgu.honours.dementiacareapp.Medication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.R;

import java.util.ArrayList;

/**
 * Created by ross1 on 17/03/2018.
 */

public class MedicationAsRequiredTab extends Fragment {

    RecyclerView medicationList;

    private String userId;
    private String patientId;
    private String patientName;

    //Initialising list of patients
    private final ArrayList<MedicationModel> medicationArrayList = new ArrayList<>();
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.medication_asrequired_tab, container, false);
        medicationList = view.findViewById(R.id.medicationRequiredView);

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
        patientId = getActivity().getIntent().getStringExtra("patientID");
        patientName = getActivity().getIntent().getStringExtra("patientName");

        DatabaseReference medicationRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
        medicationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                medicationArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MedicationModel medication = new MedicationModel();
                    if (ds.child("asRequired").getValue().equals(true)) {
                        medication.setCategory(ds.getValue(MedicationModel.class).getCategory());
                        medication.setName(ds.getValue(MedicationModel.class).getName());
                        medication.setDosageType(ds.getValue(MedicationModel.class).getDosageType());
                        medication.setId(ds.getValue(MedicationModel.class).getId());
                        medicationArrayList.add(medication);
                    }
                    mLayoutManager = new LinearLayoutManager(getActivity());
                    medicationList.setLayoutManager(mLayoutManager);
                    MyAdapter adapter = new MyAdapter(getContext(), medicationArrayList);
                    medicationList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
                    startActivity(new Intent(getContext(), MainActivity.class));
                    getActivity().finish();
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

    /**
     * Code for the Patient List Adapter.
     * This code populates the list of patients.
     */
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private final Context mContext;
        private final ArrayList<MedicationModel> medicationList;

        MyAdapter(Context context, ArrayList<MedicationModel> list) {
            mContext = context;
            medicationList = list;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.medication_asrequired_card, parent, false);
            return new MyAdapter.ViewHolder(view, mContext, medicationList);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, int position) {
            final MedicationModel medication = medicationList.get(position);
            TextView medicationName = holder.medicationName;
            TextView medicationDosageType = holder.medicationDosageType;
            medicationName.setText(medication.getName());
            medicationDosageType.setText(medication.getDosageType());
        }

        @Override
        public int getItemCount() {
            return medicationList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView medicationName;
            final TextView medicationDosageType;
            ArrayList<MedicationModel> medicationList;

            public ViewHolder(final View itemView, Context context, final ArrayList<MedicationModel> medicationList) {
                super(itemView);
                this.medicationList = medicationList;
                itemView.setOnClickListener(this);
                medicationName = itemView.findViewById(R.id.medicationName);
                medicationDosageType = itemView.findViewById(R.id.medicationDosageType);

            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                MedicationModel medication = medicationList.get(position);
                Intent intent = new Intent(getActivity(), EditMedication.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("patientID", patientId);
                intent.putExtra("medicationID", medication.getId());
                intent.putExtra("patientName", patientName);
                startActivity(intent);
            }
        }
    }
}
