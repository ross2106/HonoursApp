package com.rgu.honours.dementiacareapp.Medication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ross1 on 17/03/2018.
 */

public class MedicationBedTab extends Fragment {

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
        View view = inflater.inflate(R.layout.medication_bed_tab, container, false);
        medicationList = view.findViewById(R.id.medicationBedView);

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
                    if (ds.child("bed").getValue().equals(true)) {
                        medication.setCategory(ds.getValue(MedicationModel.class).getCategory());
                        medication.setName(ds.getValue(MedicationModel.class).getName());
                        medication.setDosageType(ds.getValue(MedicationModel.class).getDosageType());
                        medication.setId(ds.getValue(MedicationModel.class).getId());
                        medication.setBedTaken(ds.getValue(MedicationModel.class).getBedTaken());
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
            View view = layoutInflater.inflate(R.layout.medication_card, parent, false);
            return new MyAdapter.ViewHolder(view, mContext, medicationList);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, int position) {
            final MedicationModel medication = medicationList.get(position);
            TextView medicationName = holder.medicationName;
            TextView medicationDosageType = holder.medicationDosageType;
            final CheckBox checkBox = holder.checkbox;
            medicationName.setText(medication.getName());
            medicationDosageType.setText(medication.getDosageType());
            if (medication.getBedTaken() == 1) {
                checkBox.setChecked(true);
            }
            final DatabaseReference medRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
            medRef.child(medication.getId()).child("bedTakenTime").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        Date takenDate = new Date((Long) dataSnapshot.getValue());
                        Date todayDate = new Date(System.currentTimeMillis());
                        Calendar cal1 = Calendar.getInstance();
                        Calendar cal2 = Calendar.getInstance();
                        cal1.setTime(takenDate);
                        cal2.setTime(todayDate);
                        if (cal1.get(Calendar.DATE) < cal2.get(Calendar.DATE)) {
                            medRef.child(medication.getId()).child("bedTaken").setValue(0);
                            medRef.child(medication.getId()).child("bedTakenTime").setValue(0);
                        }
                    }

                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        medRef.child(medication.getId()).child("bedTaken").setValue(1);
                        medRef.child(medication.getId()).child("bedTakenTime").setValue(System.currentTimeMillis());
                    }
                    if (!isChecked) {
                        medRef.child(medication.getId()).child("bedTaken").setValue(0);
                        medRef.child(medication.getId()).child("bedTakenTime").setValue(0);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return medicationList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView medicationName;
            final TextView medicationDosageType;
            final CheckBox checkbox;
            ArrayList<MedicationModel> medicationList;

            public ViewHolder(final View itemView, Context context, final ArrayList<MedicationModel> medicationList) {
                super(itemView);
                this.medicationList = medicationList;
                itemView.setOnClickListener(this);
                medicationName = itemView.findViewById(R.id.medicationName);
                checkbox = itemView.findViewById(R.id.medicationTaken);
                medicationDosageType = itemView.findViewById(R.id.medicationDosageType);
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            itemView.setBackgroundColor(Color.parseColor("#43A047"));
                        }
                        if (!isChecked) {
                            itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                });
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
