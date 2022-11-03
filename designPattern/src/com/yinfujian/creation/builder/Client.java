package com.yinfujian.creation.builder;

public class Client {

    public static void main(String[] args) {
        Builder builder = new MobikeBuilder();
        builder.createBike();
        Directer directer = new Directer(builder);
        Bike bike = directer.construct();
        showBike(bike);
    }

    public static void showBike(Bike bike) {
        System.out.println(bike.getFrame());
        System.out.println(bike.getSeat());
    }
}
