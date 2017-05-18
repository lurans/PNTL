package com.huawei.blackhole.network.extention.service.vrm;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.FspServiceName;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.common.utils.RestClientUtils;
import com.huawei.blackhole.network.common.utils.http.Const;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.extention.bean.vrm.Sites;
import com.huawei.blackhole.network.extention.bean.vrm.Vms;
import com.huawei.blackhole.network.extention.bean.vrm.Vms.VmInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("vrmService")
public class Vrm {
    private static final Logger LOG = LoggerFactory.getLogger(Vrm.class);

    private static final ConfUtil CONF = ConfUtil.getInstance();

    private static final String X_AUTH_USER = "X-Auth-User";
    private static final String X_AUTH_KEY = "X-Auth-Key";
    private static final String X_AUTH_USER_TYPE = "X-Auth-UserType";
    private static final String X_AUTH_AUTH_TYPE = "X-Auth-AuthType";
    private static final String X_ENCRIPT_ALGORITHM = "X-ENCRIPT-ALGORITHM";

    private static final String SERVICE_NAME = FspServiceName.VRM;

    private static final String NAME = "name";

    public String getToken(String pod) throws ConfigLostException, ClientException {
        String url = getUrlToken(getEndpoint(pod));
        LOG.info("pod= {}, get vrmToken Url={}", pod, url);

        Map<String, String> header = new HashMap<>();
        header.put(X_AUTH_USER, CONF.getConfAsString(getConfKey(SERVICE_NAME, pod, Constants.PARAM_USER)));
        header.put(X_AUTH_KEY, CONF.getConfAsString(getConfKey(SERVICE_NAME, pod, Constants.PARAM_PASSWORD)));
        header.put(X_AUTH_USER_TYPE, "2");
        header.put(X_AUTH_AUTH_TYPE, "0");
        header.put(X_ENCRIPT_ALGORITHM, "1");
        header.put("Accept", "application/json;version=5.1;charset=UTF-8");

        RestResp response = RestClientExt.post(url, null, null, header);
        return RestClientUtils.getAuthToken(response.getHeader());
    }

    public Sites getSites(String pod, String token) throws ClientException, ConfigLostException {
        String url = getUrlSites(getEndpoint(pod));

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, Sites.class, header);
    }

    public Vms getVmInfoByVmName(String pod, String token, String site, String vmName) throws ConfigLostException,
            ClientException {
        String url = getUrlVms(getEndpoint(pod), site, null);

        Parameter param = new Parameter();
        param.put(NAME, vmName);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, param, Vms.class, header);
    }

    public VmInfo getVmInfo(String pod, String token, String site, String vmId) throws ConfigLostException,
            ClientException {
        String url = getUrlVms(getEndpoint(pod), site, vmId);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, VmInfo.class, header);
    }

    private String getUrlToken(String endpoint) {
        return endpoint + "/service/session";
    }

    private String getUrlSites(String endpoint) {
        return endpoint + "/service/sites";
    }

    private String getUrlVms(String endpoint, String site, String vmId) {
        String url = getUrlSites(endpoint) + "/" + site + "/vms";
        if (vmId == null) {
            return url;
        }
        return url + "/" + vmId;
    }

    private String getEndpoint(String region) throws ConfigLostException {
        if (null == region) {
            region = CONF.getConfAsString(Config.FSP_CASCADING_REGION);
        }
        return CONF.getConfAsString(getConfKey(SERVICE_NAME, region, null));
    }

    private String getConfKey(String serviceName, String region, String exetendParam) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(serviceName);
        stringBuilder.append(Constants.SYMBOL_DOT);
        stringBuilder.append(region);
        if (null != exetendParam) {
            stringBuilder.append(Constants.SYMBOL_DOT);
            stringBuilder.append(exetendParam);
        }
        return stringBuilder.toString();
    }
}
