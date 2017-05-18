package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.FIPRouterTaskRequest;
import com.huawei.blackhole.network.core.bean.Result;

public interface EIPRouterService {
    Result<String> submitEipTask(FIPRouterTaskRequest req, String taskId);
}
