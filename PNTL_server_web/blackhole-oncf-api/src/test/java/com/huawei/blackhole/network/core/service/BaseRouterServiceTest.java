package com.huawei.blackhole.network.core.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BaseRouterServiceTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void test1() {
        Pattern pattern = Pattern.compile("([0-9]+).([0-9]+).([0-9]+).([0-9]+)");

        String str2 = "1a0.0.0";
        Matcher matcher2 = pattern.matcher(str2);
        Assert.assertTrue(matcher2.find());
    }

    @Test
    public void test2() {
        Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.([0-9]+)");

        String str2 = "0.0.0.0TYFG[]";
        Matcher matcher2 = pattern.matcher(str2);
        Assert.assertFalse(matcher2.matches());
    }

    @Test
    public void test3() {
        Pattern pattern = Pattern.compile("inet ([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+/[0-9]+).*");
        String str1 = "inet 0.0.0.0/24";
        Matcher matcher1 = pattern.matcher(str1);
        Assert.assertTrue(matcher1.find());
        Assert.assertEquals("0.0.0.0/24", matcher1.group(1));
    }

    @Test
    public void test4() {
        Pattern pattern = Pattern.compile("([a-zA-Z]+)://(.+):([0-9]+)");
        String str1 = "https://identity.exapmle.com:443";
        Matcher matcher1 = pattern.matcher(str1);
        Assert.assertTrue(matcher1.find());
    }

}
