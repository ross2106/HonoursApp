package com.rgu.honours.dementiacareapp.Patient;

import android.net.Uri;

/**
 * Created by ross1 on 01/03/2018.
 */

public class PatientModel {

    private String name;
    private String gender;
    private String age;
    private String id;

    public PatientModel() {

    }

    public PatientModel(String id, String name, String gender, String age) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
