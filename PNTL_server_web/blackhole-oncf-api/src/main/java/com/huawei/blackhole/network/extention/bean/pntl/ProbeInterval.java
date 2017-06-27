package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProbeInterval implements Serializable{
    private static final long serialVersionUID = 538531942937409676L;
    @JsonProperty("probe_interval")
    private String probe_interval;

    public String getProbe_interval() {
        return probe_interval;
    }

    public void setProbe_interval(String probe_interval) {
        this.probe_interval = probe_interval;
    }
}
