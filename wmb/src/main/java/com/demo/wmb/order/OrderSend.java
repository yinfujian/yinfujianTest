package com.demo.wmb.order;

import com.alibaba.fastjson.JSON;
import com.bj58.spat.esbclient.ESBMessage;
import com.bj58.spat.spring.boot.wmb.annotation.WMBMessage;
import com.bj58.spat.spring.boot.wmb.annotation.WMBSend;
import com.bj58.spat.spring.boot.wmb.enums.SendType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OrderSend {

    @WMBMessage(subject = 113272)
    public String testSend(String body) {
//        log.info("testSend.content-{}", JSON.toJSONString(body));
        return JSON.toJSONString(body);
    }

    @WMBSend(sendType = SendType.Sync)
    public ESBMessage testOrderSend(String body,  int sequenceId) {
        ESBMessage esbMessage = new ESBMessage(113272, body.getBytes(StandardCharsets.UTF_8));
        esbMessage.setSequenceId(sequenceId);
        return esbMessage;
    }

    @WMBMessage(subject = 113272, sequenceKey = "1")
    public String testOrderSend(String body) {
//        log.info("testSend.content-{}", JSON.toJSONString(body));
        return JSON.toJSONString(body);
    }
}
