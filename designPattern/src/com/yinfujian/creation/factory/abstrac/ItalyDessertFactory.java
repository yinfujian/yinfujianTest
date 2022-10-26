package com.yinfujian.creation.factory.abstrac;

import com.yinfujian.creation.factory.product.Coffee;
import com.yinfujian.creation.factory.product.Dessert;
import com.yinfujian.creation.factory.product.LatteCoffee;
import com.yinfujian.creation.factory.product.Tiramisu;

public class ItalyDessertFactory implements DessertFactory {


    @Override
    public Coffee createCoffee() {
        return new LatteCoffee();
    }

    @Override
    public Dessert createDessert() {
        return new Tiramisu();
    }
}
