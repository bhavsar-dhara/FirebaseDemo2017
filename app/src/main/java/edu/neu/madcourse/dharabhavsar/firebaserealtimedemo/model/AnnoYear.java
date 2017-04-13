package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoYear {

    private Map<Long, AnnoMonth> years;

    public AnnoYear() {
    }

    public AnnoYear(Map<Long, AnnoMonth> years) {
        this.years = years;
    }

    public Map<Long, AnnoMonth> getYears() {
        return years;
    }

    public void setYears(Map<Long, AnnoMonth> years) {
        this.years = years;
    }
}
