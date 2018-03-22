package com.rgu.honours.dementiacareapp.Events;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Medication.EditMedication;
import com.rgu.honours.dementiacareapp.Medication.MedicationTabbedActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class EditEventActivity extends AppCompatActivity {


    //Text Fields
    private EditText eventName;
    private Button eventDate, eventStart, eventFinish;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mStartTimeSetListener;
    private TimePickerDialog.OnTimeSetListener mFinishTimeSetListener;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    DatabaseReference dbRef;

    private String patientId;
    private String patientName;
    private String userId;
    private String eventId;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        eventName = findViewById(R.id.editEventName);
        eventStart = findViewById(R.id.editStartTime);
        eventFinish = findViewById(R.id.editFinishTime);
        eventDate = findViewById(R.id.editEventDate);

        eventDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        EditEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = dayOfMonth + "/" + month + "/" + year;
                eventDate.setText(date);
            }
        };

        eventStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(
                        EditEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mStartTimeSetListener,
                        hour, min,
                        true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mStartTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventStart.setText(String.format("%02d:%02d", hourOfDay, minute));
            }
        };

        eventFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int min = cal.get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(
                        EditEventActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mFinishTimeSetListener,
                        hour, min,
                        true);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mFinishTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                eventFinish.setText(String.format("%02d:%02d", hourOfDay, minute));
            }
        };

        Button editEvent = findViewById(R.id.editEventSubmit);

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
        patientName = getIntent().getStringExtra("patientName");
        eventId = getIntent().getStringExtra("eventID");

        /*
          CODE FOR NAVIGATION DRAWER
         */
        mDrawerLayout = findViewById(R.id.editEventDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(EditEventActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Edit Event"); //Set the title of the page

        NavigationView navigationView = findViewById(R.id.editEvent_navigation_view); //Navigation view from layout file
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

        final DatabaseReference eventRef = dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Events").child(eventId);
        eventRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                eventName.setText(dataSnapshot.child("name").getValue().toString());
                eventDate.setText(dateParse((Long) dataSnapshot.child("date").getValue()));
                eventStart.setText(timeParse((Long) dataSnapshot.child("startTime").getValue()));
                eventFinish.setText(timeParse((Long) dataSnapshot.child("finishTime").getValue()));
            }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String eventNameString = eventName.getText().toString();
                @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                String dateTime;
                Long startTime = null, finishTime = null, date = null;
                try {
                    startTime = timeFormat.parse(eventStart.getText().toString()).getTime();
                    finishTime = timeFormat.parse(eventFinish.getText().toString()).getTime();
                    dateTime = eventDate.getText().toString() + " " + eventStart.getText().toString();
                    date = dateFormat.parse(dateTime).getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(startTime >= finishTime){
                    Toast.makeText(EditEventActivity.this, "The start time must be before the finish time!", Toast.LENGTH_LONG).show();
                } else if(startTime < finishTime){
                    Map newEvent = new HashMap<>();
                    newEvent.put("id", eventId);
                    newEvent.put("name", eventNameString);
                    newEvent.put("date", date);
                    newEvent.put("startTime", startTime);
                    newEvent.put("finishTime", finishTime);
                    eventRef.updateChildren(newEvent);
                    Toast.makeText(EditEventActivity.this, "Event Saved!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), EventsActivity.class);
                    intent.putExtra("patientID", patientId);
                    startActivity(intent);
                }
            }
        });

                /*
          Code to check a user is logged in.
          If they are not logged in, return them to the login page.
         */
        authListener = new FirebaseAuth.AuthStateListener()

        {
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

    private String dateParse(Long date){
        return new SimpleDateFormat("dd/MM/yyyy").format(new Date(date));
    }

    private String timeParse(Long date){
        return new SimpleDateFormat("HH:mm").format(new Date(date));
    }

    /**
     * Code to sign out a user.
     */
    private void signOut() {
        mAuth.signOut();
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
        if (item.getItemId() == R.id.deleteEvent) {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditEventActivity.this, R.style.AlertDialog);
            builder.setMessage("Are you sure you want to delete this event?")
                    .setTitle("Delete Event");
            // Add the buttons
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(EditEventActivity.this, EventsActivity.class);
                    intent.putExtra("patientID", patientId);
                    startActivity(intent);
                    dbRef.child("Users").child(userId).child("Patients").child(patientId).child("Events").child(eventId).removeValue();
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
        mMenuInflater.inflate(R.menu.edit_event_dropdown, menu);
        return true;
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
