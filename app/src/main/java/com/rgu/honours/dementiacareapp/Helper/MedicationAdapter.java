package com.rgu.honours.dementiacareapp.Helper;

/**
 * Created by ross1 on 17/03/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rgu.honours.dementiacareapp.Medication.EditMedication;
import com.rgu.honours.dementiacareapp.Medication.MedicationActivity;
import com.rgu.honours.dementiacareapp.Medication.MedicationModel;
import com.rgu.honours.dementiacareapp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Code for the Patient List Adapter.
 * This code populates the list of patients.
 */
public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    private final Context mContext;
    private final ArrayList<MedicationModel> medicationList;
    private final DatabaseReference dbRef;
    private final String userId, patientId;

    MedicationAdapter(Context context, ArrayList<MedicationModel> list, DatabaseReference dbRef, String userId, String patientId) {
        mContext = context;
        medicationList = list;
        this.dbRef = dbRef;
        this.userId = userId;
        this.patientId = patientId;
    }

    public String timeParse(Long time) {
        return new SimpleDateFormat("HH:mm").format(new Date(time));
    }

    @Override
    public MedicationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.medication_card, parent, false);
        return new MedicationAdapter.ViewHolder(view, mContext, medicationList);
    }

    @Override
    public void onBindViewHolder(final MedicationAdapter.ViewHolder holder, int position) {
        final MedicationModel medication = medicationList.get(position);
        TextView medicationName = holder.medicationName;
        TextView medicationDosage = holder.medicationDosage;
        TextView medicationTime = holder.medicationTime;
        final CheckBox checkBox = holder.checkbox;
        medicationName.setText(medication.getName());
        medicationDosage.setText(medication.getDosageValue() + " " + medication.getDosageType());
        medicationTime.setText(timeParse(medication.getTime()));
        if (medication.getTaken() == 1) {
            checkBox.setChecked(true);
        }
        final DatabaseReference medRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Medication");
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    medRef.child(medication.getId()).child("taken").setValue(1);
                }
                if (!isChecked) {
                    medRef.child(medication.getId()).child("taken").setValue(0);
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
        final TextView medicationDosage;
        final TextView medicationTime;
        final CheckBox checkbox;
        ArrayList<MedicationModel> medicationList = new ArrayList<>();

        public ViewHolder(final View itemView, Context context, final ArrayList<MedicationModel> medicationList) {
            super(itemView);
            this.medicationList = medicationList;
            itemView.setOnClickListener(this);
            medicationName = itemView.findViewById(R.id.medicationName);
            medicationDosage = itemView.findViewById(R.id.medicationDosage);
            medicationTime = itemView.findViewById(R.id.medicationTime);
            checkbox = itemView.findViewById(R.id.medicationTaken);
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
            Intent intent = new Intent(view.getContext().getApplicationContext().getApplicationContext(), EditMedication.class);
            intent.putExtra("patientID", patientId);
            intent.putExtra("medicationID", medication.getId());
            //intent.putExtra("patientName", patientName);
            //startActivity(intent);
        }
    }
}