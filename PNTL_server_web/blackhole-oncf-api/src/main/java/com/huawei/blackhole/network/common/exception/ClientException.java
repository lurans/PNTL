package com.huawei.blackhole.network.common.exception;

public class ClientException extends BaseException {
    private static final long serialVersionUID = 129584375346074948L;
    private int code = 0;

    public ClientException(String type, String message) {
        super(type, message);
    }

    public ClientException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
