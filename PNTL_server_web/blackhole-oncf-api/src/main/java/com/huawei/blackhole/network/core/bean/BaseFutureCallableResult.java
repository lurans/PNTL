package com.huawei.blackhole.network.core.bean;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.TaskStatus;

public class BaseFutureCallableResult {

    private TaskStatus status;

    private String errMsg;

    private String errType;

    private CommonCallableParam commonParam;

    private String commandMsg;

    public BaseFutureCallableResult() {
        super();
    }

    public BaseFutureCallableResult(TaskStatus status) {
        super();
        this.status = status;
    }

    public BaseFutureCallableResult(CommonCallableParam commonParam) {
        super();
        this.commonParam = commonParam;
    }

    public BaseFutureCallableResult(CommonCallableParam commonParam, String commandMsg) {
        super();
        this.commonParam = commonParam;
        this.commandMsg = commandMsg;
    }

    public BaseFutureCallableResult(TaskStatus status, String errType, String commandMsg) {
        super();
        this.status = status;
        this.errType = errType;
        this.commandMsg = commandMsg;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public CommonCallableParam getCommonParam() {
        return commonParam;
    }

    public void setCommonParam(CommonCallableParam commonParam) {
        this.commonParam = commonParam;
    }

    public String getCommandMsg() {
        return commandMsg;
    }

    public void setCommandMsg(String commandMsg) {
        this.commandMsg = commandMsg;
    }

    public String getErrType() {
        return errType != null ? errType : ExceptionType.UNKOWN_ERR;
    }

    public void setErrType(String errType) {
        this.errType = errType;
    }
}
