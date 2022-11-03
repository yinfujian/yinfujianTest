package com.yinfujian.creation.prototype;

public class Citation implements Cloneable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Citation clone() throws CloneNotSupportedException {
        return (Citation)super.clone();
    }
}
