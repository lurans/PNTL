package com.huawei.blackhole.network.common.exception;

/**
 * 当文件格式出错的时候，抛出该异常
 */
public class InvalidFormatException extends BaseException {
    private static final long serialVersionUID = 6306446015047356658L;

    public InvalidFormatException(String type, String message) {
        super(type, message);
    }
}
