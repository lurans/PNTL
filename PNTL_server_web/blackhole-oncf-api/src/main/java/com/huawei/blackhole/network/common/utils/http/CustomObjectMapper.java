package com.huawei.blackhole.network.common.utils.http;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.lang.annotation.Annotation;

public class CustomObjectMapper extends ObjectMapper {
    private static final long serialVersionUID = -228654143152241719L;

    public CustomObjectMapper() {
        this.setSerializationInclusion(Include.NON_NULL);
    }

    private boolean hasJsonRootName(JavaType valueType) {
        if (valueType.getRawClass() == null) {
            return false;
        }

        Annotation rootAnnotation = valueType.getRawClass().getAnnotation(JsonRootName.class);
        return rootAnnotation != null;
    }

    @Override
    public <T> T readValue(String content, Class<T> clazz)
            throws IOException, JsonParseException, JsonMappingException {
        JavaType valueType = _typeFactory.constructType(clazz);
        // 处理转义字符
        configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (hasJsonRootName(valueType)) {
            configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        } else {
            configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        }
        return super.readValue(content, clazz);
    }

    @Override
    public String writeValueAsString(Object value) throws JsonProcessingException {
        if (null != value) {
            Class<?> clazz = value.getClass();
            JavaType valueType = _typeFactory.constructType(clazz);
            if (hasJsonRootName(valueType)) {
                configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            } else {
                configure(SerializationFeature.WRAP_ROOT_VALUE, false);
            }
        }
        return super.writeValueAsString(value);
    }
}
