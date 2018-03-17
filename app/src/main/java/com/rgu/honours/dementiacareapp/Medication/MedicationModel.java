package com.rgu.honours.dementiacareapp.Medication;

/**
 * Created by ross1 on 13/03/2018.
 */

public class MedicationModel {

    private String id, name, dosageValue, dosageType, category;
    private Long time;
    private int taken;

    public MedicationModel() {

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

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getTaken() {
        return taken;
    }

    public void setTaken(int taken) {
        this.taken = taken;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
