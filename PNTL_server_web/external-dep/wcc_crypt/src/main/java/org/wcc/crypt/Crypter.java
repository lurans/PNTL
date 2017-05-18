package org.wcc.crypt;

import java.security.Key;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.wcc.framework.AppRuntimeException;

/**
 * <pre>
 * 加解密基类。
 * 提供加密和解密功能，使用CrypterFactory获取具体的实现类。
 * </pre>
 *
 */
public abstract class Crypter {
    /**
     * 加密算法标识在参数中的位置
     */
    protected static final int PARAM_INDEX_ALGORITHM = 0;

    /**
     * 密文在参数中的位置
     */
    protected static final int PARAM_INDEX_ENCRYPTED = 1;

    /**
     * 根密钥时间戳在参数中的位置。使用根密钥加密时需要设置其为当前根密钥的时间戳；不使用根密钥加密时，该项须指定为null
     */
    protected static final int PARAM_INDEX_ROOTKEY_TIMESTAMP = 2;

    /**
     * 生成根密钥时的迭代次数在参数中的位置
     */
    protected static final int PARAM_INDEX_KEYGEN_ITERATION_COUNT = 3;

    /**
     * 具体加密算法参数的起始下标
     */
    protected static final int ALGO_PARAM_START = 4;

    /**
     * 加解密参数。 各个参数必须按照加密结果格式规定的顺序保存。具体的加解密算法只需保存该算法相关的参数即可，其他参数不用关心 加解密结果的格式：
     * Base64Encode
     * (加解密算法标识;密文;根密钥时间戳(如果非根密钥加密，则为空);生成密钥时的迭代次数;加解密算法相关的参数1;加解密算法相关的参数1;...;)
     */
    private static ThreadLocal<Map<Integer, byte[]>> crypterParam = new ThreadLocal<Map<Integer, byte[]>>() {
        @Override
        public Map<Integer, byte[]> initialValue() {
            return new HashMap<Integer, byte[]>();
        }
    };

    /**
     * 加密
     * 
     * @param content
     *            明文
     * @param password
     *            密码的明文
     * @return 密文。加密失败返回null
     * @throws AppRuntimeException
     */
    public abstract String encrypt(String content, String password) throws AppRuntimeException;

    /**
     * 解密
     * 
     * @param content
     *            密文
     * @param password
     *            密码的明文
     * @return 明文。解密失败返回null
     * @throws AppRuntimeException
     */
    public abstract String decrypt(String content, String password) throws AppRuntimeException;

    /**
     * 使用根密钥加密
     * 
     * @param content
     *            明文
     * @return 密文。加密失败返回null
     * @throws AppRuntimeException
     */
    public String encryptByRootKey(String content) throws AppRuntimeException {
        throw new AppRuntimeException("Not Implemented");
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
        throw new AppRuntimeException("Not Implemented");
    }

    /**
     * 使用根密钥加密。该接口只会在wcc的密钥升级过程内部使用，不对外提供
     * 
     * @param content
     *            明文
     * @param rootKey
     *            根密钥
     * @return 密文。加密失败返回null
     * @throws AppRuntimeException
     */
    protected String encryptByRootKey(String content, Key rootKey) throws AppRuntimeException {
        throw new AppRuntimeException("Not Implemented");
    }

    /**
     * 使用根密钥解密。该接口只会在wcc的密钥升级过程内部使用，不对外提供
     * 
     * @param content
     *            密文
     * @param rootKey
     *            根密钥
     * @return 明文。解密失败返回null
     * @throws AppRuntimeException
     */
    protected String decryptByRootKey(String content, Key rootKey) throws AppRuntimeException {
        throw new AppRuntimeException("Not Implemented");
    }

    /**
     * 获取全部加解密参数
     * 
     * @return 全部参数
     */
    protected static List<byte[]> getParam() {
        return new LinkedList<byte[]>(crypterParam.get().values());
    }

    /**
     * 获取特定位置的加解密参数
     * 
     * @param index
     *            下标
     * @return index位置的元素
     */
    protected static byte[] getParam(int index) {
        return crypterParam.get().get(index);
    }

    /**
     * 将参数设置到特定位置
     * 
     * @param index
     *            下标
     * @param param
     *            参数值
     */
    protected static void setParam(int index, byte[] param) {
        crypterParam.get().put(index, param);
    }

    /**
     * 设置一批参数
     * 
     * @param param
     *            参数列表
     */
    protected static void setParam(List<byte[]> param) {
        if (null == param || param.isEmpty()) {
            throw new AppRuntimeException("null == param or empty");
        }

        Map<Integer, byte[]> paramMap = new HashMap<Integer, byte[]>();
        int size = param.size();
        for (int i = 0; i < size; ++i) {
            paramMap.put(i, param.get(i));
        }

        crypterParam.set(paramMap);
    }

    /**
     * 清除参数。每次加解密完成后调用
     */
    protected static void clearParam() {
        crypterParam.remove();
    }
}
