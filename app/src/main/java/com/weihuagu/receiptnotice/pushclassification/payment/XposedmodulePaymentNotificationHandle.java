package com.weihuagu.receiptnotice.pushclassification.payment;

import android.app.Notification;

import com.weihuagu.receiptnotice.PaymentNotificationHandle;
import com.weihuagu.receiptnotice.action.IDoPost;

import java.util.HashMap;
import java.util.Map;


public class XposedmodulePaymentNotificationHandle extends PaymentNotificationHandle {
    public XposedmodulePaymentNotificationHandle(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    public void handleNotification() {
        if (content.contains("微信支付") && content.contains("收款")) {
            Map<String, String> postmap = new HashMap<String, String>();
            postmap.put("type", "wechat");
            postmap.put("time", notitime);
            postmap.put("title", "微信支付");
            postmap.put("money", extractMoney(content));
            postmap.put("content", content);
            postpush.doPost(postmap);
            return;
        }


    }


}
