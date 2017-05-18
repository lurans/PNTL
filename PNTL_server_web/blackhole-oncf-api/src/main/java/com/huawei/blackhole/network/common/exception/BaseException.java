package com.huawei.blackhole.network.common.exception;

public class BaseException extends Exception {
    private static final long serialVersionUID = -2314061243260074553L;
    private String type;

    public BaseException(String errMsg) {
        super(errMsg);
    }

    public BaseException(String type, String errMsg) {
        super(errMsg);
        this.setType(type);
    }

    public BaseException(String errMsg, Throwable e) {
        super(errMsg, e);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return prefix() + super.getLocalizedMessage();
    }

    public String prefix() {
        return type != null ? type + ": " : "";
    }

}
