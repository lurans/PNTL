package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BasicResourceEntity extends IdResourceEntity implements OpenStackEntity {
    private static final long serialVersionUID = -442244779971671167L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
