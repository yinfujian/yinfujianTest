package com.yinfujian.creation.factory.abstrac;

import com.yinfujian.creation.factory.product.AmericanCoffee;
import com.yinfujian.creation.factory.product.Coffee;
import com.yinfujian.creation.factory.product.Dessert;
import com.yinfujian.creation.factory.product.MatchMousse;

public class AmericanDessertFactory implements DessertFactory {


    @Override
    public Coffee createCoffee() {
        return new AmericanCoffee();
    }

    @Override
    public Dessert createDessert() {
        return new MatchMousse();
    }
}
