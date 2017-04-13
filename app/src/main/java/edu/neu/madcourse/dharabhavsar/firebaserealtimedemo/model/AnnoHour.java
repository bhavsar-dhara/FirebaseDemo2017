package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoHour {

    private Map<Long, AnnoFileDetails> hours;

    public AnnoHour() {
    }

    public AnnoHour(Map<Long, AnnoFileDetails> hours) {
        this.hours = hours;
    }

    public Map<Long, AnnoFileDetails> getHours() {
        return hours;
    }

    public void setHours(Map<Long, AnnoFileDetails> hours) {
        this.hours = hours;
    }
}
