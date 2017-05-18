package com.huawei.blackhole.network.common.utils.http;


import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;

/**
 * Rest响应
 **/
public class RestResp {

    private Status statusCode;

    private JSONObject respBody;

    private JSONArray respArrayBody;

    private String body;

    private Header[] header;

    public Status getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Status statusCode) {
        this.statusCode = statusCode;
    }

    public JSONObject getRespBody() {
        return respBody;
    }

    public void setRespBody(JSONObject respBody) {
        this.respBody = respBody;
    }

    public JSONArray getRespArrayBody() {
        return respArrayBody;
    }

    public void setRespArrayBody(JSONArray respArrayBody) {
        this.respArrayBody = respArrayBody;
    }

    public Header[] getHeader() {
        if (null == this.header) {
            return null;
        }
        return header.clone();
    }

    public void setHeader(Header[] header) {
        if (null != header) {
            this.header = header.clone();
        }
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * toString
     *
     * @return toString
     **/
    @Override
    public String toString() {
        return "RestResp [statusCode=" + statusCode + ", respBody=" + respBody + "]";
    }

}
