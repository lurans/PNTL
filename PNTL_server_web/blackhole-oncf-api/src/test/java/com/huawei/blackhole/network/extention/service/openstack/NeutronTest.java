package com.huawei.blackhole.network.extention.service.openstack;

import org.junit.Before;
import org.junit.Test;

import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;

public class NeutronTest extends Neutron {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetVmPorts() {

        Keystone identityWrapperService = new Keystone();

        String token = null;
        try {
            token = identityWrapperService.getToken();
            System.out.println(token);
        } catch (ClientException | ConfigLostException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testGetVmRouters() {

        Keystone identityWrapperService = new Keystone();

        String token = null;
        try {
            token = identityWrapperService.getToken();
            System.out.println(token);
        } catch (ClientException | ConfigLostException e) {
            e.printStackTrace();
        }

    }

}
