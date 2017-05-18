package com.huawei.blackhole.network.common.exception;

/**
 * Configuration exception.
 */
public class ConfigLostException extends BaseException {
    private static final long serialVersionUID = -1278531072337169439L;

    public ConfigLostException(String type, String message) {
        super(type, message);
    }
}
