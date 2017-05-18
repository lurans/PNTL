package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@JsonIgnoreProperties(ignoreUnknown = true)
public enum Facing {
    INTERNAL, ADMIN, PUBLIC;

    private static final Logger log = LoggerFactory.getLogger(Facing.class);

    @JsonCreator
    public static Facing value(String facing) {
        if (facing == null || facing.isEmpty())
            return PUBLIC;
        try {
            return valueOf(facing.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn(e.getLocalizedMessage());
            return PUBLIC;
        }
    }

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }

}
