/*
 * Copyright (c) 2018.  citizenapp project
 *
 */

package io.kreolab.mobileid.model;

public class RegistrationCenter {

    private String mName;
    private float mLongitude;
    private float mLatitude;

    public RegistrationCenter(){}
    public RegistrationCenter(String mName, float latitude, float longitude) {
        this.mName = mName;
        this.mLongitude = longitude;
        this.mLatitude = latitude;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public float getLongitude() {
        return mLongitude;
    }

    public void setLongitude(float logitude) {
        this.mLongitude = logitude;
    }

    public float getLatitude() {
        return mLatitude;
    }

    public void setLatitude(float mLatitude) {
        this.mLatitude = mLatitude;
    }

    @Override
    public String toString() {
        return "RegistrationCenter{" +
                "mName='" + mName + '\'' +
                ", mLongitude=" + mLongitude +
                ", mLatitude=" + mLatitude +
                '}';
    }
}
