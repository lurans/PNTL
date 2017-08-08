package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.MapUtils;
import com.huawei.blackhole.network.extention.service.conf.PntlConfigService;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PntlConfig implements Serializable{
    private static final long serialVersionUID = 3876713150129808504L;

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
    @JsonProperty("lossRate_threshold")
    private String lossRateThreshold;
    @JsonProperty("dscp")
    private String dscp;
    @JsonProperty("lossPkg_timeout")
    private String lossPkgTimeout;

    @JsonProperty("ak")
    private String ak;
    @JsonProperty("sk")
    private String sk;
    @JsonProperty("basicToken")
    private static String basicToken;

    @JsonProperty("repo_url")
    private String repoUrl;

    private String eulerRepoUrl;
    private String suseRepoUrl;
    private String installScriptRepoUrl;

    @JsonProperty("kafka_ip")
    private String kafkaIp;
    @JsonProperty("topic")
    private String topic;
    @JsonProperty("dropPkgThresh")
    private String dropPkgThresh;
    @JsonProperty("package_size")
    private String packageSize;

    public PntlConfig(){
        this.probePeriod = "10";
        this.portCount = "5";
        this.reportPeriod = "30";
        this.pkgCount = "0";
        this.delayThreshold = "1";
        this.lossRateThreshold = "20";
        this.dscp = "0";
        this.lossPkgTimeout = "1";
        this.dropPkgThresh = "2";
        this.packageSize = "1000";
    }

    public String getPackageSize() {
        return packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public String getDropPkgThresh() {
        return dropPkgThresh;
    }

    public void setDropPkgThresh(String dropPkgThresh) {
        this.dropPkgThresh = dropPkgThresh;
    }

    public String getEulerRepoUrl() {
        return eulerRepoUrl;
    }

    public void setEulerRepoUrl(String eulerRepoUrl) {
        this.eulerRepoUrl = eulerRepoUrl;
    }

    public String getSuseRepoUrl() {
        return suseRepoUrl;
    }

    public void setSuseRepoUrl(String suseRepoUrl) {
        this.suseRepoUrl = suseRepoUrl;
    }

    public String getInstallScriptRepoUrl() {
        return installScriptRepoUrl;
    }

    public void setInstallScriptRepoUrl(String installScriptRepoUrl) {
        this.installScriptRepoUrl = installScriptRepoUrl;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
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

    public String getLossRateThreshold() {
        return lossRateThreshold;
    }

    public void setLossRateThreshold(String lossRateThreshold) {
        this.lossRateThreshold = lossRateThreshold;
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

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public static String getBasicToken() {
        return "basic" + " " + basicToken;
    }

    public void setBasicToken(String basicToken) {
        this.basicToken = basicToken;
    }

    public void setByMap(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        //agent参数配置
        this.delayThreshold = (null ==  MapUtils.getAsStr(data, "delay_threshold") ?
                this.delayThreshold : MapUtils.getAsStr(data, "delay_threshold"));
        this.lossRateThreshold = (null ==  MapUtils.getAsStr(data, "lossRate_threshold") ?
                this.lossRateThreshold : MapUtils.getAsStr(data, "lossRate_threshold"));
        this.probePeriod = (null ==  MapUtils.getAsStr(data, "probe_period") ?
                this.probePeriod : MapUtils.getAsStr(data, "probe_period"));
        this.pkgCount = (null ==  MapUtils.getAsStr(data, "pkg_count") ?
                this.pkgCount : MapUtils.getAsStr(data, "pkg_count"));
        this.portCount = (null ==  MapUtils.getAsStr(data, "port_count") ?
                this.portCount : MapUtils.getAsStr(data, "port_count"));
        this.dscp = (null ==  MapUtils.getAsStr(data, "dscp") ?
                this.dscp : MapUtils.getAsStr(data, "dscp"));
        this.lossPkgTimeout = (null ==  MapUtils.getAsStr(data, "lossPkg_timeout") ?
                this.lossPkgTimeout : MapUtils.getAsStr(data, "lossPkg_timeout"));
        this.reportPeriod = (null ==  MapUtils.getAsStr(data, "report_period") ?
                this.reportPeriod : MapUtils.getAsStr(data, "report_period"));
        this.dropPkgThresh = (null ==  MapUtils.getAsStr(data, "dropPkgThresh") ?
                this.dropPkgThresh : MapUtils.getAsStr(data, "dropPkgThresh"));
        this.packageSize = (null ==  MapUtils.getAsStr(data, "package_size") ?
                this.packageSize :  MapUtils.getAsStr(data, "package_size"));

        this.ak = MapUtils.getAsStr(data, "ak");
        this.sk = MapUtils.getAsStr(data, "sk");
        this.basicToken = MapUtils.getAsStr(data, "basicToken");
        this.repoUrl = MapUtils.getAsStr(data, "repo_url");
        this.eulerRepoUrl = MapUtils.getAsStr(data, "eulerRepoUrl");
        this.suseRepoUrl = MapUtils.getAsStr(data, "suseRepoUrl");
        this.installScriptRepoUrl = MapUtils.getAsStr(data, "installScriptRepoUrl");
        this.kafkaIp = MapUtils.getAsStr(data, "kafka_ip");
        this.topic = MapUtils.getAsStr(data, "topic");
    }

    private boolean containsEmptyField() {
        return StringUtils.isEmpty(delayThreshold) || StringUtils.isEmpty(lossRateThreshold)
                || StringUtils.isEmpty(probePeriod) || StringUtils.isEmpty(pkgCount)
                || StringUtils.isEmpty(portCount) || StringUtils.isEmpty(reportPeriod)
                || StringUtils.isEmpty(dscp) || StringUtils.isEmpty(lossPkgTimeout)
                || StringUtils.isEmpty(ak) || StringUtils.isEmpty(sk) || StringUtils.isEmpty(repoUrl)
                || StringUtils.isEmpty(topic) || StringUtils.isEmpty(dropPkgThresh)
                || StringUtils.isEmpty(kafkaIp) || StringUtils.isEmpty(packageSize);

    }

    public Map<String, Object> convertToMap() throws ApplicationException {
        if (containsEmptyField()) {
            throw new ApplicationException(ExceptionType.CLIENT_ERR, "not enough infomation provided");
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("probe_period", probePeriod);
        data.put("port_count", portCount);
        data.put("report_period", reportPeriod);
        data.put("pkg_count", pkgCount);
        data.put("delay_threshold", delayThreshold);
        data.put("lossRate_threshold", lossRateThreshold);
        data.put("dscp", dscp);
        data.put("lossPkg_timeout", lossPkgTimeout);
        data.put("dropPkgThresh", dropPkgThresh);
        data.put("ak", ak);
        data.put("sk", sk);
        data.put("basicToken", basicToken);
        data.put("repo_url", repoUrl);
        data.put("eulerRepoUrl", eulerRepoUrl);
        data.put("suseRepoUrl", suseRepoUrl);
        data.put("installScriptRepoUrl", installScriptRepoUrl);
        data.put("kafka_ip", kafkaIp);
        data.put("topic", topic);
        data.put("dropPkgThresh", dropPkgThresh);
        data.put("package_size", packageSize);

        return data;
    }
}
