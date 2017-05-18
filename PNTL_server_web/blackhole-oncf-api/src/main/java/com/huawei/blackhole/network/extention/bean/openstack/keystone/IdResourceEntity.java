package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdResourceEntity implements OpenStackEntity {
    private static final long serialVersionUID = 6069428287820542909L;

    @JsonProperty
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
