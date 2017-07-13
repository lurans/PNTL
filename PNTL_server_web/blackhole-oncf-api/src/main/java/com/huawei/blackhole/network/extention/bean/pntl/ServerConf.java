package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerConf implements Serializable{
    private static final long serialVersionUID = -7398875983262356647L;
    @JsonProperty("probe_period")
    private String probePeriod;
    @JsonProperty("port_count")
    private String portCount;
    @JsonProperty("report_period")
    private String reportPeriod;
    @JsonProperty("delay_threshold")
    private String delayThreshold;
    @JsonProperty("dscp")
    private String dscp;
    @JsonProperty("lossPkg_timeout")
    private String lossPkgTimeout;
    @JsonProperty("big_pkg_rate")
    private String bigPkgRate;

    public String getProbePeriod() {
        return probePeriod;
    }

    public void setProbePeriod(String probePeriod) {
        this.probePeriod = probePeriod;
    }

    public String getPortCount() {
        return portCount;
    }

    public void setPortCount(String portCount) {
        this.portCount = portCount;
    }

    public String getReportPeriod() {
        return reportPeriod;
    }

    public void setReportPeriod(String reportPeriod) {
        this.reportPeriod = reportPeriod;
    }

    public String getDelayThreshold() {
        return delayThreshold;
    }

    public void setDelayThreshold(String delayThreshold) {
        this.delayThreshold = delayThreshold;
    }

    public String getDscp() {
        return dscp;
    }

    public void setDscp(String dscp) {
        this.dscp = dscp;
    }

    public String getLossPkgTimeout() {
        return lossPkgTimeout;
    }

    public void setLossPkgTimeout(String lossPkgTimeout) {
        this.lossPkgTimeout = lossPkgTimeout;
    }

    public String getBigPkgRate() {
        return bigPkgRate;
    }

    public void setBigPkgRate(String bigPkgRate) {
        this.bigPkgRate = bigPkgRate;
    }
}
