package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by y00214328 on 2017/5/20.
 */
public class PntlConfig implements Serializable{
    private static final long serialVersionUID = 3876713150129808504L;

    @JsonProperty("MessageSignature")
    private String messageSignature;

    @JsonProperty("Action")
    private String action;

    @JsonProperty("target")
    private String target;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("content")
    private Content content;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Content implements Serializable{
        private static final long serialVersionUID = 4588703190564152301L;
        @JsonProperty("agent-ip")
        private String agentIp;

        public String getAgentIp() {
            return agentIp;
        }

        public void setAgentIp(String agentIp) {
            this.agentIp = agentIp;
        }
    }

    public String getMessageSignature() {
        return messageSignature;
    }

    public void setMessageSignature(String messageSignature) {
        this.messageSignature = messageSignature;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }
}
