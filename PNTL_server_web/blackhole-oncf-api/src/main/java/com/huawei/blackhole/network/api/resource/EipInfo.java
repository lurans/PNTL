package com.huawei.blackhole.network.api.resource;

/**
 * 该类设计耦合较强，作为参数传递，共享多线程状态
 */
public class EipInfo {
    private String taskId;
    private int snatNum;

    private boolean submited = false;
    private boolean hasErr = false;
    private String errMsg;

    public EipInfo(String taskId) {
        this.taskId = taskId;
    }

    public EipInfo() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getSnatNum() {
        return snatNum;
    }

    public void setSnatNum(int snatNum) {
        this.snatNum = snatNum;
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
