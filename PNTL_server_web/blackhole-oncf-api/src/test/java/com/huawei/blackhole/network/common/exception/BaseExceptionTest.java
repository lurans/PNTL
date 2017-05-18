package com.huawei.blackhole.network.common.exception;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BaseExceptionTest {
    @Test
    public void test() {
        BaseException baseException = new ApplicationException("type", "err msg");
        assertEquals("type: err msg", baseException.toString());
    }
}
