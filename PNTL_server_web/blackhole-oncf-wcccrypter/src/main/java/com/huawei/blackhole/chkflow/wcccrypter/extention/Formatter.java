package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.util.List;

import org.wcc.framework.AppRuntimeException;

public abstract class Formatter {
    public String format(List<byte[]> values) {
        throw new AppRuntimeException("Not Implemented");
    }

    public List<byte[]> parse(String formatted) {
        throw new AppRuntimeException("Not Implemented");
    }
}
