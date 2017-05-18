package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.List;
import org.wcc.framework.AppRuntimeException;

class CrypterProxy extends Crypter {
    private static final int BYTE_SIZE = 8;
    private String algorithm = null;
    private Crypter crypter = null;
    private RootKeyUpdater updater = null;
    private Formatter formatter = null;

    protected CrypterProxy(String algorithm, Crypter crypter, RootKeyUpdater updater, Formatter formatter) {
        this.algorithm = algorithm;
        this.crypter = crypter;
        this.updater = updater;
        this.formatter = formatter;
    }

    public String encrypt(String content, String password) throws AppRuntimeException {
        try {
            String encrypted = this.crypter.encrypt(content, password);

            List<byte[]> param = getParam();
            try {
                param.add(0, this.algorithm.getBytes("UTF-8"));
                param.add(1, encrypted.getBytes("UTF-8"));
                param.add(2, null);
                param.add(3, String.valueOf(KeyGen.getIterationCount()).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AppRuntimeException(e);
            }
            return this.formatter.format(param);
        } finally {
            clearParam();
        }
    }

    public String decrypt(String content, String password) throws AppRuntimeException {
        try {
            List<byte[]> params = this.formatter.parse(content);
            if (null == params) {
                Formatter formatterV0 = new FormatterV0();
                ((FormatterV0) formatterV0).setEncByRootKey(false);
                params = formatterV0.parse(content);
                if (null == params) {
                    throw new AppRuntimeException("Invalid content");
                }
            }
            setParam(params);

            byte[] encrypted = (byte[]) params.get(1);

            return this.crypter.decrypt(new String(encrypted, "UTF-8"), password);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        } finally {
            clearParam();
        }
    }

    public String encryptByRootKey(String content) throws AppRuntimeException {
        try {
            if (this.updater.needUpdate()) {
                updateRootKey();
            }
            String result = this.crypter.encryptByRootKey(content);

            List<byte[]> param = getParam();
            try {
                param.add(0, this.algorithm.getBytes("UTF-8"));
                param.add(1, result.getBytes("UTF-8"));
                param.add(2, String.valueOf(RootKeyComponent.currentTimeStamp()).getBytes("UTF-8"));
                param.add(3, String.valueOf(KeyGen.getIterationCount()).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new AppRuntimeException(e);
            }
            return this.formatter.format(param);
        } finally {
            clearParam();
        }
    }

    public String decryptByRootKey(String content) throws AppRuntimeException {
        try {
            String result = null;

            List<byte[]> params = this.formatter.parse(content);
            if (null == params) {
                Formatter formatterV0 = new FormatterV0();
                ((FormatterV0) formatterV0).setEncByRootKey(true);
                params = formatterV0.parse(content);
                if (null == params) {
                    throw new AppRuntimeException("Invalid content");
                }
            }
            setParam(params);

            byte[] encryptedBytes = (byte[]) params.get(1);
            String encrypted = new String(encryptedBytes, "UTF-8");
            long timeStamp = Long.parseLong(new String(getParam(2), "UTF-8"));
            RootKeyComponent[] rkcs;
            if (timeStamp == RootKeyComponent.currentTimeStamp()) {
                result = this.crypter.decryptByRootKey(encrypted);
            } else {
                rkcs = this.updater.getOldRKCS(timeStamp);

                int count = Integer.parseInt(new String((byte[]) params.get(3), "UTF-8"));
                Key rootKey = new RootKey(rkcs, rkcs[0].getLength() * 8, count).getKey();

                result = this.crypter.decryptByRootKey(encrypted, rootKey);
            }
            if (this.updater.needUpdate()) {
                updateRootKey();
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        } finally {
            clearParam();
        }
    }

    private void updateRootKey() {
        synchronized (RootKeyUpdater.class) {
            ProcessLocker locker = null;
            try {
                if (this.updater.needUpdate()) {
                    locker = ProcessLocker.getInstance("update_lock");

                    locker.lock();
                    if (this.updater.needUpdate()) {
                        this.updater.doUpdate();
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
