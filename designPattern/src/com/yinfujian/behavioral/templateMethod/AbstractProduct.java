package com.yinfujian.behavioral.templateMethod;

public class AbstractProduct {

    private void step1() {
        System.out.println("step 1");
    }

    private void step2() {
        System.out.println("step 2");
    }

    protected void color() {

    }

    public static void make(AbstractProduct product) {
        product.step1();
        product.step2();
        product.color();
    }

    /**
     * 制作一个产品，要分成三步
     * step1 step2 和添加颜色
     * 有三种产品，redProduct blueProduct blackProduct
     * 三种产品都是三步完成，前两步都是一样的，这样就可以把他抽取成公共的，符合抽象类的自下而上的抽象的定义
     *
     * 关于访问控制，step1,step2定义为私有的，只能本类和子类做处理，添加颜色方法，控制在本包内处理
     *
     */
    public static void main(String[] args) {
        // 制作工厂
        make(new BlackProduct());
        make(new RedProduct());
        make(new BlueProduct());

    }

}
