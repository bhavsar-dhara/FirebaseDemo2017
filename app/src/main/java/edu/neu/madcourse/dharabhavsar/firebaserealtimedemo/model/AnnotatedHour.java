package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import java.util.*;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnotatedHour {

    private Map<Long, AnnotatedFileDetails> hour;

    public Map<Long, AnnotatedFileDetails> getHour() {
        return hour;
    }

    public void setHour(Map<Long, AnnotatedFileDetails> hour) {
        this.hour = hour;
    }
}
