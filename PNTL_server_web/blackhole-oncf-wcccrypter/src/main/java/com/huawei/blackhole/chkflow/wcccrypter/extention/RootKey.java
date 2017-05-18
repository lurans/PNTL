package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import org.wcc.framework.AppRuntimeException;

public class RootKey {
    private RootKeyComponent[] rkcs = null;
    private Key key;

    public RootKey(int keyLength, int iterationCount) {
        this(RootKeyComponent.getKeyComps(), keyLength, iterationCount);
    }

    public RootKey(RootKeyComponent[] rkcs, int keyLength, int iterationCount) {
        this.rkcs = rkcs;
        if ((null != rkcs) && (rkcs.length >= 2)) {
            int compsNum = rkcs.length;

            String salt = rkcs[(compsNum - 1)].getValue();

            RootKeyComponent combinedComps = rkcs[0];
            for (int i = 1; i < compsNum - 1; i++) {
                combinedComps = combinedComps.combine(rkcs[i]);
            }
            try {
                this.key = KeyGen.genKey(combinedComps.getValue(), salt.getBytes("UTF-8"), keyLength, iterationCount);
            } catch (UnsupportedEncodingException e) {
                throw new AppRuntimeException(e);
            }
        } else {
            throw new AppRuntimeException("Param illegal");
        }
    }

    public Key getKey() {
        return this.key;
    }

    public RootKeyComponent[] getRkcs() {
        return this.rkcs;
    }
}
