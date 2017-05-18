package com.huawei.blackhole.chkflow.wcccrypter.extention;

import org.wcc.framework.AppRuntimeException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CrypterAesCBC extends Crypter {

    protected static final int PARAM_INDEX_IV = 4;
    protected static final int PARAM_INDEX_SALT = 5;
    private static final String PROP_IV_LENGTH = "crypt_aes_cbc_iv_length";
    private static final int DEFAULT_IV_LENGTH = 16;
    private static final int IV_LENGTH_MIN = 1;
    private static final String PROP_SALT_LENGTH = "crypt_aes_cbc_salt_length";
    private static final int DEFAULT_SALT_LENGTH = 8;
    private static final int SALT_LENGTH_MIN = 8;
    private static final String PROP_KEY_LENGTH = "crypt_aes_cbc_key_length";
    private static final String PROP_KEY_LENGTH_OLD = "crypt_keygen_key_length";
    private static final int KEY_LENGTH_128 = 128;
    private static final int KEY_LENGTH_192 = 192;
    private static final int KEY_LENGTH_256 = 256;
    private static final int DEFAULT_KEY_LENGTH = 256;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public String encrypt(String content, String password)
            throws AppRuntimeException {
        if ((null == content) || (null == password)) {
            throw new AppRuntimeException("content and password should not be null");
        }
        byte[] salt = genSalt();
        byte[] iv = genIV();
        setParam(4, iv);
        setParam(5, salt);

        Key key = new SecretKeySpec(KeyGen.genKey(password, salt, getKeyLength(), KeyGen.getIterationCount()).getEncoded(), "AES");

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        byte[] ecypted = null;
        try {
            ecypted = doEncrypt(content.getBytes("UTF-8"), key, ivSpec);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
        return EncryptHelper.parseByte2HexStr(ecypted);
    }

    public String decrypt(String content, String password)
            throws AppRuntimeException {
        if ((null == content) || (null == password)) {
            throw new AppRuntimeException("content and password should not be null");
        }
        byte[] iv = getParam(4);
        byte[] salt = getParam(5);
        try {
            int iterationCount = Integer.parseInt(new String(getParam(3), "UTF-8"));
            Key key = new SecretKeySpec(KeyGen.genKey(password, salt, getKeyLength(), iterationCount).getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            return new String(doDecrypt(EncryptHelper.parseHexStr2Byte(content), key, ivSpec), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    public String encryptByRootKey(String content)
            throws AppRuntimeException {
        return encryptByRootKey(content, new RootKey(getKeyLength(), KeyGen.getIterationCount()).getKey());
    }

    public String decryptByRootKey(String content)
            throws AppRuntimeException {
        try {
            int iterationCount = Integer.parseInt(new String(getParam(3), "UTF-8"));
            return decryptByRootKey(content, new RootKey(getKeyLength(), iterationCount).getKey());
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    protected String encryptByRootKey(String content, Key rootKey)
            throws AppRuntimeException {
        if (null == content) {
            throw new AppRuntimeException("content should not be null");
        }
        try {
            byte[] iv = genIV();
            setParam(4, iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            rootKey = new SecretKeySpec(rootKey.getEncoded(), "AES");

            byte[] ecypted = doEncrypt(content.getBytes("UTF-8"), rootKey, ivSpec);
            return EncryptHelper.parseByte2HexStr(ecypted);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    protected String decryptByRootKey(String content, Key rootKey)
            throws AppRuntimeException {
        if (null == content) {
            throw new AppRuntimeException("content should not be null");
        }
        try {
            int ivLen = AppProperties.getAsInt("crypt_aes_cbc_iv_length", 16);
            if (ivLen < 1) {
                throw new AppRuntimeException("Config Error. IV_LENGTH > 1");
            }
            byte[] iv = getParam(4);
            byte[] ecyptContent = EncryptHelper.parseHexStr2Byte(content);

            rootKey = new SecretKeySpec(rootKey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            return new String(doDecrypt(ecyptContent, rootKey, ivSpec), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    private byte[] doEncrypt(byte[] content, Key key, IvParameterSpec iv)
            throws AppRuntimeException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(1, key, iv);

            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    private byte[] doDecrypt(byte[] content, Key key, IvParameterSpec iv)
            throws AppRuntimeException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            cipher.init(2, key, iv);

            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    private byte[] genIV()
            throws AppRuntimeException {
        try {
            int length = AppProperties.getAsInt("crypt_aes_cbc_iv_length", 16);
            if (length < 1) {
                throw new AppRuntimeException("Config Error. IV_LENGTH > 1");
            }
            byte[] iv = new byte[length];

            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            rand.nextBytes(iv);

            return iv;
        } catch (NoSuchAlgorithmException e) {
            throw new AppRuntimeException(e);
        }
    }

    private byte[] genSalt()
            throws AppRuntimeException {
        try {
            int length = AppProperties.getAsInt("crypt_aes_cbc_salt_length", 8);
            if (length < 8) {
                throw new AppRuntimeException("Config Error. SALT_LENGTH > 8");
            }
            byte[] salt = new byte[length];

            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            rand.nextBytes(salt);

            return salt;
        } catch (NoSuchAlgorithmException e) {
            throw new AppRuntimeException(e);
        }
    }

    private int getKeyLength() {
        int keyLength = -1;
        String key = null;

        key = AppProperties.get("crypt_aes_cbc_key_length");
        if (null == key) {
            key = AppProperties.get("crypt_keygen_key_length");
            if (null == key) {
                return 256;
            }
        }
        try {
            keyLength = Integer.parseInt(key);
        } catch (NumberFormatException ex) {
            throw new AppRuntimeException("NumberFormatException. Please check config: crypt_aes_cbc_key_length");
        }
        if ((keyLength != 128) && (keyLength != 192) && (keyLength != 256)) {
            throw new AppRuntimeException("Config Error. Key Length should be 128, 192 or 256");
        }
        return keyLength;
    }

}
