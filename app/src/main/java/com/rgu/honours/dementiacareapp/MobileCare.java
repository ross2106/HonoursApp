package com.rgu.honours.dementiacareapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ross1 on 17/03/2018.
 */

public class MobileCare extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
