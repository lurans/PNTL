package com.huawei.blackhole.network.common.utils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.huawei.blackhole.network.common.exception.ConfigConflictException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;

public class ConfUtilTest {
    private static ConfUtil CONF;

    @Before
    public void setUp() throws Exception {
        CONF = ConfUtil.getInstance();
    }

    @Test
    public void testConfLost() {
        String str = null;
        try {
            str = CONF.getConfAsString("not_exsit");
        } catch (ConfigLostException e) {
            // do nothing
        }
        Assert.assertNull(str);
    }

    @Test
    public void testRegConfAsString() {
        String key = "test";
        String value = "testValue";
        try {
            CONF.regConfAsString(key, value);
        } catch (ConfigConflictException e) {
            Assert.fail(String.format("conf %s should not exist", key));
        }
        try {
            Assert.assertEquals(value, CONF.getConfAsString(key));
        } catch (ConfigLostException e) {
            Assert.fail(String.format("conf %s should exist", key));
        }
        try {
            CONF.regConfAsString(key, value);
        } catch (ConfigConflictException e) {
            // do nothing
            System.out.printf(String.format("%s has been exist", key));
        }
    }

    @Test
    public void testRegConfAsMap() {
        String key = "testMap";
        Map<String, String> obj = new HashMap<>();
        String k = "nova_url";
        String v = "http://nova.com:443";
        obj.put(k, v);

        try {
            CONF.regConfAsMap(key, obj);
        } catch (ConfigConflictException e) {
            Assert.fail(String.format("conf %s should not exist", key));
        }
        try {
            Assert.assertEquals(v, CONF.getConfAsMap(key).get(k));
        } catch (ConfigLostException e) {
            Assert.fail(String.format("conf %s.%s should exist", key, k));
        }
    }
}
