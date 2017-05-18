package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.VPNRouterTaskRequest;
import com.huawei.blackhole.network.core.bean.Result;

public interface VPNRouterService {
    Result<String> submitVpnTask(VPNRouterTaskRequest req, String taskId);
}
