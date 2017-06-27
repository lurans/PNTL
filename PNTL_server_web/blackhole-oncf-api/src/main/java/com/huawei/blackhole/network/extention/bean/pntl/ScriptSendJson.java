package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by y00214328 on 2017/6/7.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptSendJson implements Serializable{
    private static final long serialVersionUID = -1750429675605739300L;

    @JsonProperty("filename")
    private String filename;

    @JsonProperty("path")
    private String path;

    @JsonProperty("repoUrl")
    private String repoUrl;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("userName")
    private String userName;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("override")
    private String override;

    @JsonProperty("timeout")
    private String timeout;

    @JsonProperty("token")
    private String token;

    @JsonProperty("agentListType")
    private String agentListType;

    @JsonProperty("agentSNList")
    private List<String> agentSNList;

    @JsonProperty("groupType")
    private String groupType;

    public ScriptSendJson(){
        this.mode = "";
        this.override = "true";
        this.timeout = "10000";
        this.token = "123";
        this.agentListType = "0";
        this.groupType = "";
        this.topic = "pntl";
        this.tag = "pntl";
        this.userName = "root";
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOverride() {
        return override;
    }

    public void setOverride(String override) {
        this.override = override;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAgentListType() {
        return agentListType;
    }

    public void setAgentListType(String agentListType) {
        this.agentListType = agentListType;
    }

    public List<String> getAgentSNList() {
        return agentSNList;
    }

    public void setAgentSNList(List<String> agentSNList) {
        this.agentSNList = agentSNList;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }
}
