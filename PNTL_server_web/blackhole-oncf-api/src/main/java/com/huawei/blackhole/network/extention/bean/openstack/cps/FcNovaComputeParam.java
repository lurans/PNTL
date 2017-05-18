package com.huawei.blackhole.network.extention.bean.openstack.cps;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName("cfg")
public class FcNovaComputeParam implements Serializable {
    private static final long serialVersionUID = -8082889130506360788L;
    
    @JsonProperty("fusioncompute_fc_pwd")
    private String fcPasswd;
    
    @JsonProperty("fusioncompute_fc_user")
    private String fcUser;
    
    @JsonProperty("fusioncompute_fc_ip")
    private String fcIp;

    public String getFcPasswd() {
        return fcPasswd;
    }

    public void setFcPasswd(String fcPasswd) {
        this.fcPasswd = fcPasswd;
    }

    public String getFcUser() {
        return fcUser;
    }

    public void setFcUser(String fcUser) {
        this.fcUser = fcUser;
    }

    public String getFcIp() {
        return fcIp;
    }

    public void setFcIp(String fcIp) {
        this.fcIp = fcIp;
    }        
}
