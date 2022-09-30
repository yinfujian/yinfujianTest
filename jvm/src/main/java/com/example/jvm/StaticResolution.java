package com.example.jvm;

/**
 * 静态方法解析
 */
public class StaticResolution {

    public static void sayHello() {
        System.out.println("hello");
    }

    public static void main(String[] args) {
        StaticResolution.sayHello();
    }
}
