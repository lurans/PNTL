package com.huawei.blackhole.network.common.utils;

import org.apache.http.Header;


public class RestClientUtils {

    public static String getAuthToken(Header[] header) {
        if (header != null) {
            for (Header h : header) {
                if (h.getName().equals("X-Auth-Token")) {
                    return h.getValue();
                }
            }
        }

        return null;
    }
}
