package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.CommonException;
import org.apache.commons.lang3.StringUtils;
import org.wcc.crypt.Crypter;
import org.wcc.crypt.CrypterFactory;
import org.wcc.framework.AppRuntimeException;

import java.util.Map;

/**
 * Encrypt and decrypt utils.
 **/
public class WccCrypter {
    //    private static final Logger LOG = LoggerFactory.getLogger(WccCrypter.class);
    private static final String ROOT_KEY = "FusionNetwork";

    /**
     * Encrypt data by specify key.
     *
     * @param data To be encoded data.
     * @param key  Key to be used in encryption.
     * @return Encrypted data.
     * @throws CommonException
     */
    public static String encryptData(String data, String key) throws CommonException {
        if (StringUtils.isEmpty(data) || StringUtils.isEmpty(key)) {
            throw new CommonException(ExceptionType.CLIENT_ERR, "data or key is null");
        }

        try {
            Crypter crypter = CrypterFactory.getCrypter(CrypterFactory.AES_CBC);
            String encodedData = crypter.encrypt(data, key);
            return encodedData;
        } catch (AppRuntimeException e) {
            String errMsg = String.format("encrypt data fail: %s", e.getLocalizedMessage());
            throw new CommonException(ExceptionType.SERVER_ERR, errMsg);
        }
    }

    /**
     * Encrypt data by internal key.
     *
     * @param data To be encoded data.
     * @return Encrypted data.
     * @throws CommonException
     */
    public static String encryptDataByRootKey(String data) throws CommonException {
        return encryptData(data, ROOT_KEY);
    }

    /**
     * Decrypt data by specify key.
     *
     * @param encodedData Data to be decrypted.
     * @param originKey   Key to be used in decryption.
     * @return Data after decryption.
     * @throws CommonException
     */
    public static String decryptData(String encodedData, String originKey) throws CommonException {
        if (StringUtils.isEmpty(encodedData) || StringUtils.isEmpty(originKey)) {
            throw new CommonException(ExceptionType.CLIENT_ERR, "data or key is null");
        }

        try {
            Crypter crypter = CrypterFactory.getCrypter(CrypterFactory.AES_CBC);
            String originData = crypter.decrypt(encodedData, originKey);
            return originData;
        } catch (AppRuntimeException e) {
            String errMsg = String.format("decrypt data fail: %s", e.getLocalizedMessage());
            throw new CommonException(ExceptionType.SERVER_ERR, errMsg);
        }
    }

    /**
     * Decrypt data by internal key.
     *
     * @param encodedData Data to be decrypted.
     * @return Data after decryption.
     * @throws CommonException
     */
    public static String decryptDataByRootKey(String encodedData) throws CommonException {
        return decryptData(encodedData, ROOT_KEY);
    }

    public static void encryptMapEntry(Map<String, Object> map, String key) throws CommonException {
        if (map == null || !map.containsKey(key)) {
            return;
        }
        String value = (String) map.get(key);

        map.put(key, encryptDataByRootKey(value));
    }


    public static void decryptMapEntry(Map<String, Object> map, String key) throws CommonException {
        if (map == null || !map.containsKey(key)) {
            return;
        }
        String value = (String) map.get(key);

        map.put(key, decryptDataByRootKey(value));
    }

}
