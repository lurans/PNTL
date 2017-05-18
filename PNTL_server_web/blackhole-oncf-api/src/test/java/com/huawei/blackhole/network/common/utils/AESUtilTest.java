package com.huawei.blackhole.network.common.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class AESUtilTest {

    @Test
    public void test() {
        StringBuffer buf = new StringBuffer(AESUtil.getFcKey());            
        while (buf.length() < 32) {
            buf.append(AESUtil.getFcKey());
        }
        String newkey = buf.toString().substring(0, 32);
        System.out.println("newkey=" + newkey);
        
        String oldkey = "crypt key";
        while (oldkey.length() < 32) {
            oldkey += oldkey;
        }
        oldkey = oldkey.substring(0, 32);
        System.out.println("oldkey=" + oldkey);
        
        assertEquals(newkey, oldkey);
    }

}
