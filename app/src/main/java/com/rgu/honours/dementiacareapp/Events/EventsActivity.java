package com.rgu.honours.dementiacareapp.Events;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rgu.honours.dementiacareapp.Carer.CareHomeActivity;
import com.rgu.honours.dementiacareapp.MainActivity;
import com.rgu.honours.dementiacareapp.Patient.PatientProfile;
import com.rgu.honours.dementiacareapp.R;

import java.util.ArrayList;
import java.util.List;

public class EventsActivity extends AppCompatActivity {

    //Initialising list of patients
    private final ArrayList<EventsModel> eventsArrayList = new ArrayList<>();
    //private RecyclerView medicationList;
    private RecyclerView.LayoutManager mLayoutManager;

    //Firebase User Authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;

    //Firebase Database
    private DatabaseReference dbRef;

    //ID of logged in carer, and patient for profile
    private String userId;
    private String patientId;
    private String patientName;

    private FloatingActionButton addEvent;

    //Navigation Drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar toolbar;

    private SectionsPageAdapter mSectionsPageAdapter;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);

        addEvent = findViewById(R.id.addEvent);

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

        /*
          CODE FOR NAVIGATION DRAWER
         */
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        mDrawerLayout = findViewById(R.id.eventsDrawerLayout); //Drawer from layout file
        mToggle = new ActionBarDrawerToggle(EventsActivity.this, mDrawerLayout, R.string.open, R.string.close); //Setting action toggle
        mDrawerLayout.addDrawerListener(mToggle); //Settings drawer listener
        mToggle.syncState(); //Synchronize with drawer layout state
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Show button
        getSupportActionBar().setTitle("Events"); //Set the title of the page
        NavigationView navigationView = findViewById(R.id.medication_tabbed_navigation_view); //Navigation view from layout file
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

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddEventActivity.class);
                intent.putExtra("patientID", patientId);
                intent.putExtra("patientName", patientName);
                startActivity(intent);
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
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        };
    }

    /**
     * Code to sign out a user.
     */
    private void signOut() {
        mAuth.signOut();
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

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new UpComingEventsTab(), "UPCOMING EVENTS");
        adapter.addFragment(new PreviousEventsTab(), "PREVIOUS EVENTS");
        viewPager.setAdapter(adapter);
    }

    public class SectionsPageAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SectionsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
    }
}
