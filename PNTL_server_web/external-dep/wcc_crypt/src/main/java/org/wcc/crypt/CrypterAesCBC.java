package org.wcc.crypt;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.wcc.framework.AppProperties;
import org.wcc.framework.AppRuntimeException;

/**
 * 使用AES/CBC方式加解密数据。
 * 
 */
class CrypterAesCBC extends Crypter {
    // 参数下标
    protected static final int PARAM_INDEX_IV = ALGO_PARAM_START;
    protected static final int PARAM_INDEX_SALT = ALGO_PARAM_START + 1;

    // 配置项：IV长度（单位：byte）。可选。默认16字节。虽然Java当前只支持16字节，但是为了以后扩展，也做为可配的
    private static final String PROP_IV_LENGTH = "crypt_aes_cbc_iv_length";
    // 目前IV值只支持128位的
    private static final int DEFAULT_IV_LENGTH = 16;
    // IV长度最小为1，小于1肯定不合法
    private static final int IV_LENGTH_MIN = 1;
    // 配置项：盐值长度（单位：byte）。可选。默认为8字节。
    private static final String PROP_SALT_LENGTH = "crypt_aes_cbc_salt_length";
    // 盐值长度默认为8字节
    private static final int DEFAULT_SALT_LENGTH = 8;
    // 《密码算法应用规范V1.1》 3.5节规定盐值至少为8字节
    private static final int SALT_LENGTH_MIN = 8;

    // 配置项：密钥的长度。可选。默认值为256位，对于AES而言，可选值为128、192和256。该值决定了最终加密使用AES256还是AES128。《密码算法应用规范V1.1》表格1-1推荐使用128位或以上
    private static final String PROP_KEY_LENGTH = "crypt_aes_cbc_key_length";
    private static final String PROP_KEY_LENGTH_OLD = "crypt_keygen_key_length";

    // 密钥的长度（单位：位）
    private static final int KEY_LENGTH_128 = 128;
    private static final int KEY_LENGTH_192 = 192;
    private static final int KEY_LENGTH_256 = 256;
    // 默认的密钥长度为256位，即AES 256
    private static final int DEFAULT_KEY_LENGTH = KEY_LENGTH_256;

