package com.huawei.blackhole.chkflow.wcccrypter.extention;

import org.wcc.framework.AppRuntimeException;

public class CrypterFactory {
    public static final String AES_CBC = "AES_CBC";

    public static Crypter getCrypter(String algorithm) throws AppRuntimeException {
        Crypter crypter = null;
        if (null == algorithm) {
            throw new AppRuntimeException("Algorithm Should not be null");
        }
        if (algorithm.equals("AES_CBC")) {
            crypter = new CrypterAesCBC();
        } else {
            throw new AppRuntimeException("Unsupported Crypter Algorithm: " + algorithm);
        }
        return new CrypterProxy(algorithm, crypter, RootKeyUpdater.getInstance(), new FormatterV1());
    }
}
