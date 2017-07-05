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
        this.delayThreshold = MapUtils.getAsStr(data, "delay_threshold");
        this.lossRateThreshold = MapUtils.getAsStr(data, "lossRate_threshold");
        this.probePeriod = MapUtils.getAsStr(data, "probe_period");
        this.pkgCount = MapUtils.getAsStr(data, "pkg_count");
        this.portCount = MapUtils.getAsStr(data, "port_count");
        this.dscp = MapUtils.getAsStr(data, "dscp");
        this.lossPkgTimeout = MapUtils.getAsStr(data, "lossPkg_timeout");
        this.reportPeriod = MapUtils.getAsStr(data, "report_period");
        this.ak = MapUtils.getAsStr(data, "ak");
        this.sk = MapUtils.getAsStr(data, "sk");
        this.basicToken = MapUtils.getAsStr(data, "basicToken");
    }

    private boolean containsEmptyField() {
        return StringUtils.isEmpty(delayThreshold) || StringUtils.isEmpty(lossRateThreshold)
                || StringUtils.isEmpty(probePeriod) || StringUtils.isEmpty(pkgCount)
                || StringUtils.isEmpty(portCount) || StringUtils.isEmpty(reportPeriod)
                || StringUtils.isEmpty(dscp) || StringUtils.isEmpty(lossPkgTimeout)
                || StringUtils.isEmpty(ak) || StringUtils.isEmpty(sk);
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
        data.put("ak", ak);
        data.put("sk", sk);

        data.put("basicToken", basicToken);

        return data;
    }
}
