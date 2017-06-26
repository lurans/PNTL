package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.PingListRequest;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.AgentFlowsJson;
import com.huawei.blackhole.network.extention.bean.pntl.IpListJson;

import java.util.List;

public interface PntlService {
    Result<String> startPntl();

    Result<AgentFlowsJson> getPingList(PingListRequest config);

    Result<IpListJson> getIpListinfo(String azId, String podId);
}
