package org.wcc.crypt;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.wcc.framework.AppProperties;
import org.wcc.framework.AppRuntimeException;

/**
 * 加密结果解析，V0版本（wcc_crypt-1.0.0 AesCBC对应的密文格式）
 * 
 * 该类只用于解析使用V0版本格式的密文，不用于格式化密文
 * 
 * V0版本的格式： 使用根密钥加密的格式：IV(16字节，可配)密文 => Base64 => 输出
 * 使用其他密钥加密格式：IV(16字节，可配)SALT(8字节，可配)密文 => Base64 => 输出
 * 
 *
 */
class FormatterV0 extends Formatter {
    // 配置项：IV长度（单位：byte）。可选。默认16字节。虽然Java当前只支持16字节，但是为了以后扩展，也做为可配的
    public static final String PROP_IV_LENGTH = "crypt_aes_cbc_iv_length";
    // 配置项：盐值长度（单位：byte）。可选。默认为8字节。
    public static final String PROP_SALT_LENGTH = "crypt_aes_cbc_salt_length";

    // 目前IV值只支持128位的
    private static final int DEFAULT_IV_LENGTH = 16;
    // IV长度最小为1，小于1肯定不合法
    private static final int IV_LENGTH_MIN = 1;

    // 盐值长度默认为8字节
    private static final int DEFAULT_SALT_LENGTH = 8;
    // 《密码算法应用规范V1.1》 3.5节规定盐值至少为8字节
    private static final int SALT_LENGTH_MIN = 8;

    // 密文是否是使用根密钥加密的
    private boolean encByRootKey = false;

    /**
     * <pre>
     * 解析格式化后的加密结果。
     * 在调用该接口之前，必须先调用setEncByRootKey(boolean) 设置密文是否由根密钥加密
     * 
     * @param formatted 格式化后的加密结果
     * @return 密文及其参数。格式无效时返回null
     * </pre>
     */
    @Override
    public List<byte[]> parse(String formatted) {
        if (null == formatted || formatted.length() == 0) {
            return null;
        }

        int ivLen = AppProperties.getAsInt(PROP_IV_LENGTH, DEFAULT_IV_LENGTH);
        if (ivLen < IV_LENGTH_MIN) {
            throw new AppRuntimeException("Config Error. IV_LENGTH > " + IV_LENGTH_MIN);
        }

        int saltLen = AppProperties.getAsInt(PROP_SALT_LENGTH, DEFAULT_SALT_LENGTH);
        if (saltLen < SALT_LENGTH_MIN) {
            throw new AppRuntimeException("Config Error. SALT_LENGTH > " + SALT_LENGTH_MIN);
        }

        byte[] decoded = Base64.decodeBase64(formatted);
        byte[] iv = Arrays.copyOfRange(decoded, 0, ivLen);
        byte[] salt = null;
        byte[] ecyptContent = null;
        // 如果密文是使用根密钥加密的，则密文中没有盐值
        if (encByRootKey) {
            ecyptContent = Arrays.copyOfRange(decoded, ivLen, decoded.length);
        } else {
            salt = Arrays.copyOfRange(decoded, ivLen, ivLen + saltLen);
            ecyptContent = Arrays.copyOfRange(decoded, ivLen + saltLen, decoded.length);
        }

        List<byte[]> result = new LinkedList<byte[]>();
        try {
            // 将密文中的参数按照新版本的顺序存放
            // 加解密算法名称
            result.add(CrypterFactory.AES_CBC.getBytes("UTF-8"));
            // 密文
            result.add(EncryptHelper.parseByte2HexStr(ecyptContent).getBytes("UTF-8"));
            // 时间戳
            result.add(String.valueOf(Long.MAX_VALUE).getBytes("UTF-8"));
            // 迭代次数
            result.add(String.valueOf(KeyGen.DEFAULT_ITERATION_COUNT).getBytes("UTF-8"));
            // IV
            result.add(iv);
            // 盐值
            result.add(salt);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }

        return result;
    }

    /**
     * 
     * 设置密文是否是使用根密钥加密的。
     * 
     * @param encByRootKey
     *            密文是否是使用根密钥加密的
     */
    public void setEncByRootKey(boolean encByRootKey) {
        this.encByRootKey = encByRootKey;
    }
}
