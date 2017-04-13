package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoHour {

    private Map<String, AnnoFileDetails> hours;

    public AnnoHour() {
    }

    public AnnoHour(Map<String, AnnoFileDetails> hours) {
        this.hours = hours;
    }

    public Map<String, AnnoFileDetails> getHours() {
        return hours;
    }

    public void setHours(Map<String, AnnoFileDetails> hours) {
        this.hours = hours;
    }
}
