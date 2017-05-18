package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeystoneCatalog implements OpenStackEntity {
    private static final long serialVersionUID = -2452319915315245771L;

    @JsonProperty
    private String id;

    @JsonProperty
    private String type;
    
    @JsonProperty
    private String name;

    @JsonProperty
    private List<KeystoneEndpointV3> endpoints;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<KeystoneEndpointV3> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<KeystoneEndpointV3> endpoints) {
        this.endpoints = endpoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
