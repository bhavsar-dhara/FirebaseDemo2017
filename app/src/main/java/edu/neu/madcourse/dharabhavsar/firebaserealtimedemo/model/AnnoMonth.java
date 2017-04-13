package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoMonth {

    private Map<String, AnnoDay> months;

    public AnnoMonth() {
    }

    public AnnoMonth(Map<String, AnnoDay> months) {
        this.months = months;
    }

    public Map<String, AnnoDay> getMonths() {
        return months;
    }

    public void setMonths(Map<String, AnnoDay> months) {
        this.months = months;
    }
}
