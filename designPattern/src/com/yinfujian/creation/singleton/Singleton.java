package com.yinfujian.creation.singleton;

/**
 * 饿汉式
 */
public class Singleton {

    public static Singleton singleton = new Singleton();

    public static Singleton getInstance() {
        return singleton;
    }

    private Singleton() {

    }

}
