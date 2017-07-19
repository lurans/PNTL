package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PntlServerInfo implements Serializable{
    private static final long serialVersionUID = 8715310729579294093L;
    @JsonProperty("pingListFlag")
    private String pingListFlag;
    @JsonProperty("probe_period")
    private String probePeriod;
    @JsonProperty("port_count")
    private String portCount;
    @JsonProperty("report_period")
    private String reportPeriod;
    @JsonProperty("pkg_count")
    private String pkgCount;
    @JsonProperty("delay_threshold")
    private String delayThreshold;
    @JsonProperty("dscp")
    private String dscp;
    @JsonProperty("lossPkg_timeout")
    private String lossPkgTimeout;

    public String getPingListFlag() {
        return pingListFlag;
    }

    public void setPingListFlag(String pingListFlag) {
        this.pingListFlag = pingListFlag;
    }

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

    public String getPkgCount() {
        return pkgCount;
    }

    public void setPkgCount(String pkgCount) {
        this.pkgCount = pkgCount;
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
}
