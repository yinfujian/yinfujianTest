package com.yinfujian.creation.builder;

public class Builder {
    protected Bike mBike;

    public void buildFrame() {

    }

    public void buildSeat() {

    }

    public Bike createBike() {
        mBike = new Bike();
        return mBike;
    }
}
