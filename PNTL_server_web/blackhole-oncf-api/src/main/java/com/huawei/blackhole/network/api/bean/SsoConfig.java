package com.huawei.blackhole.network.api.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SsoConfig implements Serializable {
    private static final long serialVersionUID = -7432089182358410713L;

    @JsonProperty("sso_ip")
    private String ssoIp;

    @JsonProperty("sso_port")
    private String ssoPort;

    public boolean setted() {
        return ssoIp != null && ssoPort != null;
    }

    public String getSsoIp() {
        return ssoIp;
    }

    public void setSsoIp(String ssoIp) {
        this.ssoIp = ssoIp;
    }

    public String getSsoPort() {
        return ssoPort;
    }

    public void setSsoPort(String ssoPort) {
        this.ssoPort = ssoPort;
    }

    public String toSocket() {
        if (setted()) {
            return ssoIp + ":" + ssoPort;
        }
        return "";
    }

}
