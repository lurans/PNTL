package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OncfConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("cascading_ip")
    private String cascadingIp;

    @JsonProperty("os_tenant_name")
    private String osTenantName;

    @JsonProperty("os_username")
    private String osUsername;

    @JsonProperty("os_password")
    private String osPassword;

    @JsonProperty("os_auth_url")
    private String osAuthUrl;

    @JsonProperty("cna_ssh_key")
    private String cnaSshKey;

    public String getCascadingIp() {
        return cascadingIp;
    }

    public void setCascadingIp(String cascadingIp) {
        this.cascadingIp = cascadingIp;
    }

    public String getOsTenantName() {
        return osTenantName;
    }

    public void setOsTenantName(String osTenantName) {
        this.osTenantName = osTenantName;
    }

    public String getOsUsername() {
        return osUsername;
    }

    public void setOsUsername(String osUsername) {
        this.osUsername = osUsername;
    }

    public String getOsPassword() {
        return osPassword;
    }

    public void setOsPassword(String osPassword) {
        this.osPassword = osPassword;
    }

    public String getOsAuthUrl() {
        return osAuthUrl;
    }

    public void setOsAuthUrl(String osAuthUrl) {
        this.osAuthUrl = osAuthUrl;
    }

    public Map<String, Object> convertToMap() throws ApplicationException {
        if (containsEmptyField()) {
            throw new ApplicationException(ExceptionType.CLIENT_ERR, "not enough infomation provided");
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("cascading_ip", cascadingIp);
        data.put("OS_TENANT_NAME", osTenantName);
        data.put("OS_USERNAME", osUsername);
        data.put("OS_PASSWORD", osPassword);
        data.put("OS_AUTH_URL", osAuthUrl);
        // 不起作用，会被外层函数覆盖
        data.put("cna_ssh_key", cnaSshKey);
        return data;
    }

    private boolean containsEmptyField() {
        return StringUtils.isEmpty(cascadingIp) || StringUtils.isEmpty(osTenantName) || StringUtils.isEmpty(osUsername)
                || StringUtils.isEmpty(osPassword) || StringUtils.isEmpty(osAuthUrl) || StringUtils.isEmpty(cnaSshKey);
    }

    public void setByMap(Map<String, Object> data) {
        if (data == null) {
            return;
        }
        this.cascadingIp = MapUtils.getAsStr(data, "cascading_ip");
        this.osTenantName = MapUtils.getAsStr(data, "OS_TENANT_NAME");
        this.osUsername = MapUtils.getAsStr(data, "OS_USERNAME");
        this.osPassword = MapUtils.getAsStr(data, "OS_PASSWORD");
        this.osAuthUrl = MapUtils.getAsStr(data, "OS_AUTH_URL");
        this.cnaSshKey = MapUtils.getAsStr(data, "cna_ssh_key");
    }

}
