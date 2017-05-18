package com.huawei.blackhole.network.extention.service.openstack;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.common.utils.http.Const;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.extention.bean.openstack.cps.ComponentTemplates;
import com.huawei.blackhole.network.extention.bean.openstack.cps.DnsServerParam;
import com.huawei.blackhole.network.extention.bean.openstack.cps.FcNovaComputeParam;
import com.huawei.blackhole.network.extention.bean.openstack.cps.HostInfo;
import com.huawei.blackhole.network.extention.bean.openstack.cps.Instances;
import com.huawei.blackhole.network.extention.bean.openstack.cps.NeutronTemplateParam;

@Service("cpsService")
public class Cps {

    private static final Logger LOG = LoggerFactory.getLogger(Cps.class);
    private static final ConfUtil CONF = ConfUtil.getInstance();

    private static final String SERVICE = "service";
    private static final String TEMPLATE = "template";

    private static final String V1_VERSION = "/v1";
    private static final String SERVICE_NAME = "cps";

    private static final String SERVICE_DNS = "dns";
    private static final String SERVIVE_NOVA = "nova";
    private static final String SERVICE_NEUTRON = "neutron";
    private static final String TEMPLATE_DNSSERVER = "dns-server";

    private static final String QUERY_TEMPLATE_DETAIL_URL = "%s/services/%s/componenttemplates";
    private static final String QUERY_TEMPLATE_PARAMS_URL = "%s/services/%s/componenttemplates/%s/params";

    // instances?service=neutron&template=neutron-l3-nat-agent01

    public DnsServerParam getDnsServerParams(String region, String token) throws ClientException, ConfigLostException {
        String url = getUrlCpsTemplateParams(getEndpoint(region), SERVICE_DNS, TEMPLATE_DNSSERVER);
        LOG.info("get dns server params url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, DnsServerParam.class, header);
    }

    public ComponentTemplates getComponentTemplates(String region, String service, String token)
            throws ClientException, ConfigLostException {
        String url = getUrlCpsTemplateDetail(getEndpoint(region), service);
        LOG.info("get component templates url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, ComponentTemplates.class, header);
    }

    public FcNovaComputeParam getFcNovaComputeParam(String region, String template, String token)
            throws ClientException, ConfigLostException {
        String url = getUrlCpsTemplateParams(getEndpoint(region), SERVIVE_NOVA, template);
        LOG.info("get fc nova compute param url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, FcNovaComputeParam.class, header);
    }

    public NeutronTemplateParam getCpsNeutronTemplateParam(String region, String template, String token)
            throws ClientException, ConfigLostException {
        String url = getUrlCpsTemplateParams(getEndpoint(region), SERVICE_NEUTRON, template);
        LOG.info("get cps neutron template param url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, NeutronTemplateParam.class, header);
    }

    public Instances getInstances(String region, String service, String template, String token) throws ClientException,
            ConfigLostException {
        String url = getUrlCps(getEndpoint(region)) + "/instances";
        Parameter param = new Parameter();
        if (null != service) {
            param.put(SERVICE, service);
        }
        if (null != template) {
            param.put(TEMPLATE, template);
        }

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, param, Instances.class, header);

    }

    public HostInfo getHostInfo(String region, String hostId, String token) throws ConfigLostException, ClientException {
        String url = getUrlCpsHostInfo(getUrlCps(getEndpoint(region)), hostId);
        LOG.info("get host info url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, HostInfo.class, header);
    }

    private String getUrlCpsTemplateParams(String endpoint, String serviceName, String templateName) {
        return String.format(QUERY_TEMPLATE_PARAMS_URL, getUrlCps(endpoint), serviceName, templateName);
    }

    private String getUrlCpsTemplateDetail(String endpoint, String service) {
        String newService = (null == service) ? "all" : service;
        return String.format(QUERY_TEMPLATE_DETAIL_URL, getUrlCps(endpoint), newService);
    }

    private String getUrlCpsHostInfo(String endpoint, String hostId) {
        return endpoint + "/hosts/" + hostId;
    }

    private String getUrlCps(String endpoint) {
        return endpoint + "/cps" + V1_VERSION;
    }

    private String getEndpoint(String region) throws ConfigLostException {
        if (null == region) {
            region = CONF.getConfAsString(Config.FSP_CASCADING_REGION);
        }
        return CONF.getConfAsString(getServiceRegion(SERVICE_NAME, region));
    }

    private String getServiceRegion(String serviceName, String region) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(serviceName);
        stringBuilder.append(Constants.SYMBOL_DOT);
        stringBuilder.append(region);
        return stringBuilder.toString();
    }
}
