package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import android.util.Log;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnoFileDetails {

    private static final String TAG = AnnoFileDetails.class.getSimpleName();

    private String data;
    private String annotation;

    public AnnoFileDetails() {
    }

    public AnnoFileDetails(String data, String annotation) {
        this.data = data;
        this.annotation = annotation;
    }

    public String getData() {
        String tempData = data.replaceAll("_", ".");
        Log.d(TAG, "getData: " + tempData);
        return tempData;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAnnotation() {
        String tempData = annotation.replaceAll("_", ".");
        Log.d(TAG, "getAnnotation: " + tempData);
        return tempData;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
