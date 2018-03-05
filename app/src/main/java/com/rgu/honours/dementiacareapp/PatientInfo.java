package com.rgu.honours.dementiacareapp;

import android.net.Uri;

/**
 * Created by ross1 on 01/03/2018.
 */

public class PatientInfo {

    private String image;
    private String name;
    private String gender;
    private String age;
    private String id;

    public PatientInfo() {

    }

    public PatientInfo(String id, String name, String gender, String age/*, int image*/) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.id = id;
        //this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
