package com.yinfujian.creation.singleton;

/**
 * 懒汉式
 */
public class LHSingle {

    private static LHSingle instance = null;

    private LHSingle() {

    }

    public static LHSingle getInstance() {
        if (instance == null) {
            instance = new LHSingle();
        }
        return instance;
    }
}
