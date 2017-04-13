package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

/**
 * Created by Dhara on 4/13/2017.
 */

public class AnnotatedFileDetails {

    private String data;
    private String annotation;

    public String getData() {
        return data.replaceAll("_", ".");
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAnnotation() {
        return annotation.replaceAll("_", ".");
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
}
