package com.weihuagu.receiptnotice;

import android.app.Notification;
import android.provider.Telephony.Sms;

import com.weihuagu.receiptnotice.action.IDoPost;
import com.weihuagu.receiptnotice.pushclassification.payment.AlipayPaymentNotificationHandle;
import com.weihuagu.receiptnotice.pushclassification.payment.BanksProxy;
import com.weihuagu.receiptnotice.pushclassification.payment.CashbarPaymentNotificationHandle;
import com.weihuagu.receiptnotice.pushclassification.payment.IcbcelifePaymentNotificationHandle;
import com.weihuagu.receiptnotice.pushclassification.payment.MipushPaymentNotificationHandle;
import com.weihuagu.receiptnotice.pushclassification.payment.UnionpayPaymentNotificationHandle;
import com.weihuagu.receiptnotice.pushclassification.payment.WechatPaymentNotificationHandle;
import com.weihuagu.receiptnotice.pushclassification.payment.XposedmodulePaymentNotificationHandle;

public class NotificationHandleFactory {
    public PaymentNotificationHandle getNotificationHandle(String pkg, Notification notification, IDoPost postpush) {
        //mipush
        if ("com.xiaomi.xmsf".equals(pkg)) {
            return new MipushPaymentNotificationHandle("com.xiaomi.xmsf", notification, postpush);
        }
        //支付宝
        if ("com.eg.android.AlipayGphone".equals(pkg)) {
            return new AlipayPaymentNotificationHandle("com.eg.android.AlipayGphone", notification, postpush);
        }

        //应用管理GCM代收
        if ("android".equals(pkg)) {
            return new XposedmodulePaymentNotificationHandle("github.tornaco.xposedmoduletest", notification, postpush);
        }
        //微信
        if ("com.tencent.mm".equals(pkg)) {
            return new WechatPaymentNotificationHandle("com.tencent.mm", notification, postpush);
        }
        //收钱吧
        if ("com.wosai.cashbar".equals(pkg)) {
            return new CashbarPaymentNotificationHandle("com.wosai.cashbar", notification, postpush);
        }
        //云闪付
        if ("com.unionpay".equals(pkg)) {
            return new UnionpayPaymentNotificationHandle("com.unionpay", notification, postpush);
        }
        //工银商户之家
        if ("com.icbc.biz.elife".equals(pkg)) {
            return new IcbcelifePaymentNotificationHandle("com.icbc.biz.elife", notification, postpush);
        }
        //接到短信
        if (getMessageAppPkg().equals(pkg)) {
            return new BanksProxy(getMessageAppPkg(), notification, postpush);
        }


        return null;

    }

    private String getMessageAppPkg() {
        return Sms.getDefaultSmsPackage(MainApplication.getAppContext());

    }

}


