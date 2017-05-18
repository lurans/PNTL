package com.huawei.blackhole.network.extention.service.wcc;

import com.huawei.blackhole.network.common.utils.WccCrypter;
import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {
    @Test
    public void testEnDecrypt() throws Exception {
        String orginCode = "xxx";
        String encode = WccCrypter.encryptDataByRootKey(orginCode);
        String actual = WccCrypter.decryptDataByRootKey(encode);
        Assert.assertEquals(orginCode, actual);
    }
}
