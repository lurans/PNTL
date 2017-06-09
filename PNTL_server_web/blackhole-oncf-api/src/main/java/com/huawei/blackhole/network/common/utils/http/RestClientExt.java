package com.huawei.blackhole.network.common.utils.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.HTTPServerException;

public class RestClientExt {
    private static final int SocketTimeout = 30000;

    private static final int ConnectTimeout = 30000;

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClientExt.class);

    private static CloseableHttpClient getHttpClient() throws GeneralSecurityException {
        SSLContext sslcontext = SSLContexts.custom().build();
        sslcontext.init(null, new TrustAnyTrustManager[] { new TrustAnyTrustManager() }, new SecureRandom());
        // Allow TLSv1,TLSv1.1,TLSv1.2 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[] { "TLSv1",
                "TLSv1.1", "TLSv1.2" }, null, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }

    private static RestResp send(HttpUriRequest request) throws IOException, GeneralSecurityException {
        RestResp httpResp = new RestResp();

        CloseableHttpClient closeableHttpClient = getHttpClient();

        try {
            // 请求数据
            CloseableHttpResponse response = closeableHttpClient.execute(request);

            httpResp.setStatusCode(Status.valueOf(response.getStatusLine().getStatusCode()));

            HttpEntity entity = response.getEntity();
            httpResp.setHeader(response.getAllHeaders());
            // do something useful with the response body
            // and ensure it is fully consumed
            if (entity != null) {
                String retString = EntityUtils.toString(entity);
                if (!StringUtils.isEmpty(retString)) {
                    final char firstChar = retString.charAt(0);
                    if (firstChar == '{') {
                        httpResp.setRespBody(new JSONObject(retString));
                    } else if (firstChar == '[') {
                        httpResp.setRespArrayBody(new JSONArray(retString));
                    } else {
                        httpResp.setBody(retString);
                        return httpResp;
                    }
                }

            }
        } finally {
            try {
                closeableHttpClient.close();
            } catch (IOException e) {
                LOGGER.error("close HttpClient error: {}", e.getLocalizedMessage());
            }
        }

        return httpResp;
    }

    private static String buildUrl(String url, Parameter parameters) {
        StringBuilder urlSb = new StringBuilder(url);

        if (parameters != null && !parameters.isEmpty()) {
            urlSb.append("?").append(parameters.toString());
        }

        return urlSb.toString();
    }

    private static void configHttpEntityRequest(HttpEntityEnclosingRequestBase request, JSONObject body,
            Map<String, String> customizedHeaders) throws UnsupportedEncodingException {
        if (body != null) {
            request.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));
        }

        configHttpBaseRequest(request, customizedHeaders);
    }

    private static void configHttpBaseRequest(HttpRequestBase request, Map<String, String> customizedHeaders)
            throws UnsupportedEncodingException {
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SocketTimeout)
                .setConnectTimeout(ConnectTimeout).build();
        request.setConfig(requestConfig);

        // custom header
        if (customizedHeaders != null) {
            for (Entry<String, String> entry : customizedHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        // common header
        request.addHeader("Accept", "application/json;charset=UTF-8");
        request.addHeader("Content-type", "application/json;charset=UTF-8");
    }

    private static String covertObject2String(Object obj) throws ClientException {
        if (obj instanceof String) {
            return (String) obj;
        }

        ObjectMapper mapper = new CustomObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            String errMsg = String.format("convert object to json fail: %s", e.getLocalizedMessage());
            throw new ClientException(ExceptionType.SERVER_ERR, errMsg);
        }
    }

    private static <T> T convertString2Object(String src, Class<T> clazz) throws ClientException {
        if (null == clazz || StringUtils.isEmpty(src) || src.trim().isEmpty()) {
            return null;
            // throw new ClientException("convert string to object fail");
        }

        if (String.class.getSimpleName().equals(clazz.getSimpleName())) {
            @SuppressWarnings("unchecked")
            T resp = (T) src;
            return resp;
        }

        ObjectMapper mapper = new CustomObjectMapper();
        try {
            return mapper.readValue(src, clazz);
        } catch (Exception e) {
            String errMsg = String.format("convert string to object fail: %s", e.getLocalizedMessage());
            throw new ClientException(ExceptionType.SERVER_ERR, errMsg);
        }
    }

    private static String getRespBodyAsString(RestResp response) {
        String message = null;
        if (response.getRespBody() != null) {
            message = response.getRespBody().toString();
        } else if (response.getRespArrayBody() != null) {
            message = response.getRespArrayBody().toString();
        } else if (response.getBody() != null) {
            message = response.getBody();
        }
        return message;
    }

    private static ClientException createHttpError(RestResp response) throws ClientException {
        int respStatusCode = response.getStatusCode().getCode();
        Header[] headerElements = response.getHeader();
        Map<String, String> headers = new HashMap<>();
        if (headerElements != null) {
            for (Header headerElement : headerElements) {
                headers.put(headerElement.getName(), headerElement.getValue());
            }
        }
        String message = getRespBodyAsString(response);
        return HTTPServerException.createHTTPServerException(respStatusCode, message, headers);
    }

    private static RestResp post(String url, Parameter parameters, JSONObject body,
            Map<String, String> customizedHeaders) throws ClientException {
        try {
            // 指定url,和http方式
            HttpPost httpPost = new HttpPost(buildUrl(url, parameters));
            configHttpEntityRequest(httpPost, body, customizedHeaders);

            RestResp response = send(httpPost);
            if (response.getStatusCode().isError()) {
                throw createHttpError(response);
            }
            return response;
        } catch (IOException | GeneralSecurityException e) {
            throw new ClientException(ExceptionType.SERVER_ERR, e.getLocalizedMessage());
        }
    }

    private static void configPntlHttpBaseRequest(HttpRequestBase request, Map<String, String> customizedHeaders)
            throws UnsupportedEncodingException {
        // 设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(SocketTimeout)
                .setConnectTimeout(ConnectTimeout).build();
        request.setConfig(requestConfig);

        // custom header
        if (customizedHeaders != null) {
            for (Entry<String, String> entry : customizedHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }
    private static RestResp pntlPost(String url, Parameter parameters, List<NameValuePair> body,
                                 Map<String, String> customizedHeaders) throws ClientException {
        try {
            // 指定url,和http方式
            HttpPost httpPost = new HttpPost(buildUrl(url, parameters));
            if (body != null) {
                httpPost.setEntity(new UrlEncodedFormEntity(body, "UTF-8"));
            }
            configPntlHttpBaseRequest(httpPost, customizedHeaders);
            RestResp response = send(httpPost);
            if (response.getStatusCode().isError()) {
                throw createHttpError(response);
            }
            return response;
        } catch (IOException | GeneralSecurityException e) {
            throw new ClientException(ExceptionType.SERVER_ERR, e.getLocalizedMessage());
        }
    }


    public static RestResp get(String url, Parameter parameters, Map<String, String> header) throws ClientException {
        try {
            String newUrl = buildUrl(url, parameters);
            LOGGER.info("get url={}", newUrl);
            HttpGet httpPut = new HttpGet(newUrl);

            configHttpBaseRequest(httpPut, header);

            RestResp response = send(httpPut);
            if (response.getStatusCode().isError()) {
                throw createHttpError(response);
            }
            return response;
        } catch (IOException | GeneralSecurityException e) {
            throw new ClientException(ExceptionType.SERVER_ERR, e.getLocalizedMessage());
        }
    }

    public static <T> T get(String url, Parameter para, Class<T> responseType, Map<String, String> header)
            throws ClientException {
        RestResp response = get(url, para, header);
        if (response == null || getRespBodyAsString(response) == null) {
            throw new ClientException(ExceptionType.SERVER_ERR, "null response from server.");
        }
        String strResponse = getRespBodyAsString(response);
        return convertString2Object(strResponse, responseType);
    }

    public static RestResp post(String url, Parameter para, List<NameValuePair> reqBody,
                                Map<String, String> header) throws ClientException{

        return pntlPost(url, para, reqBody, header);
    }

    public static RestResp post(String url, Parameter para, Object reqBody, Map<String, String> header)
            throws ClientException {
        String beanStrBody = null;
        JSONObject jsonBody = null;
        if (reqBody != null) {
            beanStrBody = covertObject2String(reqBody);
            jsonBody = new JSONObject(beanStrBody);
        }

        return post(url, para, jsonBody, header);
    }

    public static <T> T post(String url, Parameter para, Object reqBody, Class<T> responseType,
            Map<String, String> header) throws ClientException {
        RestResp response = post(url, para, reqBody, header);

        String strResponse = getRespBodyAsString(response);
        return convertString2Object(strResponse, responseType);
    }

    private static class TrustAnyTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }

}
