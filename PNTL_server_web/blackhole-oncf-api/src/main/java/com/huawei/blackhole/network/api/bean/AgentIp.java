package com.huawei.blackhole.network.api.bean;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class AgentIp implements Serializable{
    private static final long serialVersionUID = -6599106694436322958L;
    @JsonProperty("agent_ip")
    private String agentIp;
    @JsonProperty("vbond_ip")
    private String vbondIp;

    public String getAgentIp() {
        return agentIp;
    }

    public void setAgentIp(String agentIp) {
        this.agentIp = agentIp;
    }

    public String getVbondIp() {
        return vbondIp;
    }

    public void setVbondIp(String vbondIp) {
        this.vbondIp = vbondIp;
    }
}
