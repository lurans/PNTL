package org.wcc.crypt;

import org.wcc.framework.AppRuntimeException;

/**
 * 用于创建加解密类的工厂方法
 *
 */
public class CrypterFactory {
    /**
     * 加解密算法类型。目前只支持AES_CBC
     */
    public static final String AES_CBC = "AES_CBC";

    /**
     * 获得指定算法的加解密方法
     * 
     * @param algorithm
     *            加解密算法
     * @return 对应的加解密实现实例
     * @throws AppRuntimeException
     */
    public static Crypter getCrypter(String algorithm) throws AppRuntimeException {
        Crypter crypter = null;

        if (null == algorithm) {
            throw new AppRuntimeException("Algorithm Should not be null");
        }

        if (algorithm.equals(AES_CBC)) {
            crypter = new CrypterAesCBC();
        } else {
            throw new AppRuntimeException("Unsupported Crypter Algorithm: " + algorithm);
        }

        // 返回代理类。该类调用具体加解密算法，并提供根密钥更新功能
        return (Crypter) new CrypterProxy(algorithm, crypter, RootKeyUpdater.getInstance(), new FormatterV1());
    }
}