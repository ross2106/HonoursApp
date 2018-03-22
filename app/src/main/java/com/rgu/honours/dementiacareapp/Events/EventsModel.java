package com.rgu.honours.dementiacareapp.Events;

/**
 * Created by ross1 on 22/03/2018.
 */

public class EventsModel {

    Long startTime, finishTime, date;
    String name, id, desc;

    public EventsModel() {

    }

    public Long getStartTime() {
        return startTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
