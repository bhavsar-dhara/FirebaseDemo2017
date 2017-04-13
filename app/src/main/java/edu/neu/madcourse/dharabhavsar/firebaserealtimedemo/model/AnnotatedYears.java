package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnotatedYears {

    private List<AnnotatedYear> years;

    public AnnotatedYears() {
        years = new ArrayList<>();
    }

    public List<AnnotatedYear> getYears() {
        return years;
    }

    public void setYears(List<AnnotatedYear> years) {
        this.years = years;
    }
}
