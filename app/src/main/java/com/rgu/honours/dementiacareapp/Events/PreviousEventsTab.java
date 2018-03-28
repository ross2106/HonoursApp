package com.rgu.honours.dementiacareapp.Events;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * Created by ross1 on 22/03/2018.
 */

public class PreviousEventsTab extends Fragment {

    RecyclerView eventsListView;

    private String userId;
    private String patientId;
    private String patientName;

    //Initialising list of patients
    private final ArrayList<EventsModel> eventsArrayList = new ArrayList<>();
    //private RecyclerView medicationList;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.previous_events_tab, container, false);
        eventsListView = view.findViewById(R.id.previousEventsView);

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

        final DatabaseReference eventRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Events");
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventsArrayList.clear();
                int counter = 0;
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    EventsModel event = new EventsModel();
                    if(ds.getValue(EventsModel.class).getDate() < System.currentTimeMillis()){
                        event.setId(ds.getValue(EventsModel.class).getId());
                        event.setName(ds.getValue(EventsModel.class).getName());
                        event.setDate(ds.getValue(EventsModel.class).getDate());
                        event.setStartTime(ds.getValue(EventsModel.class).getStartTime());
                        event.setFinishTime(ds.getValue(EventsModel.class).getFinishTime());
                        eventsArrayList.add(event);
                        for (int i = 0; i < eventsArrayList.size(); i++) {
                            if (eventsArrayList.get(i).getDate() > eventsArrayList.get(counter).getDate()) {
                                Collections.swap(eventsArrayList, i, counter);
                            }
                        }
                        counter++;
                    }
                    if (ds.getValue(EventsModel.class).getDate() < (System.currentTimeMillis() - 604800000)) {
                        eventRef.child(ds.getValue(EventsModel.class).getId()).removeValue();
                    }
                }
                mLayoutManager = new LinearLayoutManager(getActivity());
                eventsListView.setLayoutManager(mLayoutManager);
                MyAdapter adapter = new MyAdapter(getContext(), eventsArrayList);
                eventsListView.setAdapter(adapter);
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

    private String timeParse(Long time) {
        return new SimpleDateFormat("HH:mm").format(new Date(time));
    }

    private String dateParse(Long date){
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(date));
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
        private final ArrayList<EventsModel> eventsList;

        MyAdapter(Context context, ArrayList<EventsModel> list) {
            mContext = context;
            eventsList = list;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater.inflate(R.layout.events_card, parent, false);
            return new MyAdapter.ViewHolder(view, mContext, eventsList);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, int position) {
            final EventsModel event = eventsList.get(position);
            TextView eventName = holder.eventName;
            TextView eventStart = holder.eventStart;
            TextView eventFinish = holder.eventFinish;
            TextView eventDate = holder.eventDate;
            eventName.setText(event.getName());
            eventStart.setText(timeParse(event.getStartTime()));
            eventFinish.setText(timeParse(event.getFinishTime()));
            eventDate.setText(dateParse(event.getDate()));
        }

        @Override
        public int getItemCount() {
            return eventsList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView eventName, eventStart, eventFinish, eventDate;
            ArrayList<EventsModel> eventsList = new ArrayList<>();

            public ViewHolder(final View itemView, Context context, final ArrayList<EventsModel> eventsList) {
                super(itemView);
                this.eventsList = eventsList;
                itemView.setOnClickListener(this);
                eventName = itemView.findViewById(R.id.eventName);
                eventStart = itemView.findViewById(R.id.eventStart);
                eventFinish = itemView.findViewById(R.id.eventFinish);
                eventDate = itemView.findViewById(R.id.eventDate);
            }

            @Override
            public void onClick(View view) {
                int position = getAdapterPosition();
                EventsModel event = eventsList.get(position);
                Intent intent = new Intent(getActivity(), EditEventActivity.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("eventID", event.getId());
                intent.putExtra("patientName", getActivity().getIntent().getStringExtra("patientName"));
                startActivity(intent);
            }
        }
    }
}
