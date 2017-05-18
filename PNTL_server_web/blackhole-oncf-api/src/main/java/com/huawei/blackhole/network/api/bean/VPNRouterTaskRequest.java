package com.huawei.blackhole.network.api.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VPNRouterTaskRequest implements Serializable {
    private static final long serialVersionUID = 4355645583238685373L;
    
    @JsonProperty("vm_ip")
    private String vmIp;

    @JsonProperty("net_id")
    private String netId;
    
    @JsonProperty("vm_id")
    private String vmId;
    
    @JsonProperty("remote_ip")
    private String remoteIp;

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getVmId() {
        return vmId;
    }

    public void setVmId(String vmId) {
        this.vmId = vmId;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }    
}
