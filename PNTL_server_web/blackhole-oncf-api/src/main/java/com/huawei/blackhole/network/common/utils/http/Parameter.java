package com.huawei.blackhole.network.common.utils.http;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


/**
 * 参数
 **/
public class Parameter {

    private Map<String, String> parameter = new HashMap<String, String>();

    /**
     * put
     *
     * @param key   String
     * @param value String
     * @return this
     **/
    public Parameter put(String key, String value) {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return this;
        }
        parameter.put(key, value);
        return this;
    }

    /**
     * get
     *
     * @param key String
     * @return parameter
     **/
    public String get(String key) {
        return parameter.get(key);
    }

    public boolean isEmpty() {
        return parameter == null || parameter.size() == 0;
    }

    /**
     * toString
     *
     * @return toString
     **/
    @Override
    public String toString() {
        if (isEmpty()) {
            return StringUtils.EMPTY;
        }

        List<String> paramList = new ArrayList<String>();
        for (Entry<String, String> entry : parameter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            paramList.add(key + "=" + value);
        }
        return StringUtils.join(paramList, "&");
    }

}
