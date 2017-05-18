package com.huawei.blackhole.network.api.resource;

/**
 * 该类设计耦合较强，作为参数传递，共享多线程状态
 */
public class VpnInfo {
    // 信息 传递
    private String taskId;
    private int l2gwNum;
    private int rfNum;

    // 状态共享
    private boolean submited = false;
    private boolean hasErr = false;
    private String errMsg;

    public VpnInfo(String taskId) {
        this.taskId = taskId;
    }

    public VpnInfo() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getL2gwNum() {
        return l2gwNum;
    }

    public void setL2gwNum(int l2gwNum) {
        this.l2gwNum = l2gwNum;
    }

    public int getRfNum() {
        return rfNum;
    }

    public void setRfNum(int rfNum) {
        this.rfNum = rfNum;
    }

    public void setSubmited() {
        submited = true;
    }

    public boolean submited() {
        return submited;
    }

    public void errHappened() {
        hasErr = true;
    }

    public boolean hasErr() {
        return hasErr;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

}
