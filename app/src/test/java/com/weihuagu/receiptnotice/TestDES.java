package com.weihuagu.receiptnotice;
import com.weihuagu.receiptnotice.util.LogUtil;
import com.weihuagu.receiptnotice.util.encrypt.DES;
import com.weihuagu.receiptnotice.util.encrypt.MD5;
import junit.framework.TestCase;

import org.junit.Test;

import java.util.Map;
import java.util.HashMap;
public class TestDES extends TestCase{
    DES des=null;
    protected void setUp() throws Exception {
        super.setUp();
        des =new DES("12345678");
    }

    @Test
    public void testTransferMapValue() throws Exception{
        Map<String,String> postmap=new HashMap<String,String>();
                                postmap.put("time","2090 12 04");
                                postmap.put("title","支付宝支付");
                                postmap.put("money","345.56");
                                postmap.put("content","测试des伪造的");
    
        des.transferMapValue(postmap);
    }



    @Test
    public void testMD5() throws Exception {
        MD5 md5 = new MD5("122345566");
        Map<String,String> postmap=new HashMap<String,String>();
        postmap.put("time","2090 12 04");
        postmap.put("title","支付宝支付");
        postmap.put("money","0.01");
        postmap.put("type","alipay");
        postmap.put("content","测试md5伪造的");
        md5.transferMapValue(postmap);
        String sign = md5.getMd5String("alipay0.01");
        LogUtil.debugLogWithJava(sign);
    }

}
