package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PntlHostInfo implements Serializable{
    private static final long serialVersionUID = 7937424430479648565L;
    @JsonProperty("hostsInfo")
    private List<HostInfo> hostInfo = null;

    public List<HostInfo> getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(List<HostInfo> hostInfo) {
        this.hostInfo = hostInfo;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class HostInfo implements Serializable{
            @JsonProperty("ip")
            private String ip;
            @JsonProperty("pod")
            private String pod;
            @JsonProperty("az")
            private String az;
            @JsonProperty("os")
            private String os;

            public String getIp() {
                return ip;
            }

            public void setIp(String ip) {
                this.ip = ip;
            }

            public String getPod() {
                return pod;
            }

            public void setPod(String pod) {
                this.pod = pod;
            }

            public String getAz() {
                return az;
            }

            public void setAz(String az) {
                this.az = az;
            }

            public String getOs() {
                return os;
            }

            public void setOs(String os) {
                this.os = os;
            }
    }
}
