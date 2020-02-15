package com.weihuagu.receiptnotice.pushclassification.payment;
import android.app.Notification;

import com.weihuagu.receiptnotice.IDoPost;
import com.weihuagu.receiptnotice.NotificationHandle;

import java.util.Map;
import java.util.HashMap;


public class WechatNotificationHandle extends NotificationHandle {
        public WechatNotificationHandle(String pkgtype, Notification notification, IDoPost postpush){
                super(pkgtype,notification,postpush);
        }

        public void handleNotification(){
                if((title.contains("微信支付")|title.contains("微信收款"))&&content.contains("收款")){
                        Map<String,String> postmap=new HashMap<String,String>();
                                postmap.put("type","wechat");
                                postmap.put("time",notitime);
                                postmap.put("title",title);
                                postmap.put("money",extractMoney(content));
                                postmap.put("content",content);
                                postpush.doPost(postmap);
                                return ;
                }



        }













}