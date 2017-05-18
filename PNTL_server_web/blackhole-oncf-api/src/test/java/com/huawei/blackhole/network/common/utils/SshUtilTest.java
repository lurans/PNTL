package com.huawei.blackhole.network.common.utils;

import ch.ethz.ssh2.Connection;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SshUtilTest {
    String ip = "0.0.0.0";
    String user = "user";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        Connection conn = new Connection(ip);
        try {
            conn.connect();
            String[] methods = conn.getRemainingAuthMethods(user);
            for (String method : methods) {
                System.out.println(method);
            }
        } catch (IOException e) {
            e.printStackTrace();
            conn.close();
        } finally {
            conn.close();
        }
    }

}
