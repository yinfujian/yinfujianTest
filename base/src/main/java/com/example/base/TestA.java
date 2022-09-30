package com.example.base;

public class TestA {

    public static void main(String[] args) {
        int pageSize = 50;
        long totalCount = 251L;
        System.out.println(totalCount%pageSize);

        // 整数相除向上取整
        System.out.println(totalCount/pageSize + (totalCount % pageSize != 0 ? 1 : 0));
    }
}
