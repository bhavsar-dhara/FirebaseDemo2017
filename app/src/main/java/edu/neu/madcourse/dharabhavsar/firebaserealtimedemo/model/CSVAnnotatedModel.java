package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import com.univocity.parsers.annotations.Parsed;

import java.io.Serializable;

/**
 * Created by Dhara on 3/28/2017.
 */

public class CSVAnnotatedModel implements Serializable {
    private static final long serialVersionUID = 1L;

//    @CsvBindByName(required = true)
    @Parsed(index = 0)
    private String HEADER_TIME_STAMP;
//    @CsvBindByName(required = true)
    @Parsed(index = 1)
    private String X_ACCELERATION_METERS_PER_SECOND_SQUARED;
//    @CsvBindByName(required = true)
    @Parsed(index = 2)
    private String Y_ACCELERATION_METERS_PER_SECOND_SQUARED;
//    @CsvBindByName(required = true)
    @Parsed(index = 3)
    private String Z_ACCELERATION_METERS_PER_SECOND_SQUARED;

    public String getHEADER_TIME_STAMP() {
        return HEADER_TIME_STAMP;
    }

    public void setHEADER_TIME_STAMP(String HEADER_TIME_STAMP) {
        this.HEADER_TIME_STAMP = HEADER_TIME_STAMP;
    }

    public String getX_ACCELERATION_METERS_PER_SECOND_SQUARED() {
        return X_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    public void setX_ACCELERATION_METERS_PER_SECOND_SQUARED(String x_ACCELERATION_METERS_PER_SECOND_SQUARED) {
        X_ACCELERATION_METERS_PER_SECOND_SQUARED = x_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    public String getY_ACCELERATION_METERS_PER_SECOND_SQUARED() {
        return Y_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    public void setY_ACCELERATION_METERS_PER_SECOND_SQUARED(String y_ACCELERATION_METERS_PER_SECOND_SQUARED) {
        Y_ACCELERATION_METERS_PER_SECOND_SQUARED = y_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    public String getZ_ACCELERATION_METERS_PER_SECOND_SQUARED() {
        return Z_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    public void setZ_ACCELERATION_METERS_PER_SECOND_SQUARED(String z_ACCELERATION_METERS_PER_SECOND_SQUARED) {
        Z_ACCELERATION_METERS_PER_SECOND_SQUARED = z_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    @Override
    public String toString()
    {
        return "CSVAnnotatedModel [HEADER_TIME_STAMP=" + HEADER_TIME_STAMP
                + ", X_ACCELERATION_METERS_PER_SECOND_SQUARED=" + X_ACCELERATION_METERS_PER_SECOND_SQUARED
                + ", Y_ACCELERATION_METERS_PER_SECOND_SQUARED=" + Y_ACCELERATION_METERS_PER_SECOND_SQUARED
                + ", Z_ACCELERATION_METERS_PER_SECOND_SQUARED=" + Z_ACCELERATION_METERS_PER_SECOND_SQUARED + "]";
    }
}
