package com.rgu.honours.dementiacareapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ross1 on 13/03/2018.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    ArrayList<String> myDataset;

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView medicationName, medicationDosage, medicationTime;
        public CardView mCardView;
        public MyViewHolder(View v){
            super(v);

            mCardView = (CardView) v.findViewById(R.id.medicationCard);
            medicationName = (TextView) v.findViewById(R.id.medicationName);
            medicationDosage = (TextView) v.findViewById(R.id.medicationDosage);
            medicationTime = (TextView) v.findViewById(R.id.medicationTime);
        }
    }

    public MyAdapter(ArrayList<String> myDataset){
        myDataset = myDataset;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.medication_card, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.medicationName

    }
}
