package com.demo.wmb.order;

import com.bj58.spat.esbclient.ESBMessage;
import com.bj58.spat.esbclient.ESBReceiveHandler;
import com.bj58.spat.spring.boot.wmb.annotation.WMBConsume;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@WMBConsume(subject = 113272)
public class Consume extends ESBReceiveHandler {
    @Override
    public void messageReceived(ESBMessage msg) {
        log.info("WMB get msg, body is {}", new String(msg.getBody()));
    }

}
