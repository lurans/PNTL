package org.wcc.crypt;

import java.security.Key;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.wcc.framework.AppProperties;
import org.wcc.framework.AppRuntimeException;

/**
 * 
 * 密钥生成器
 * 
 * 用于生成密钥
 */
class KeyGen {
    // 配置项：生成密钥时的迭代运算次数，值越大越安全，但是越耗时。可选。默认50000次。如果要提升性能，可适当降低迭代次数，具体可参考《密码算法应用规范V1.1》表格3-1
    public static final String PROP_ITERATION_COUNT = "crypt_keygen_iteration_count";
    protected static final int DEFAULT_ITERATION_COUNT = 50000;

    /**
     * 导出密钥
     * 
     * @param password
     *            密码。一般为用户输入的密码。
     * @param salt
     *            盐值
     * @param iterationCount
     *            迭代次数
     * @param keyLength
     *            密钥长度
     * @return 密钥对象 如果返回值用于AES加解密，需要使用new SecretKeySpec(result, "AES")包装一下密钥对象
     * @throws AppRuntimeException
     */
    public static Key genKey(String password, byte[] salt, int keyLength, int iterationCount)
            throws AppRuntimeException {
        try {
            // 《密码算法应用规范V1.1》 2.4.1建议使用PBKDF2导出密钥
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec keyspec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);

            return factory.generateSecret(keyspec);
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 
     * 获取生成密钥时的迭代次数
     * 
     * @return 迭代次数
     */
    public static int getIterationCount() {
        return AppProperties.getAsInt(PROP_ITERATION_COUNT, DEFAULT_ITERATION_COUNT);
    }
}