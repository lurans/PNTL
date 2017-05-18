package com.huawei.blackhole.network.common.exception;


public class ErrorCodeException extends Exception {
    private static final long serialVersionUID = 1445865263183732005L;

    private String errorCode;

    private String errorMessage;

    public ErrorCodeException() {
        super();
    }


    public ErrorCodeException(String errorCode, String errorMessage) {
        super();

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorsCode() {
        return errorCode;
    }

    public String getErrorsMessage() {
        return errorMessage;
    }


}
