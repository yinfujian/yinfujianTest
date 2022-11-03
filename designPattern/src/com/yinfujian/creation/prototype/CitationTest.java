package com.yinfujian.creation.prototype;

public class CitationTest {

    public static void main(String[] args) throws CloneNotSupportedException {
        Citation citation = new Citation();
        citation.setName("111");
        Citation citation1 = citation.clone();
        citation1.setName("222");
        System.out.println(citation.getName());
        System.out.println(citation1.getName());
    }
}
