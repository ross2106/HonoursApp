package com.rgu.honours.dementiacareapp.Medication;

/**
 * Created by ross1 on 13/03/2018.
 */

public class MedicationModel {

    private String id, name, dosageValue, dosageType, category;
    private Long time;
    private int morningTaken, afternoonTaken, eveningTaken, bedTaken;

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getMorningTaken() {
        return morningTaken;
    }

    public void setMorningTaken(int morningTaken) {
        this.morningTaken = morningTaken;
    }

    public int getAfternoonTaken() {
        return afternoonTaken;
    }

    public void setAfternoonTaken(int afternoonTaken) {
        this.afternoonTaken = afternoonTaken;
    }

    public int getEveningTaken() {
        return eveningTaken;
    }

    public void setEveningTaken(int eveningTaken) {
        this.eveningTaken = eveningTaken;
    }

    public int getBedTaken() {
        return bedTaken;
    }

    public void setBedTaken(int bedTaken) {
        this.bedTaken = bedTaken;
    }
}
