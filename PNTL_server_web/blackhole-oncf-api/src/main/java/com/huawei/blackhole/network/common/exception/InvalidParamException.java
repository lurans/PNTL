package com.huawei.blackhole.network.common.exception;

/**
 * 参数校验失败抛出此异常
 */
public class InvalidParamException extends BaseException {
    private static final long serialVersionUID = 6306446015047356658L;

    public InvalidParamException(String type, String message) {
        super(type, message);
    }
}
