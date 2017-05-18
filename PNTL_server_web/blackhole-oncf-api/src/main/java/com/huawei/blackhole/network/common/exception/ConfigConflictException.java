package com.huawei.blackhole.network.common.exception;

/**
 * Configuration exception.
 */
public class ConfigConflictException extends BaseException {
    private static final long serialVersionUID = 6306446015047356658L;

    public ConfigConflictException(String type, String errMsg) {
        super(errMsg);
        this.setType(type);
    }

}
