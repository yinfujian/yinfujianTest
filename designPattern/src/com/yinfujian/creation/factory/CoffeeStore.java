package com.yinfujian.creation.factory;

import com.yinfujian.creation.factory.abstrac.AmericanDessertFactory;
import com.yinfujian.creation.factory.abstrac.DessertFactory;
import com.yinfujian.creation.factory.product.AmericanCoffee;
import com.yinfujian.creation.factory.product.Coffee;
import com.yinfujian.creation.factory.product.Dessert;

public class CoffeeStore {
    /**
     * 客户点咖啡
     * @return
     */
    public Coffee orderCoffer() {
        // 工厂方法点咖啡
//        CoffeeFactory coffeeFactory = new AmeraicanCoffeeFactory();
//        CoffeeFactory coffeeFactory1 = new LatteCoffeeFactory();
//
//        Coffee ameraicanCoffee = coffeeFactory.createCoffee();
//        Coffee latteCoffee = coffeeFactory1.createCoffee();
//
//        System.out.println(ameraicanCoffee.getName());
//        System.out.println(latteCoffee.getName());
//
        // 抽象工厂点咖啡 和甜点
        DessertFactory americanDessertFactory = new AmericanDessertFactory();
        DessertFactory italyDessertFactory = new AmericanDessertFactory();
        Coffee americanCoffee = americanDessertFactory.createCoffee();
        Dessert dessert = americanDessertFactory.createDessert();
        System.out.println(americanCoffee.getName());
        dessert.show();


        Coffee LatterCoffee = italyDessertFactory.createCoffee();
        Dessert dessert1 = italyDessertFactory.createDessert();
        System.out.println(LatterCoffee.getName());
        dessert1.show();

        return null;
    }

    public static void main(String[] args) {
        new CoffeeStore().orderCoffer();
    }
}
