package com.huawei.blackhole.network.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * json转换类
 **/
public class JsonConvertUtils {
    private static final Logger LOG = LoggerFactory.getLogger(JsonConvertUtils.class);

    /**
     * 提供将JSON格式字符串转换为bean类的工具方法
     *
     * @param json  String
     * @param clazz Class
     * @param <T>   T
     * @return bean
     */
    public static <T> T convertJson2Bean(String json, Class<T> clazz) {
        if (null == json || null == clazz) {
            return null;
        }
        if (String.class.getSimpleName().equals(clazz.getSimpleName())) {
            @SuppressWarnings("unchecked")
            T resp2 = (T) json;
            return resp2;
        }
        ObjectMapper mp = new ObjectMapper();
        T bean = null;
        try {
            bean = mp.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("convertJson2Bean failed! Exception while json to bean:", e);
        }
        return bean;
    }

    /**
     * 提供将bean类转换为json格式字符串的工具方法
     *
     * @param <T>  T
     * @param bean T
     * @return jsonStr
     */
    public static <T> String convertBean2Json(T bean) {
        if (null == bean) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(bean);
        } catch (IOException e) {
            LOG.error("convertBean2Json failed! Exception while bean to json:", e);
        }

        return jsonStr;
    }

    /**
     * 提供将带有rootName的bean类转换为json格式字符串的工具方法
     *
     * @param <T>  T
     * @param bean T
     * @return json
     */
    public static <T> String convertBeanWithRootName2Json(T bean) {
        if (null == bean) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        String json = null;
        try {
            json = objectMapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            LOG.error("convertBeanWithRootName2Json failed. exception bean to json", e);
        }

        return json;
    }

}
