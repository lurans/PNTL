package com.huawei.blackhole.network.core.service;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RouterServiceImplTest {

    private static ApplicationContext context;

    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext(new String[] { "classpath*:*.service.xml",
                "classpath*:*.datasource.xml" });
    }

    @Test
    public void test() {
        System.out.println(context);
    }

}
