package com.rgu.honours.dementiacareapp.Family;

/**
 * Created by ross1 on 19/03/2018.
 */

public class FamilyModel {

    String name;
    String contactNo;
    String id;
    String relation;

    public FamilyModel(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }
}
