package com.huawei.blackhole.network.common.utils;

import java.util.Map;

public class MapUtils {
    public static String getAsStr(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        return String.valueOf(map.get(key));
    }
}
