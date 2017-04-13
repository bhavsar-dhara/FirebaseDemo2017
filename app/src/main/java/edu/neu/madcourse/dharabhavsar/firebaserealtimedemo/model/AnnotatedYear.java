package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnotatedYear {

    private Map<Long, AnnotatedMonths> year;

    public Map<Long, AnnotatedMonths> getYear() {
        return year;
    }

    public void setYear(Map<Long, AnnotatedMonths> year) {
        this.year = year;
    }
}
