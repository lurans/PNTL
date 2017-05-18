package com.huawei.blackhole.network.common.exception;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class HTTPServerException extends ClientException {

    public static final int HTTP_STATUS_CODE_FOUND = HttpStatus.SC_MOVED_TEMPORARILY;

    public static final int HTTP_STATUS_CODE_NO_AUTH = HttpStatus.SC_UNAUTHORIZED;

    public static final int HTTP_STATUS_CODE_ITEM_NOT_FOUND = HttpStatus.SC_NOT_FOUND;

    public static final int HTTP_STATUS_CODE_OVER_LIMIT = HttpStatus.SC_REQUEST_TOO_LONG;

    public static final int HTTP_STATUS_CODE_CONFLICT = HttpStatus.SC_CONFLICT;

    private static final long serialVersionUID = 2028008965334204860L;

    private static final Map<String, String> header = new HashMap<String, String>();

    private static final Object lock = new Object();

    public HTTPServerException(int code, String body) {
        super(code, body);
    }

    public static HTTPServerException createHTTPServerException(int statusCode, String response,
            Map<String, String> hMap) {
        HTTPServerException ex;
        switch (statusCode) {
        case HTTP_STATUS_CODE_FOUND:
            ex = new HTTPServerException(HTTP_STATUS_CODE_FOUND, response);
            break;
        case HTTP_STATUS_CODE_NO_AUTH:
            ex = new HTTPServerException(HTTP_STATUS_CODE_NO_AUTH, response);
            break;
        case HTTP_STATUS_CODE_ITEM_NOT_FOUND:
            ex = new HTTPServerException(HTTP_STATUS_CODE_ITEM_NOT_FOUND, response);
            break;
        case HTTP_STATUS_CODE_OVER_LIMIT:
            ex = new HTTPServerException(HTTP_STATUS_CODE_OVER_LIMIT, response);
            break;
        case HTTP_STATUS_CODE_CONFLICT:
            ex = new HTTPServerException(HTTP_STATUS_CODE_CONFLICT, response);
            break;
        default:
            ex = new HTTPServerException(statusCode, response);
            break;
        }
        ex.setHeader(hMap);
        return ex;
    }

    public Map<String, String> getHeader() {
        synchronized (lock) {
            return header;
        }
    }

    public void setHeader(Map<String, String> hMap) {
        if (null == hMap || hMap.isEmpty()) {
            return;
        }

        synchronized (lock) {
            header.clear();
            header.putAll(hMap);
        }
    }

}
