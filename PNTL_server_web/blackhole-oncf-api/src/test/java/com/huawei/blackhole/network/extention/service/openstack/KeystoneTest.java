package com.huawei.blackhole.network.extention.service.openstack;

import org.junit.Before;
import org.junit.Test;

import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;

public class KeystoneTest extends Keystone {

    Keystone keystone = null;

    @Before
    public void setUp() throws Exception {
        keystone = new Keystone();
    }

    @Test
    public void test() {
        String token = null;
        try {
            token = keystone.getToken();
            System.out.println(token);
        } catch (ClientException | ConfigLostException e) {
            e.printStackTrace();
        }
    }

}
