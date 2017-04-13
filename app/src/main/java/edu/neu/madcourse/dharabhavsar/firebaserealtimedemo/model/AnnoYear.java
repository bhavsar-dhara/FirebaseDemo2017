package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoYear {

    private Map<String, AnnoMonth> years;
    private Map<String, AnnoDay> months;
    private Map<String, AnnoHour> days;
    private Map<String, AnnoFileDetails> hours;
    private String data;
    private String annotation;

    public AnnoYear() {
    }

    public AnnoYear(Map<String, AnnoMonth> years, Map<String, AnnoDay> months, Map<String, AnnoHour> days,
                    Map<String, AnnoFileDetails> hours, String data, String annotation) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.data = data;
        this.annotation = annotation;
    }

    public Map<String, AnnoMonth> getYears() {
        return years;
    }

    public void setYears(Map<String, AnnoMonth> years) {
        this.years = years;
    }

    public Map<String, AnnoDay> getMonths() {
        return months;
    }

    public void setMonths(Map<String, AnnoDay> months) {
        this.months = months;
    }

    public Map<String, AnnoHour> getDays() {
        return days;
    }

    public void setDays(Map<String, AnnoHour> days) {
        this.days = days;
    }

    public Map<String, AnnoFileDetails> getHours() {
        return hours;
    }

    public void setHours(Map<String, AnnoFileDetails> hours) {
        this.hours = hours;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
