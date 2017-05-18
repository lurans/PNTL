package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class NodeInfo implements Serializable {
    private static final long serialVersionUID = 3756243591840666044L;
    @JsonProperty("name")
    private String name;
    //br-int,tap,tun,br-tun,br-ex,Drv,

    @JsonProperty("type")
    private String type;
    
    @JsonProperty("vm_ip")
    private String vmIp;

    @JsonProperty("host_ip")
    private String hostIp;

    @JsonProperty("host_type")
    private String hostType;

    @JsonProperty("availability_zone")
    private String az;

    @JsonProperty("pod")
    private String pod;

    @JsonProperty("src_ip")
    private String srcIp;

    @JsonProperty("dst_ip")
    private String destIp;

    /**
     * 包数量，为0表示没流量
     */
    @JsonProperty("packets")
    private Integer packetNum;

    @JsonProperty("src_mac")
    private String srcMac;

    @JsonProperty("dst_mac")
    private String destMac;


    public NodeInfo(String name, String type, String srcIp, String destIp,
                    int packetNum) {
        this.name = name;
        this.type = type;
        this.srcIp = srcIp;
        this.destIp = destIp;
        this.packetNum = packetNum;
    }

    public NodeInfo(String name, String type, Integer packetNum) {
        super();
        this.name = name;
        this.type = type;
        this.packetNum = packetNum;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getSrcMac() {
        return srcMac;
    }

    public void setSrcMac(String srcMac) {
        this.srcMac = srcMac;
    }

    public String getDestMac() {
        return destMac;
    }

    public void setDestMac(String destMac) {
        this.destMac = destMac;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }

    public String getAz() {
        return az;
    }

    public void setAz(String az) {
        this.az = az;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDestIp() {
        return destIp;
    }

    public void setDestIp(String destIp) {
        this.destIp = destIp;
    }

    public Integer getPacketNum() {
        return packetNum;
    }

    public void setPacketNum(Integer packetNum) {
        this.packetNum = packetNum;
    }
}
