package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoMonth {

    private Map<Long, AnnoDay> months;

    public AnnoMonth() {
    }

    public AnnoMonth(Map<Long, AnnoDay> months) {
        this.months = months;
    }

    public Map<Long, AnnoDay> getMonths() {
        return months;
    }

    public void setMonths(Map<Long, AnnoDay> months) {
        this.months = months;
    }
}
