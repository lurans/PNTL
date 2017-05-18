package org.wcc.crypt;

import java.io.UnsupportedEncodingException;
import java.security.Key;

import org.wcc.framework.AppRuntimeException;

/**
 * 
 * 根密钥
 * 
 * 用于生成根密钥
 * 
 */
class RootKey {
    // 根密钥组件
    private RootKeyComponent[] rkcs = null;
    // 根密钥
    private Key key;

    /**
     * 生成指定长度的根密钥
     * 
     * @param keyLength
     *            根密钥长度
     * @param iterationCount
     *            迭代次数
     */
    public RootKey(int keyLength, int iterationCount) {
        this(RootKeyComponent.getKeyComps(), keyLength, iterationCount);
    }

    /**
     * 使用指定的密钥组件生成根密钥
     * 
     * @param rkcs
     *            指定的密钥组件
     * @param keyLength
     *            根密钥长度(单位：bit)
     * @param iterationCount
     *            迭代次数
     */
    public RootKey(RootKeyComponent[] rkcs, int keyLength, int iterationCount) {
        this.rkcs = rkcs;

        if (null != rkcs && rkcs.length >= RootKeyComponent.ROOT_KEY_COMPS_SIZE_MIN) {
            int compsNum = rkcs.length;

            // 最后一个密钥组件做为盐值
            String salt = rkcs[compsNum - 1].getValue();

            // 将其余的密钥组件合并起来
            RootKeyComponent combinedComps = rkcs[0];
            for (int i = 1; i < compsNum - 1; ++i) {
                combinedComps = combinedComps.combine(rkcs[i]);
            }

            // 生成根密钥
            try {
                this.key = KeyGen.genKey(combinedComps.getValue(), salt.getBytes("UTF-8"), keyLength, iterationCount);
            } catch (UnsupportedEncodingException e) {
                throw new AppRuntimeException(e);
            }
        } else {
            throw new AppRuntimeException("Param illegal");
        }
    }

    /**
     * 获取根密钥的值
     * 
     * @return
     */
    public Key getKey() {
        return this.key;
    }

    /**
     * 获取密钥组件
     * 
     * @return
     */
    public RootKeyComponent[] getRkcs() {
        return this.rkcs;
    }
}