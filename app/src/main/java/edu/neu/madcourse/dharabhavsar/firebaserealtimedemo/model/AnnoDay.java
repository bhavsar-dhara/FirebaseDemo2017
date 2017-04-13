package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoDay {

    private Map<Long, AnnoHour> days;

    public AnnoDay() {
    }

    public AnnoDay(Map<Long, AnnoHour> days) {
        this.days = days;
    }

    public Map<Long, AnnoHour> getDays() {
        return days;
    }

    public void setDays(Map<Long, AnnoHour> days) {
        this.days = days;
    }
}
