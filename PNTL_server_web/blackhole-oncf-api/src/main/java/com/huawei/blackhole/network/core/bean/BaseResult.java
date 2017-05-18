package com.huawei.blackhole.network.core.bean;

import java.io.Serializable;


/**
 * BaseResult
 **/
public class BaseResult implements Serializable {
    private static final long serialVersionUID = -6757711633169599579L;

    private boolean isSuccess = true;

    private String errorCode;

    private String errorMessage;

    private String status;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        this.isSuccess = success;
    }

    /**
     * 错误信息
     *
     * @param errorCode    String
     * @param errorMessage String
     */
    public void addError(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.status = "ERROR";
        this.isSuccess = false;
    }


}
