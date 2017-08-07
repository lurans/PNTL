package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentConfig implements Serializable{
    private static final long serialVersionUID = -2150230682817940440L;
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
    @JsonProperty("kafka_ip")
    private String kafkaIp;
    @JsonProperty("topic")
    private String topic;
    @JsonProperty("vbondIp_flag")
    private String vbondIpFlag;
    @JsonProperty("dropPkgThresh")
    private String dropPkgThresh;
    @JsonProperty("package_size")
    private String packageSize;
    @JsonProperty("pingList")
    private Map<String, List<String>> pingList;

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
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

    public String getKafkaIp() {
        return kafkaIp;
    }

    public void setKafkaIp(String kafkaIp) {
        this.kafkaIp = kafkaIp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getVbondIpFlag() {
        return vbondIpFlag;
    }

    public void setVbondIpFlag(String vbondIpFlag) {
        this.vbondIpFlag = vbondIpFlag;
    }

    public String getDropPkgThresh() {
        return dropPkgThresh;
    }

    public void setDropPkgThresh(String dropPkgThresh) {
        this.dropPkgThresh = dropPkgThresh;
    }

    public Map<String, List<String>> getPingList() {
        return pingList;
    }

    public void setPingList(Map<String, List<String>> pingList) {
        this.pingList = pingList;
    }

    private String pingListToString(Map<String, List<String>> pingList){
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(pingList);
        } catch (IOException e){
            e.printStackTrace();
        }
        return jsonStr;
    }

    @Override
    public String toString(){
        return "{"
                + "\"probe_period\":\"" + probePeriod + "\","
                + "\"port_count\":\"" + portCount + "\","
                + "\"report_period\":\"" + reportPeriod + "\","
                + "\"pkg_count\":\"" + pkgCount + "\","
                + "\"delay_threshold\":\"" + delayThreshold + "\","
                + "\"dscp\":\"" + dscp + "\","
                + "\"lossPkg_timeout\":\"" + lossPkgTimeout + "\","
                + "\"kafka_ip\":\"" + kafkaIp + "\","
                + "\"topic\":\"" + topic + "\","
                + "\"vbondIp_flag\":\"" + vbondIpFlag + "\","
                + "\"dropPkgThresh\":\"" + dropPkgThresh + "\","
                + "\"pingList\":" + pingListToString(pingList)
                + "}";
    }
}
