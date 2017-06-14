package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.AgentFlowsJson;
import com.huawei.blackhole.network.extention.bean.pntl.IpListJson;

import java.util.List;

public interface PntlService {
    Result<String> startPntl();

    Result<AgentFlowsJson> getPingList(PntlConfig config);

    Result<IpListJson> getIpListinfo(String azId, String podId);
}
