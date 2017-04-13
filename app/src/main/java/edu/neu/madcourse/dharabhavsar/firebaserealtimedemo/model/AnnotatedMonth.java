package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnotatedMonth {

    private Map<Long, AnnotatedDays> month;

    public Map<Long, AnnotatedDays> getMonth() {
        return month;
    }

    public void setMonth(Map<Long, AnnotatedDays> month) {
        this.month = month;
    }
}
