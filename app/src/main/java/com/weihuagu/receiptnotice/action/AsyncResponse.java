package com.weihuagu.receiptnotice.action;

import java.util.Map;

public interface AsyncResponse {
    void onDataReceivedSuccess(String[] returnstr);

    void onDataReceivedFailed(String[] returnstr, Map<String, String> postedmap);
}