    // 加解密算法
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 加密
     * 
     * @param content
     *            明文
     * @param password
     *            密码
     * @return 密文。加密失败返回null
     * @throws AppRuntimeException
     */
    @Override
    public String encrypt(String content, String password) throws AppRuntimeException {
        if (null == content || null == password) {
            throw new AppRuntimeException("content and password should not be null");
        }

        byte[] salt = genSalt();
        byte[] iv = genIV();
        setParam(PARAM_INDEX_IV, iv);
        setParam(PARAM_INDEX_SALT, salt);

        // 生成密钥和IV
        Key key = new SecretKeySpec(KeyGen.genKey(password, salt, getKeyLength(), KeyGen.getIterationCount())
                .getEncoded(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // 加密
        byte[] ecypted = null;
        try {
            ecypted = doEncrypt(content.getBytes("UTF-8"), key, ivSpec);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }

        return EncryptHelper.parseByte2HexStr(ecypted);
    }

    /**
     * 解密
     * 
     * @param content
     *            密文
     * @param password
     *            密码
     * @return 明文。解密失败返回null
     * @throws AppRuntimeException
     */
    @Override
    public String decrypt(String content, String password) throws AppRuntimeException {
        if (null == content || null == password) {
            throw new AppRuntimeException("content and password should not be null");
        }

        byte[] iv = getParam(PARAM_INDEX_IV);
        byte[] salt = getParam(PARAM_INDEX_SALT);
        try {
            int iterationCount = Integer.parseInt(new String(getParam(PARAM_INDEX_KEYGEN_ITERATION_COUNT), "UTF-8"));
            Key key = new SecretKeySpec(KeyGen.genKey(password, salt, getKeyLength(), iterationCount).getEncoded(),
                    "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            return new String(doDecrypt(EncryptHelper.parseHexStr2Byte(content), key, ivSpec), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 使用根密钥加密
     * 
     * @param content
     *            明文
     * @return 密文。加密失败返回null
     * @throws AppRuntimeException
     */
    public String encryptByRootKey(String content) throws AppRuntimeException {
        return encryptByRootKey(content, new RootKey(getKeyLength(), KeyGen.getIterationCount()).getKey());
    }

    /**
     * 使用根密钥解密
     * 
     * @param content
     *            密文
     * @return 明文。解密失败返回null
     * @throws AppRuntimeException
     */
    public String decryptByRootKey(String content) throws AppRuntimeException {
        try {
            int iterationCount = Integer.parseInt(new String(getParam(PARAM_INDEX_KEYGEN_ITERATION_COUNT), "UTF-8"));
            return decryptByRootKey(content, new RootKey(getKeyLength(), iterationCount).getKey());
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 使用根密钥加密
     * 
     * @param content
     *            明文 rootKey 根密钥
     * @return 密文。加密失败返回null
     * @throws AppRuntimeException
     */
    @Override
    protected String encryptByRootKey(String content, Key rootKey) throws AppRuntimeException {
        if (null == content) {
            throw new AppRuntimeException("content should not be null");
        }

        try {
            byte[] iv = genIV();
            setParam(PARAM_INDEX_IV, iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            rootKey = new SecretKeySpec(rootKey.getEncoded(), "AES");

            byte[] ecypted = doEncrypt(content.getBytes("UTF-8"), rootKey, ivSpec);
            return EncryptHelper.parseByte2HexStr(ecypted);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 使用根密钥解密
     * 
     * @param content
     *            密文 rootKey 根密钥
     * @return 明文。解密失败返回null
     * @throws AppRuntimeException
     */
    @Override
    protected String decryptByRootKey(String content, Key rootKey) throws AppRuntimeException {
        if (null == content) {
            throw new AppRuntimeException("content should not be null");
        }

        try {
            int ivLen = AppProperties.getAsInt(PROP_IV_LENGTH, DEFAULT_IV_LENGTH);
            if (ivLen < IV_LENGTH_MIN) {
                throw new AppRuntimeException("Config Error. IV_LENGTH > " + IV_LENGTH_MIN);
            }

            byte[] iv = getParam(PARAM_INDEX_IV);
            byte[] ecyptContent = EncryptHelper.parseHexStr2Byte(content);

            // 生成根密钥
            rootKey = new SecretKeySpec(rootKey.getEncoded(), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            return new String(doDecrypt(ecyptContent, rootKey, ivSpec), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 使用指定密钥进行AES加密
     * 
     * @param content
     *            明文。调用者应保证此参数不是null
     * @param key
     *            密钥
     * @param iv
     *            向量对象
     * @return byte[] 密文
     * @throws AppRuntimeException
     */
    private byte[] doEncrypt(byte[] content, Key key, IvParameterSpec iv) throws AppRuntimeException {
        try {
            // 创建密码器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 初始化
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            // 加密
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 用指定密钥AES解密
     * 
     * @param content
     *            密文。调用者应保证此参数不是null
     * @param key
     *            密钥
     * @param iv
     *            向量对象
     * @return 明文
     * @throws AppRuntimeException
     */
    private byte[] doDecrypt(byte[] content, Key key, IvParameterSpec iv) throws AppRuntimeException {
        try {
            // 创建密码器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            // 解密
            return cipher.doFinal(content);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 生成IV
     * 
     * @return IV
     * @throws AppRuntimeException
     */
    private byte[] genIV() throws AppRuntimeException {
        try {
            int length = AppProperties.getAsInt(PROP_IV_LENGTH, DEFAULT_IV_LENGTH);
            if (length < IV_LENGTH_MIN) {
                throw new AppRuntimeException("Config Error. IV_LENGTH > " + IV_LENGTH_MIN);
            }

            byte[] iv = new byte[length];
            // 《Web应用安全开发规范V2.0》 建议3.1.1 建议这样使用随机数
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            rand.nextBytes(iv);

            return iv;
        } catch (NoSuchAlgorithmException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 生成盐值
     * 
     * @return 盐值
     * @throws AppRuntimeException
     */
    private byte[] genSalt() throws AppRuntimeException {
        try {
            int length = AppProperties.getAsInt(PROP_SALT_LENGTH, DEFAULT_SALT_LENGTH);
            if (length < SALT_LENGTH_MIN) {
                throw new AppRuntimeException("Config Error. SALT_LENGTH > " + SALT_LENGTH_MIN);
            }

            byte[] salt = new byte[length];
            // 《Web应用安全开发规范V2.0》 建议3.1.1 建议这样使用随机数
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            rand.nextBytes(salt);

            return salt;
        } catch (NoSuchAlgorithmException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 
     * 获取密钥长度
     * 
     * @return 密钥长度
     */
    private int getKeyLength() {
        int keyLength = -1;
        String key = null;

        key = AppProperties.get(PROP_KEY_LENGTH);
        if (null == key) {
            key = AppProperties.get(PROP_KEY_LENGTH_OLD);
            if (null == key) {
                return DEFAULT_KEY_LENGTH;
            }
        }

        try {
            keyLength = Integer.parseInt(key);
        } catch (NumberFormatException ex) {
            throw new AppRuntimeException("NumberFormatException. Please check config: " + PROP_KEY_LENGTH);
        }

        if (keyLength != KEY_LENGTH_128 && keyLength != KEY_LENGTH_192 && keyLength != KEY_LENGTH_256) {
            throw new AppRuntimeException("Config Error. Key Length should be 128, 192 or 256");
        }

        return keyLength;
    }
}