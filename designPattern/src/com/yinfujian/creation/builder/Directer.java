package com.yinfujian.creation.builder;

public class Directer {

    private Builder mBuilder;

    public Directer(Builder builder) {
        this.mBuilder = builder;
    }

    public Bike construct() {
        mBuilder.buildFrame();
        mBuilder.buildSeat();
        return mBuilder.mBike;
    }
}
