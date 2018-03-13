package com.rgu.honours.dementiacareapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ross1 on 13/03/2018.
 */

public class MedicationAdapter extends RecyclerView.Adapter<MedicationAdapter.ViewHolder> {

    private ArrayList<MedicationModel> medicationList;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView medicationName, medicationDosage, medicationTime;

        public ViewHolder(View itemView) {
            super(itemView);
            medicationName = itemView.findViewById(R.id.medicationName);
            medicationDosage = itemView.findViewById(R.id.medicationDosage);
            medicationTime = itemView.findViewById(R.id.medicationTime);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            MedicationModel medication = medicationList.get(position);
            Intent intent = new Intent(itemView.getContext(), EditMedication.class);
            intent.putExtra("patientID", intent.getExtras());
            intent.putExtra("medicationID", medication.getId());
            itemView.getContext().startActivity(intent);
        }
    }

    MedicationAdapter(ArrayList<MedicationModel> list) {
        medicationList = list;
    }

    @NonNull
    @Override
    public MedicationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medication_card, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicationModel medication = medicationList.get(position);
        TextView medicationName = holder.medicationName;
        TextView medicationDosage = holder.medicationDosage;
        TextView medicationTime = holder.medicationTime;
        medicationName.setText(medication.getName());
        medicationDosage.setText(medication.getDosageValue() + " " + medication.getDosageType());
        medicationTime.setText(medication.getTime());

    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

}
