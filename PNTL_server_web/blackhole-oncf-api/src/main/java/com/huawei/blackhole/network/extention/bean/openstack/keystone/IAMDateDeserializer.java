package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class IAMDateDeserializer extends JsonDeserializer<Date> {
    private static final Logger LOGGER = LoggerFactory.getLogger(IAMDateDeserializer.class);

    @Override
    public Date deserialize(JsonParser jp, DeserializationContext context) throws IOException, JsonProcessingException {
        Date date = null;

        if (jp == null) {
            return null;
        }

        String value = jp.getValueAsString();

        if (value == null) {
            return null;
        }

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            date = df.parse(value);
        } catch (ParseException e) {
            LOGGER.error("failed to parse zone : ", e);
        }

        return date;
    }
}
