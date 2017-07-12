package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.PingListRequest;
import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.AgentFlowsJson;
import com.huawei.blackhole.network.extention.bean.pntl.IpListJson;

import java.util.List;

public interface PntlService {
    Result<String> startPntl();

    Result<AgentFlowsJson> getPingList(PingListRequest config);

    Result<IpListJson> getIpListinfo(String azId, String podId);

    Result<String> setProbeInterval(String timeInterval);

    Result<String> startAgent();

    Result<String> initPntl() throws ClientException;

    Result<String> initHostList();

    Result<String> saveAgentIp(String agentIp, String vbondIp);

    Result<String> updateAgents(String type);

    Result<String> setServerConf(PntlConfig config);
}
