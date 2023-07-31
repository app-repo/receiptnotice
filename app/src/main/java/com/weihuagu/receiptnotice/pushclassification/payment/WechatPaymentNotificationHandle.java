package com.weihuagu.receiptnotice.pushclassification.payment;

import android.app.Notification;

import com.weihuagu.receiptnotice.PaymentNotificationHandle;
import com.weihuagu.receiptnotice.action.IDoPost;

import java.util.HashMap;
import java.util.Map;


public class WechatPaymentNotificationHandle extends PaymentNotificationHandle {
    public WechatPaymentNotificationHandle(String pkgtype, Notification notification, IDoPost postpush) {
        super(pkgtype, notification, postpush);
    }

    public void handleNotification() {
        if (title.contains("微信支付") | title.contains("微信收款")) {
            if (content.contains("收款")) {
                Map<String, String> postmap = new HashMap<String, String>();
                postmap.put("type", "wechat");
                postmap.put("time", notitime);
                postmap.put("title", title);
                postmap.put("money", extractMoney(content));
                postmap.put("content", content);
                postpush.doPost(postmap);
                return;
            }
            if (content.contains("二维码赞赏")) {
                Map<String, String> postmap = new HashMap<String, String>();
                postmap.put("type", "wechat-sponsor");
                postmap.put("time", notitime);
                postmap.put("title", title);
                postmap.put("money", extractMoney(content));
                postmap.put("content", content);
                postpush.doPost(postmap);
                return;
            }
        }


    }


}
