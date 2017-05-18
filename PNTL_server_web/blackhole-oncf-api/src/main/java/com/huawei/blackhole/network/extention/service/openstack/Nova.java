package com.huawei.blackhole.network.extention.service.openstack;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.FspServiceName;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.common.utils.http.Const;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.extention.bean.openstack.nova.OsAvailabilityZones;
import com.huawei.blackhole.network.extention.bean.openstack.nova.OsInterfaces;
import com.huawei.blackhole.network.extention.bean.openstack.nova.ServerDetail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("novaService")
public class Nova {
    private static final Logger LOG = LoggerFactory.getLogger(Nova.class);

    private static final ConfUtil CONF = ConfUtil.getInstance();
    private static final String V2_VERSION = "/v2";

    private static final String SERVICE_NAME = FspServiceName.NOVA;

    /**
     * Get server detail.
     *
     * @param pod
     *            Nova service pod.
     * @param token
     *            Token.
     * @param tenantId
     *            Tenant id to query.
     * @param serverId
     *            Target server.
     * @return Server detail.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public ServerDetail getServerDetail(String pod, String token, String tenantId, String serverId)
            throws ClientException, ConfigLostException {
        String url = getUrlServer(getEndpoint(pod), tenantId, serverId);
        LOG.info("getServerDetail url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, ServerDetail.class, header);
    }

    /**
     * List interface attached to a server.
     *
     * @param pod
     *            Nova service pod.
     * @param token
     *            Token.
     * @param tenantId
     *            Tenant id to query.
     * @param serverId
     *            Target server.
     * @return List of ports attached to a server.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public OsInterfaces getServerOsInterfaces(String pod, String token, String tenantId, String serverId)
            throws ClientException, ConfigLostException {
        String url = getUrlOsInterface(getEndpoint(pod), tenantId, serverId);
        LOG.info("get server os interfaces url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, OsInterfaces.class, header);
    }

    public OsAvailabilityZones getOsAvailabilityZones(String pod, String token, String tenantId)
            throws ClientException, ConfigLostException {
        String url = getUrlOsAvailabilityZones(getEndpoint(pod), tenantId);
        LOG.info("get os availability zones url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, OsAvailabilityZones.class, header);
    }

    private String getUrlServer(String endpoint, String tenantId, String serverId) {
        return endpoint + V2_VERSION + "/" + tenantId + "/servers" + "/" + serverId;
    }

    private String getUrlOsInterface(String endpoint, String tenantId, String serverId) {
        return getUrlServer(endpoint, tenantId, serverId) + "/os-interface";
    }

    private String getUrlOsAvailabilityZones(String endpoint, String tenantId) {
        return endpoint + V2_VERSION + "/" + tenantId + "os-availability-zone/detail";
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
