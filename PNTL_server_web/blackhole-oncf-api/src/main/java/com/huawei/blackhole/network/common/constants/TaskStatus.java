package com.huawei.blackhole.network.common.constants;

import org.apache.commons.lang3.StringUtils;


public enum TaskStatus {
    NONE,
    SUBMITTING,
    EXECUTING,
    SUCCESS,
    ERROR;

    public static TaskStatus value(String value) {
        for (TaskStatus result : TaskStatus.values()) {
            if (StringUtils.equals(result.name(), value)) {
                return result;
            }
        }
        return null;
    }
}
