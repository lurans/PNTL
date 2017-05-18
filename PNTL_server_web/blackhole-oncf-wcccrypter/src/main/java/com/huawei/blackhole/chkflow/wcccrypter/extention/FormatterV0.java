package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.wcc.framework.AppRuntimeException;

class FormatterV0 extends Formatter {
    public static final String PROP_IV_LENGTH = "crypt_aes_cbc_iv_length";
    public static final String PROP_SALT_LENGTH = "crypt_aes_cbc_salt_length";
    private static final int DEFAULT_IV_LENGTH = 16;
    private static final int IV_LENGTH_MIN = 1;
    private static final int DEFAULT_SALT_LENGTH = 8;
    private static final int SALT_LENGTH_MIN = 8;
    private boolean encByRootKey = false;

    public List<byte[]> parse(String formatted) {
        if ((null == formatted) || (formatted.length() == 0)) {
            return null;
        }
        int ivLen = AppProperties.getAsInt("crypt_aes_cbc_iv_length", 16);
        if (ivLen < 1) {
            throw new AppRuntimeException("Config Error. IV_LENGTH > 1");
        }
        int saltLen = AppProperties.getAsInt("crypt_aes_cbc_salt_length", 8);
        if (saltLen < 8) {
            throw new AppRuntimeException("Config Error. SALT_LENGTH > 8");
        }
        byte[] decoded = Base64.decodeBase64(formatted);
        byte[] iv = Arrays.copyOfRange(decoded, 0, ivLen);
        byte[] salt = null;
        byte[] ecyptContent = null;
        if (this.encByRootKey) {
            ecyptContent = Arrays.copyOfRange(decoded, ivLen, decoded.length);
        } else {
            salt = Arrays.copyOfRange(decoded, ivLen, ivLen + saltLen);
            ecyptContent = Arrays.copyOfRange(decoded, ivLen + saltLen, decoded.length);
        }
        List<byte[]> result = new LinkedList();
        try {
            result.add("AES_CBC".getBytes("UTF-8"));

            result.add(EncryptHelper.parseByte2HexStr(ecyptContent).getBytes("UTF-8"));

            result.add(String.valueOf(Long.MAX_VALUE).getBytes("UTF-8"));

            result.add(String.valueOf(50000).getBytes("UTF-8"));

            result.add(iv);

            result.add(salt);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
        return result;
    }

    public void setEncByRootKey(boolean encByRootKey) {
        this.encByRootKey = encByRootKey;
    }
}
