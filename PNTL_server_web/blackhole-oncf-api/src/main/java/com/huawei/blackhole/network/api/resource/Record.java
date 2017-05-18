package com.huawei.blackhole.network.api.resource;

import java.util.Date;

import com.huawei.blackhole.network.api.bean.RouterInfoResponse;
import com.huawei.blackhole.network.common.constants.Constants;

public class Record {
    private RouterInfoResponse content;
    private Date submitTime;

    public Record(RouterInfoResponse content, Date submitTime) {
        super();
        this.content = content;
        this.submitTime = submitTime;
    }

    public RouterInfoResponse getContent() {
        return content;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    /**
     * 判断该记录是否超时
     * 
     * @return
     */
    public boolean overtime() {
        return (new Date().getTime() - submitTime.getTime()) > (Constants.RECORD_OVERTIME);
    }

}
