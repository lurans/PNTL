package com.huawei.blackhole.network.api.resource;

public class PntlShareInfo {
    private boolean sendSuccess = false;
    private String errMsg = null;

    public boolean isSendSuccess() {
        return sendSuccess;
    }

    public void setSendSuccess(boolean sendSuccess) {
        this.sendSuccess = sendSuccess;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}


