package com.huawei.blackhole.network.common.exception;

public class ApplicationException extends BaseException {

    private static final long serialVersionUID = -6473337127341642275L;

    public ApplicationException(String type, String errMsg) {
        super(type, errMsg);
    }

}
