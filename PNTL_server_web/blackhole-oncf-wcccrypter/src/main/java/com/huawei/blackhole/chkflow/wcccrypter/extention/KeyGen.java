package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.security.Key;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.wcc.framework.AppRuntimeException;

public class KeyGen {
    public static final String PROP_ITERATION_COUNT = "crypt_keygen_iteration_count";
    protected static final int DEFAULT_ITERATION_COUNT = 50000;

    public static Key genKey(String password, byte[] salt, int keyLength, int iterationCount)
            throws AppRuntimeException {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);

            return factory.generateSecret(keyspec);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    public static int getIterationCount() {
        return AppProperties.getAsInt("crypt_keygen_iteration_count", 50000);
    }
}
