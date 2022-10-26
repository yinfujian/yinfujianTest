package com.yinfujian.creation.factory.method.fac;

import com.yinfujian.creation.factory.product.AmericanCoffee;
import com.yinfujian.creation.factory.product.Coffee;

public class AmeraicanCoffeeFactory implements CoffeeFactory {

    @Override
    public Coffee createCoffee() {
        return new AmericanCoffee();
    }
}
