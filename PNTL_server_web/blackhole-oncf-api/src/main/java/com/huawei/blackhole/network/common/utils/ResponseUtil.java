package com.huawei.blackhole.network.common.utils;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.huawei.blackhole.network.common.constants.Constants;

public class ResponseUtil {
    /**
     * 返回成功，包含data的json成功信息
     * 
     * @param data
     * @return
     */
    public static Response succ(Object data) {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(data).build();
    }

    public static Response succ(JSONObject jsonData) {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(jsonData.toString()).build();
    }

    /**
     * 返回 {"result":"success"}
     * 
     * @return
     */
    public static Response succ() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("result", Constants.RESULT_SUCCESS);
        return succ(data);
    }

    /**
     * 返回失败：包含data的json失败信息 type表示失败类型
     * 
     * @return
     */
    public static Response err(Response.Status type, Object data) {
        JSONObject json = new JSONObject();
        json.put("err_msg", data);
        return Response.status(type).entity(json.toString()).build();
    }

    /**
     * 返回 {"result":"failed"}
     * 
     * @return
     */
    public static Response err(Response.Status type) {
        Map<String, String> data = new HashMap<String, String>();
        data.put("result", Constants.RESULT_FAILED);
        return err(type, data);
    }

    public static Response errData(Response.Status type, Object data) {
        return Response.status(type).entity(data).build();
    }

    public static Response errJson(Response.Status type, JSONObject data) {
        return Response.status(type).entity(data.toString()).build();
    }

}
