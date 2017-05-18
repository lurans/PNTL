package com.huawei.blackhole.chkflow.wcccrypter;

import com.huawei.blackhole.chkflow.wcccrypter.extention.Crypter;
import com.huawei.blackhole.chkflow.wcccrypter.extention.CrypterFactory;

public class WccCrypter {
    private static final String ROOT_KEY = "FusionNetwork";

    /**
     * Encrypt data by specify key.
     *
     * @param data
     *            To be encoded data.
     * @param key
     *            Key to be used in encryption.
     * @return Encrypted data.
     * @throws CommonException
     */
    public static String encryptData(final String data, final String key) throws WccException {
        if (isEmpty(data) || isEmpty(key)) {
            throw new WccException("data or key is null");
        }

        try {
            Crypter crypter = CrypterFactory.getCrypter(CrypterFactory.AES_CBC);
            String encodedData = crypter.encrypt(data, key);
            return encodedData;
        } catch (Exception e) {
            throw new WccException(String.format("encrypt data fail: %s", e.getLocalizedMessage()));
        }
    }

    /**
     * Encrypt data by internal key.
     *
     * @param data
     *            To be encoded data.
     * @return Encrypted data.
     * @throws CommonException
     */
    public static String encryptDataByRootKey(final String data) throws WccException {
        return encryptData(data, ROOT_KEY);
    }

    private static boolean isEmpty(String str) {
        if (null == str || 0 == str.length()) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("invalid args num");
            System.exit(1);
        }
        String data = args[0];
        try {
            System.out.println(encryptDataByRootKey(data));
        } catch (WccException e) {
            System.out.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
