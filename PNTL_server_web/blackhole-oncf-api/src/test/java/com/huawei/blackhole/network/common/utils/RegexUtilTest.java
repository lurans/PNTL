package com.huawei.blackhole.network.common.utils;

import org.junit.Assert;
import org.junit.Test;

public class RegexUtilTest {
    @Test
    public void testValidAuthUrl() {
        Assert.assertTrue(!RegexUtil.validAuthUrl(null));
        Assert.assertTrue(!RegexUtil.validAuthUrl(""));
        Assert.assertTrue(RegexUtil.validAuthUrl("http://195.yes.com:123"));

        Assert.assertTrue(!RegexUtil.validAuthUrl("://jiji.com:123"));
        Assert.assertTrue(!RegexUtil.validAuthUrl("//jiji.com:123"));
        Assert.assertTrue(!RegexUtil.validAuthUrl(":/jiji.com:123"));
        Assert.assertTrue(!RegexUtil.validAuthUrl("http://195.yes.com"));
        Assert.assertTrue(!RegexUtil.validAuthUrl("http://195.yes.com:"));
        Assert.assertTrue(!RegexUtil.validAuthUrl("http::"));
        Assert.assertTrue(!RegexUtil.validAuthUrl("iamworng"));
    }
}
