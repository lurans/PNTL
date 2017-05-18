package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.RouterTaskRequest;
import com.huawei.blackhole.network.core.bean.Result;

public interface EwRouterService {
    Result<String> submitEwTask(RouterTaskRequest req, String taskId);
}
