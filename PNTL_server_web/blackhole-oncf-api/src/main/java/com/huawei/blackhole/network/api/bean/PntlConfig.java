package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PntlConfig implements Serializable{
    private static final long serialVersionUID = 3876713150129808504L;

    @JsonProperty("delay_threshold")
    private String delayThreshold;
    @JsonProperty("lossRate_threshold")
    private String lossRateThreshold;
    @JsonProperty("probe_interval")
    private String probeInterval;
    @JsonProperty("pkg_count")
    private String pkgCount;

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

    public String getProbeInterval() {
        return probeInterval;
    }

    public void setProbeInterval(String probeInterval) {
        this.probeInterval = probeInterval;
    }

    public String getPkgCount() {
        return pkgCount;
    }

    public void setPkgCount(String pkgCount) {
        this.pkgCount = pkgCount;
    }

    public void setByMap(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        this.delayThreshold = MapUtils.getAsStr(data, "delay_threshold");
        this.lossRateThreshold = MapUtils.getAsStr(data, "lossRate_threshold");
        this.probeInterval = MapUtils.getAsStr(data, "probe_interval");
        this.pkgCount = MapUtils.getAsStr(data, "pkg_count");
    }

    private boolean containsEmptyField() {
        return StringUtils.isEmpty(delayThreshold) || StringUtils.isEmpty(lossRateThreshold)
                || StringUtils.isEmpty(probeInterval) || StringUtils.isEmpty(pkgCount);
    }
    public Map<String, Object> convertToMap() throws ApplicationException {
        if (containsEmptyField()) {
            throw new ApplicationException(ExceptionType.CLIENT_ERR, "not enough infomation provided");
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("delay_threshold", delayThreshold);
        data.put("lossRate_threshold", lossRateThreshold);
        data.put("probe_interval", probeInterval);
        data.put("pkg_count", pkgCount);

        return data;
    }
}
