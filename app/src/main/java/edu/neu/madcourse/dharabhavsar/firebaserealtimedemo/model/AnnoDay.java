package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoDay {

    private Map<String, AnnoHour> days;

    public AnnoDay() {
    }

    public AnnoDay(Map<String, AnnoHour> days) {
        this.days = days;
    }

    public Map<String, AnnoHour> getDays() {
        return days;
    }

    public void setDays(Map<String, AnnoHour> days) {
        this.days = days;
    }
}
