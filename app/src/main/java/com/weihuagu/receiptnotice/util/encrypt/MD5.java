package com.weihuagu.receiptnotice.util.encrypt;

import com.weihuagu.receiptnotice.util.ByteUtil;
import com.weihuagu.receiptnotice.util.LogUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class MD5 extends Encrypter {
    public MD5(String key) {
        super(key);
    }

    public MD5() {

    }

    public String getTimestamp() { //生成时间戳
        long timestampLong = System.currentTimeMillis();
        String timestampStr = String.valueOf(timestampLong);
        return timestampStr;
    }

    public Map<String, String> transferMapValue(Map<String, String> params) {
        if (params.get("type") != null && params.get("money") != null && key != null) {
            Map<String, String> postmap = new HashMap<String, String>();
            postmap.putAll(params);
            String nonce = getMd5String(getTimestamp() + "|" + java.util.UUID.randomUUID().toString());
            postmap.put("nonce", nonce);
            postmap.put("sign", getSignMd5WithSecretkey(params.get("type"), params.get("money"), params.get("time"), nonce, key));
            /*
            LogUtil.debugLogWithJava("调试，sign md5");
            LogUtil.debugLogWithJava("加密后的map");
            LogUtil.debugLogWithJava(postmap.toString());
            // */
            return postmap;
        } else
            return params;


    }


    public String getMd5String(String str) {
        try {
            byte[] bytesOfMessage = str.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            return ByteUtil.parseByte2HexStr(thedigest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            return null;
        }

    }

    public String getSignMd5(String type, String price) {
        String md5str = getMd5String(getMd5String(type + price));
        LogUtil.debugLog("md5 string: type is" + type + " price is " + price + "   md5str:" + md5str);
        return md5str;
    }

    public String getSignMd5WithSecretkey(String type, String price, String time, String nonce, String secretkey) {
        String md5str = getMd5String(getMd5String(type + price + time + nonce) + secretkey);
        return md5str;
    }


}
