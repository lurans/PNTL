package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmdSetJson implements Serializable{
    private static final long serialVersionUID = 8133966046317073019L;

    @JsonProperty("agentListType")
    private String agentListType;
    @JsonProperty("agentSNList")
    private List<String> agentSNList;
    @JsonProperty("timeout")
    private String timeout;
    @JsonProperty("userName")
    private String userName;
    @JsonProperty("cmdSet")
    private String cmdSet;
    @JsonProperty("cmdType")
    private String cmdType;

    public String getAgentListType() {
        return agentListType;
    }

    public void setAgentListType(String agentListType) {
        this.agentListType = agentListType;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCmdSet() {
        return cmdSet;
    }

    public void setCmdSet(String cmdSet) {
        this.cmdSet = cmdSet;
    }

    public String getCmdType() {
        return cmdType;
    }

    public void setCmdType(String cmdType) {
        this.cmdType = cmdType;
    }

    public List<String> getAgentSNList() {
        return agentSNList;
    }

    public void setAgentSNList(List<String> agentSNList) {
        this.agentSNList = agentSNList;
    }
}
