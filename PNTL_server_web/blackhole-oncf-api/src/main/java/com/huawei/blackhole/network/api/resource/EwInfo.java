package com.huawei.blackhole.network.api.resource;

/**
 * 该类设计耦合较强，作为参数传递，共享多线程状态
 */
public class EwInfo {
    private String taskId;
    private int srcTaskNum;
    private int dstTaskNum;

    private boolean submited = false;
    private boolean hasErr = false;
    private String errMsg;

    public EwInfo(String taskId) {
        this.taskId = taskId;
    }

    public EwInfo() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getSrcTaskNum() {
        return srcTaskNum;
    }

    public void setSrcTaskNum(int srcTaskNum) {
        this.srcTaskNum = srcTaskNum;
    }

    public int getDstTaskNum() {
        return dstTaskNum;
    }

    public void setDstTaskNum(int dstTaskNum) {
        this.dstTaskNum = dstTaskNum;
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
