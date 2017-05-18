package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KeystoneEndpointV3 implements OpenStackEntity {
    private static final long serialVersionUID = 877378365468633530L;

    @JsonProperty
    private String id;

    @JsonProperty("interface")
    private Facing iface;

    @JsonProperty("region")
    private String region;

    @JsonProperty("url")
    private URL url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Facing getIface() {
        return iface;
    }

    public void setIface(Facing iface) {
        this.iface = iface;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Endpoints implements OpenStackEntity {
        private static final long serialVersionUID = -1497233709481381104L;

        @JsonProperty("endpoints")
        List<KeystoneEndpointV3> endpoints;

        public List<KeystoneEndpointV3> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<KeystoneEndpointV3> endpoints) {
            this.endpoints = endpoints;
        }

        public String generateServicePublicUrl() {
            for (KeystoneEndpointV3 end : endpoints) {
                if (end.getIface().equals(Facing.PUBLIC)) {
                    if (end.getUrl().toString().contains("$(tenant_id)s"))
                        return end.getUrl().toString().replace("$(tenant_id)s", "");
                    else
                        return end.getUrl().toString();
                }
            }
            return null;
        }
    }

}
