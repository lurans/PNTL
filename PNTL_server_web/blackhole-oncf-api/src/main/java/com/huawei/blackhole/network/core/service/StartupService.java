package com.huawei.blackhole.network.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.FspServiceName;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.AESUtil;
import com.huawei.blackhole.network.extention.bean.openstack.cps.ComponentTemplates;
import com.huawei.blackhole.network.extention.bean.openstack.cps.DnsServerParam;
import com.huawei.blackhole.network.extention.bean.openstack.cps.ComponentTemplates.Template;
import com.huawei.blackhole.network.extention.bean.openstack.cps.FcNovaComputeParam;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.Facing;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneCatalog;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneEndpointV3;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneTokenV3;
import com.huawei.blackhole.network.extention.service.openstack.Cps;

@Service("startupService")
public class StartupService {

    private static final Logger LOG = LoggerFactory.getLogger(StartupService.class);

    @Resource(name = "cpsService")
    private Cps cpsService;

    public Map<String, String> getServiceEndpoints(KeystoneTokenV3 adminToken) {

        Map<String, String> serviceURIMap = new HashMap<String, String>();

        List<KeystoneCatalog> catalogs = adminToken.getCatalog();
        for (KeystoneCatalog catalog : catalogs) {
            String catalogName = catalog.getName();
            if (isNeedRecordSevice(catalogName)) {
                List<KeystoneEndpointV3> endpoints = catalog.getEndpoints();
                for (KeystoneEndpointV3 endpoint : endpoints) {
                    if (endpoint.getIface().equals(Facing.PUBLIC)) {
                        String endpointUri = endpoint.getUrl().getProtocol() + "://" + endpoint.getUrl().getAuthority();
                        serviceURIMap.put(catalogName + Constants.SYMBOL_DOT + endpoint.getRegion(), endpointUri);
                    }
                }
            }
        }
        return serviceURIMap;
    }

    public Map<String, String> getRegionIpMap(String region, String token) throws ClientException, ConfigLostException {
        DnsServerParam dnsServerParam = null;
        Map<String, String> regionIpMap = new HashMap<String, String>();
        dnsServerParam = cpsService.getDnsServerParams(region, token);
        if (null == dnsServerParam || StringUtils.isEmpty(dnsServerParam.getAddress())) {
            String err = "empty dnsServerParam for region : " + region;
            throw new ClientException(ExceptionType.ENV_ERR, err);
        }
        String[] allAddressesInfo = dnsServerParam.getAddress().split(Constants.SYMBOL_COMMA);
//        LOG.info(dnsServerParam.getAddress());
        Pattern pattern = Pattern.compile("/(.+)/([0-9\\.]+)");
        for (String addressInfo : allAddressesInfo) {
            Matcher matcher = pattern.matcher(addressInfo);
            if (matcher.find()) {
                regionIpMap.put(matcher.group(Constants.NUM_ONE), matcher.group(Constants.NUM_TWO));
            }
        }
        return regionIpMap;
    }

    public Map<String, FcNovaComputeParam> getFcNovaComputeParamofRegions(List<String> regions, String token)
            throws ClientException, ConfigLostException {
        Map<String, FcNovaComputeParam> fcParamMap = new HashMap<String, FcNovaComputeParam>();
        for (String region : regions) {
            FcNovaComputeParam fcParam = getFcNovaComputeParamofOneRegion(region, token);
            if (null != fcParam) {
                fcParamMap.put(region, fcParam);
            }
        }
        return fcParamMap;
    }

    public FcNovaComputeParam getFcNovaComputeParamofOneRegion(String region, String token) throws ClientException,
            ConfigLostException {
        List<String> novaTemplateList = getNovaTemplateList(region, token);
        if (null == novaTemplateList || novaTemplateList.isEmpty()) {
            LOG.error("empty nova templates for region : " + region);
            return null;
        }
        FcNovaComputeParam fcParam = null;
        for (String template : novaTemplateList) {
            try {
                fcParam = cpsService.getFcNovaComputeParam(region, template, token);
            } catch (ClientException e) {
                LOG.warn(e.getLocalizedMessage(),e);
                continue;
            }
            if (null != fcParam && !StringUtils.isEmpty(fcParam.getFcIp()) && !StringUtils.isEmpty(fcParam.getFcUser())
                    && !StringUtils.isEmpty(fcParam.getFcPasswd())) {
                String oldPassword = fcParam.getFcPasswd();
                try {
                    fcParam.setFcPasswd(AESUtil.decrypt(oldPassword));
                } catch (Exception e) {
                    String errMsg = String.format("decode %s fail: %s", oldPassword, e.getLocalizedMessage());
                    throw new ClientException(ExceptionType.SERVER_ERR, errMsg);
                }
                return fcParam;
            }
        }
        LOG.error("can not find fcParam from endpoint {}", region);
        return null;
    }

    private List<String> getNovaTemplateList(String region, String token) throws ConfigLostException {
        List<String> novaTemplateList = new ArrayList<String>();
        ComponentTemplates novaTemplates = null;
        try {
            novaTemplates = cpsService.getComponentTemplates(region, FspServiceName.NOVA, token);
        } catch (ClientException e) {
            LOG.warn("get nova template list of region {} fail:", region, e);
            return null;
        }
        if (null == novaTemplates || null == novaTemplates.getTemplates()) {
            LOG.error("empty nova templates for region : " + region);
            return null;
        }
        for (Template template : novaTemplates.getTemplates()) {
            if (template.getName().startsWith(Constants.FC_NOVA_COMPUTE)) {
                novaTemplateList.add(template.getName());
            }
        }
        return novaTemplateList;
    }

    private boolean isNeedRecordSevice(String name) {
        if (StringUtils.equals(name, FspServiceName.NOVA) || StringUtils.equals(name, FspServiceName.NEUTRON)
                || StringUtils.equals(name, FspServiceName.CPS) || StringUtils.equals(name, FspServiceName.KEYSTONE)) {
            return true;
        }
        return false;
    }
}
