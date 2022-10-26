package com.yinfujian.creation.factory.method.fac;

import com.yinfujian.creation.factory.product.Coffee;
import com.yinfujian.creation.factory.product.LatteCoffee;

public class LatteCoffeeFactory implements CoffeeFactory{

    public Coffee createCoffee() {
        return new LatteCoffee();
    }
}
