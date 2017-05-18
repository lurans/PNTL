package org.wcc.crypt;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.List;

import org.wcc.framework.AppRuntimeException;

/**
 * 
 * 加解密代理类
 * 
 * 代理具体的加解密算法，并提供密钥更新功能
 */
class CrypterProxy extends Crypter {
    private static final int BYTE_SIZE = 8;

    // 加解密算法名称
    private String algorithm = null;
    // 加解密算法实例
    private Crypter crypter = null;
    // 根密钥升级器
    private RootKeyUpdater updater = null;
    // 密文格式化
    private Formatter formatter = null;

    protected CrypterProxy(String algorithm, Crypter crypter, RootKeyUpdater updater, Formatter formatter) {
        this.algorithm = algorithm;
        this.crypter = crypter;
        this.updater = updater;
        this.formatter = formatter;
    }

    @Override
    public String encrypt(String content, String password) throws AppRuntimeException {
        try {
            // 调用具体实现类的加密接口
            String encrypted = crypter.encrypt(content, password);

            // 根据密文格式，添加与实现无关的参数
            List<byte[]> param = getParam();
            try {
                param.add(PARAM_INDEX_ALGORITHM, this.algorithm.getBytes("UTF-8"));
                param.add(PARAM_INDEX_ENCRYPTED, encrypted.getBytes("UTF-8"));
                param.add(PARAM_INDEX_ROOTKEY_TIMESTAMP, null);
                param.add(PARAM_INDEX_KEYGEN_ITERATION_COUNT,
                        String.valueOf(KeyGen.getIterationCount()).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AppRuntimeException(e);
            }

            // 格式化
            return formatter.format(param);
        } finally {
            // 清除参数
            clearParam();
        }
    }

    @Override
    public String decrypt(String content, String password) throws AppRuntimeException {
        try {
            // 获取密文中的加密参数
            List<byte[]> params = formatter.parse(content);
            if (null == params) {
                // 尝试用V1.0.0格式进行解析
                Formatter formatterV0 = new FormatterV0();
                ((FormatterV0) formatterV0).setEncByRootKey(false);
                params = formatterV0.parse(content);
                if (null == params) {
                    throw new AppRuntimeException("Invalid content");
                }
            }

            setParam(params);

            byte[] encrypted = params.get(PARAM_INDEX_ENCRYPTED);
            // 调用具体实现的解密接口
            return crypter.decrypt(new String(encrypted, "UTF-8"), password);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        } finally {
            // 清除参数
            clearParam();
        }
    }

    public String encryptByRootKey(String content) throws AppRuntimeException {
        try {
            // 更新根密钥
            if (updater.needUpdate()) {
                updateRootKey();
            }

            // 使用新密钥加密数据
            String result = crypter.encryptByRootKey(content);

            // 根据密文格式，添加与实现无关的参数
            List<byte[]> param = getParam();

            try {
                param.add(PARAM_INDEX_ALGORITHM, this.algorithm.getBytes("UTF-8"));
                param.add(PARAM_INDEX_ENCRYPTED, result.getBytes("UTF-8"));
                param.add(PARAM_INDEX_ROOTKEY_TIMESTAMP,
                        String.valueOf(RootKeyComponent.currentTimeStamp()).getBytes("UTF-8"));
                param.add(PARAM_INDEX_KEYGEN_ITERATION_COUNT,
                        String.valueOf(KeyGen.getIterationCount()).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AppRuntimeException(e);
            }

            // 格式化
            return formatter.format(param);
        } finally {
            // 清除参数
            clearParam();
        }
    }

    public String decryptByRootKey(String content) throws AppRuntimeException {
        // 先解密数据，然后更新根密钥
        try {
            String result = null;

            // 先使用当前格式解析密文，如果失败再使用就版本格式解析
            List<byte[]> params = formatter.parse(content);
            if (null == params) {
                // 尝试用V1.0.0格式进行解析
                Formatter formatterV0 = new FormatterV0();
                ((FormatterV0) formatterV0).setEncByRootKey(true);
                params = formatterV0.parse(content);
                if (null == params) {
                    throw new AppRuntimeException("Invalid content");
                }
            }

            setParam(params);

            byte[] encryptedBytes = params.get(PARAM_INDEX_ENCRYPTED);
            String encrypted = new String(encryptedBytes, "UTF-8");
            long timeStamp = Long.parseLong(new String(getParam(PARAM_INDEX_ROOTKEY_TIMESTAMP), "UTF-8"));

            if (timeStamp == RootKeyComponent.currentTimeStamp()) {
                result = crypter.decryptByRootKey(encrypted);
            } else {
                // 密文中的时间戳和当前根密钥组件的不一致，说明该密文是使用历史根密钥解密的，需要生成时间戳对应的根密钥再解密
                RootKeyComponent[] rkcs = updater.getOldRKCS(timeStamp);

                int count = Integer.parseInt(new String(params.get(PARAM_INDEX_KEYGEN_ITERATION_COUNT), "UTF-8"));
                Key rootKey = new RootKey(rkcs, rkcs[0].getLength() * BYTE_SIZE, count).getKey();

                result = crypter.decryptByRootKey(encrypted, rootKey);
            }

            if (updater.needUpdate()) {
                updateRootKey();
            }

            return result;
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        } finally {
            clearParam();
        }
    }

    /**
     * 升级密钥组件
     */
    private void updateRootKey() {
        // 检查是否需要升级根密钥，如果需要则升级。因为升级过程要读写密钥组件，所以此处必须保证单线程单进程执行
        synchronized (RootKeyUpdater.class) {
            ProcessLocker locker = null;
            try {
                if (updater.needUpdate()) {
                    locker = ProcessLocker.getInstance(RootKeyUpdater.UPDATE_LOCKER);
                    // 保证只有一个进程执行
                    locker.lock();
                    if (updater.needUpdate()) {
                        updater.doUpdate();
                    }
                }
            } finally {
                if (null != locker) {
                    locker.unlock();
                }
            }
        }
    }
}
