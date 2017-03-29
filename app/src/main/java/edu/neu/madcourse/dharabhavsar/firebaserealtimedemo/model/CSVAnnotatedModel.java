package edu.neu.madcourse.dharabhavsar.firebaserealtimedemo.model;

import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Dhara on 3/28/2017.
 */

public class CSVAnnotatedModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @CsvBindByName(required = true)
    private Date HEADER_TIME_STAMP;
    @CsvBindByName(required = true)
    private String X_ACCELERATION_METERS_PER_SECOND_SQUARED;
    @CsvBindByName(required = true)
    private String Y_ACCELERATION_METERS_PER_SECOND_SQUARED;
    @CsvBindByName(required = true)
    private int Z_ACCELERATION_METERS_PER_SECOND_SQUARED;

    public Date getHEADER_TIME_STAMP() {
        return HEADER_TIME_STAMP;
    }

    public void setHEADER_TIME_STAMP(Date HEADER_TIME_STAMP) {
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

    public int getZ_ACCELERATION_METERS_PER_SECOND_SQUARED() {
        return Z_ACCELERATION_METERS_PER_SECOND_SQUARED;
    }

    public void setZ_ACCELERATION_METERS_PER_SECOND_SQUARED(int z_ACCELERATION_METERS_PER_SECOND_SQUARED) {
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
