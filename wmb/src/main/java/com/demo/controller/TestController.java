package com.demo.controller;

import com.demo.wmb.order.OrderSend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @Autowired
    private OrderSend orderSend;

    @ResponseBody
    @RequestMapping("/test")
    public String test() {
//        for (int i = 0; i < 100000; i++) {
//            orderSend.testSend(i + "");
//        }
        for (int i = 0; i < 100000; i++) {
            orderSend.testOrderSend(i + "", 100);
        }
//        for (int i = 0; i < 100000; i++) {
//            orderSend.testOrderSend(i + "");
//        }
        return "test";
    }
}
