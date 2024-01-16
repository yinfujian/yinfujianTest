package com.yinfujian.behavioral.observer;

import java.util.HashSet;
import java.util.Set;

/**
 * 观察者模式，原需求是发生错误，则推送消息，推送消息的方式包括，邮件，短信，微信，
 */
public class Observer {

    static class App {
        //        MailSend mailSend = new MailSend();
//        WechatSend wechatSend = new WechatSend();
        Set<send> set = new HashSet<>();

        public void error() {
            String error = "发生错误";
//            mailSend.sendMsg(error);
//            wechatSend.sendMsg(error);
            for (send send : set) {
                send.sendMsg(error);

            }
        }

        public void addSend(send send) {
            set.add(send);
        }
    }

    // 抽象一个接口出来
    interface send  {
        void sendMsg(String msg);
    }

    static class MailSend implements send{

        public void sendMsg(String msg) {
            System.out.println("send mail:" + msg);
        }
    }

    static class WechatSend implements send{

        public void sendMsg(String msg) {
            System.out.println("send wechat:" + msg);
        }
    }

    public static void main(String[] args) {
        App app = new App();
        app.addSend(new MailSend());
        app.addSend(new WechatSend());
        app.error();

    }
}
