package com.huawei.blackhole.network.extention.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 876281284704224354L;

    public Map<String, String> createHeader() {
        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-type", "application/json");
        return header;
    }

}
