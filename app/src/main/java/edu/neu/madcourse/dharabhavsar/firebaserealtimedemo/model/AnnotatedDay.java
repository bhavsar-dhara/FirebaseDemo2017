package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnotatedDay {

    private Map<Long, AnnotatedHours> day;

    public Map<Long, AnnotatedHours> getDay() {
        return day;
    }

    public void setDay(Map<Long, AnnotatedHours> day) {
        this.day = day;
    }
}
