package com.yinfujian.creation.factory.abstrac;

import com.yinfujian.creation.factory.product.Coffee;
import com.yinfujian.creation.factory.product.Dessert;

public interface DessertFactory {

    Coffee createCoffee();

    Dessert createDessert();
}
