package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RouterTaskRequest implements Serializable {
    private static final long serialVersionUID = 7719038509238742364L;

    @JsonProperty("src_vm_ip")
    private String srcVmIp;

    @JsonProperty("dst_vm_ip")
    private String dstVmIp;

    @JsonProperty("src_net_id")
    private String srcNetId;

    @JsonProperty("dst_net_id")
    private String dstNetId;

    @JsonProperty("src_vm_id")
    private String srcVmId;

    @JsonProperty("dst_vm_id")
    private String dstVmId;

    public String getSrcVmIp() {
        return srcVmIp;
    }

    public void setSrcVmIp(String srcVmIp) {
        this.srcVmIp = srcVmIp;
    }

    public String getDstVmIp() {
        return dstVmIp;
    }

    public void setDstVmIp(String dstVmIp) {
        this.dstVmIp = dstVmIp;
    }

    public String getSrcNetId() {
        return srcNetId;
    }

    public void setSrcNetId(String srcNetId) {
        this.srcNetId = srcNetId;
    }

    public String getDstNetId() {
        return dstNetId;
    }

    public void setDstNetId(String dstNetId) {
        this.dstNetId = dstNetId;
    }

    public String getSrcVmId() {
        return srcVmId;
    }

    public void setSrcVmId(String srcVmId) {
        this.srcVmId = srcVmId;
    }

    public String getDstVmId() {
        return dstVmId;
    }

    public void setDstVmId(String dstVmId) {
        this.dstVmId = dstVmId;
    }
}
