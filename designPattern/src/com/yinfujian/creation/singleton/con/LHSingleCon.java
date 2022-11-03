package com.yinfujian.creation.singleton.con;

/**
 * 饿汉式为内存预加载方式，不存在并发的问题，但上来就创建，浪费了内存
 * 懒汉式解决了内存浪费的问题，但是出现了并发创建对象的问题，没办法实现只有全局访问点，访问的是同一个对象
 *      并发分析，TODO 暂时不分析，等待并发时一同解决
 */
public class LHSingleCon {

}
