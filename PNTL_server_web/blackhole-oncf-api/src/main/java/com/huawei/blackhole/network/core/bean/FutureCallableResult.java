package com.huawei.blackhole.network.core.bean;

import com.huawei.blackhole.network.api.bean.NodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FutureCallableResult {
    private String taskId;
    private Map<String, List<NodeInfo>> oneHostRouterInfo;

    public FutureCallableResult() {
        this.oneHostRouterInfo = new HashMap<String, List<NodeInfo>>();
        this.oneHostRouterInfo.put("OUTPUT", new ArrayList<NodeInfo>());
        this.oneHostRouterInfo.put("INPUT", new ArrayList<NodeInfo>());

    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Map<String, List<NodeInfo>> getOneHostRouterInfo() {
        return oneHostRouterInfo;
    }

    public void setOneHostRouterInfo(Map<String, List<NodeInfo>> oneHostRouterInfo) {
        this.oneHostRouterInfo = oneHostRouterInfo;
    }

    @Override
    public String toString() {
        return "FutureCallableResult [taskId=" + taskId
                + ", oneHostRouterInfo=" + oneHostRouterInfo + "]";
    }

}
