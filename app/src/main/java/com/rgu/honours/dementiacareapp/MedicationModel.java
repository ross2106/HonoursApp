package com.rgu.honours.dementiacareapp;

/**
 * Created by ross1 on 13/03/2018.
 */

public class MedicationModel {

    String id, name, dosageValue, dosageType, time;

    public MedicationModel(String id, String name, String dosageValue, String dosageType, String time) {
        this.id = id;
        this.name = name;
        this.dosageValue = dosageValue;
        this.dosageType = dosageType;
        this.time = time;
    }

    public MedicationModel(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosageValue() {
        return dosageValue;
    }

    public void setDosageValue(String dosageValue) {
        this.dosageValue = dosageValue;
    }

    public String getDosageType() {
        return dosageType;
    }

    public void setDosageType(String dosageType) {
        this.dosageType = dosageType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
