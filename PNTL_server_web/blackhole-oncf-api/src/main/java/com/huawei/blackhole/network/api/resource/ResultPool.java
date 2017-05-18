package com.huawei.blackhole.network.api.resource;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huawei.blackhole.network.api.bean.RouterInfoResponse;
import com.huawei.blackhole.network.common.constants.ResultTag;

public class ResultPool {
    private static final Logger LOG = LoggerFactory.getLogger(ResultPool.class);
    // task id -> record
    private static Map<String, Record> pool = new ConcurrentHashMap<String, Record>();

    public static RouterInfoResponse getResult(String taskId) {
        RouterInfoResponse result = null;
        if (taskId != null && pool.containsKey(taskId)) {
            LOG.info("get result of task : " + taskId);
            result = pool.get(taskId).getContent();
            pool.remove(taskId);
            LOG.info("TASK-END:" + taskId);
        } else {
            result = new RouterInfoResponse();
            result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
        }
        return result;
    }

    public static void add(String taskId, RouterInfoResponse result) {
        Record record = new Record(result, new Date());
        LOG.info("add result of task : " + taskId);
        pool.put(taskId, record);
    }

    public static void clearOvertimeRecord() {
        List<String> clearKeys = new LinkedList<String>();
        for (Entry<String, Record> entry : pool.entrySet()) {
            if (entry.getValue().overtime()) {
                String taskId = entry.getKey();
                LOG.info("clear result of task : " + taskId);
                clearKeys.add(taskId);
            }
        }
        for (String key : clearKeys) {
            pool.remove(key);
        }
    }

}
