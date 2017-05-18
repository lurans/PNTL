package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.exception.CommonException;
import org.junit.Before;
import org.junit.Test;

public class WccCrypterTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testDecryptDataByRootKey() {
        String encodedData = "pass-encripyted";

        try {
            System.out.println("Encoded Data:" + WccCrypter.decryptDataByRootKey(encodedData));
        } catch (CommonException e) {
            e.printStackTrace();
        }
    }
}
