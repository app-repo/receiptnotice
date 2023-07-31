package com.weihuagu.receiptnotice;

import com.google.gson.Gson;
import com.weihuagu.receiptnotice.util.DataBaseHolder;

import java.util.Map;

public class OnlyWriteToDatabase {
    DataBaseHolder database = DataBaseHolder.getInstance();

    public void onePostWriteToDatabase(String postjson) {
        Gson gson = new Gson();
        Map<String, String> postmap = gson.fromJson(postjson, Map.class);
        String type = postmap.get("type");
        String time = postmap.get("time");
        String title = postmap.get("title");
        String money = postmap.get("money");
        String content = postmap.get("content");
        checkHavePlatName(type);

    }


    public boolean checkHavePlatName(String plattype) {
        database.sqliteDatabase.query("plat", new String[]{"name"}, plattype, null, null, null, null);
        return false;
    }

}
