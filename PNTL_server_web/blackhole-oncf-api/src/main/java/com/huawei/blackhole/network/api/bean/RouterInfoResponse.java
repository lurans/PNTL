package com.huawei.blackhole.network.api.bean;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RouterInfoResponse implements Serializable {

    private static final long serialVersionUID = -2665708124031387657L;

    @JsonProperty("cna_input")
    private List<NodeInfo> cnaInputRouter;

    @JsonProperty("cna_output")
    private List<NodeInfo> cnaOutputRouter;

    @JsonProperty("l2gw_input")
    private List<NodeInfo> l2gwInput;

    @JsonProperty("l2gw_output")
    private List<NodeInfo> l2gwOutput;

    @JsonProperty("rf_input")
    private List<NodeInfo> rfInput;

    @JsonProperty("rf_output")
    private List<NodeInfo> rfOutput;

    @JsonProperty("snat_input")
    private List<NodeInfo> snatInput;

    @JsonProperty("snat_output")
    private List<NodeInfo> snatOutput;

    public List<NodeInfo> getCnaInputRouter() {
        return cnaInputRouter;
    }

    public void setCnaInputRouter(List<NodeInfo> cnaInputRouter) {
        this.cnaInputRouter = cnaInputRouter;
    }

    public List<NodeInfo> getCnaOutputRouter() {
        return cnaOutputRouter;
    }

    public void setCnaOutputRouter(List<NodeInfo> cnaOutputRouter) {
        this.cnaOutputRouter = cnaOutputRouter;
    }

    public List<NodeInfo> getL2gwInput() {
        return l2gwInput;
    }

    public void setL2gwInput(List<NodeInfo> l2gwInput) {
        this.l2gwInput = l2gwInput;
    }

    public List<NodeInfo> getL2gwOutput() {
        return l2gwOutput;
    }

    public void setL2gwOutput(List<NodeInfo> l2gwOutput) {
        this.l2gwOutput = l2gwOutput;
    }

    public List<NodeInfo> getRfInput() {
        return rfInput;
    }

    public void setRfInput(List<NodeInfo> rfInput) {
        this.rfInput = rfInput;
    }

    public List<NodeInfo> getRfOutput() {
        return rfOutput;
    }

    public void setRfOutput(List<NodeInfo> rfOutput) {
        this.rfOutput = rfOutput;
    }

    public List<NodeInfo> getSnatInput() {
        return snatInput;
    }

    public void setSnatInput(List<NodeInfo> snatInput) {
        this.snatInput = snatInput;
    }

    public List<NodeInfo> getSnatOutput() {
        return snatOutput;
    }

    public void setSnatOutput(List<NodeInfo> snatOutput) {
        this.snatOutput = snatOutput;
    }

    @JsonProperty("status")
    private String status; // SUCCESS为完成，ERROR为错误

    @JsonProperty("err_msg")
    private String errorInfo;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }

}
