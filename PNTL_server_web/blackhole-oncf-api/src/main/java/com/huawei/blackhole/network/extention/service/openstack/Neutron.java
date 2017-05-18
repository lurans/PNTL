package com.huawei.blackhole.network.extention.service.openstack;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.FspServiceName;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.common.utils.http.Const;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Agents;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.EipFloatingFip;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.FloatingIps;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Network;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.PortDetail;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Routers;

@Service("neutronService")
public class Neutron {
    private static final Logger LOG = LoggerFactory.getLogger(Neutron.class);
    private static final ConfUtil CONF = ConfUtil.getInstance();

    private static final String V2_VERSION = "/v2.0";
    private static final String TENANT_ID = "tenant_id";
    private static final String NETWORK_ID = "network_id";
    private static final String DEVICE_ID = "device_id";
    private static final String NAME = "name";
    private static final String DEVICE_OWNER = "device_owner";
    private static final String FLOATING_IP_ADDR = "floating_ip_address";
    private static final String FIXED_IP_ADDR = "fixed_ip_address";
    private static final String FIXED_IPS_SUBNET = "subnet_id";
    private static final String AGENT_TYPE = "agent_type";
    private static final String HOST = "host";

    private static final String SERVICE_NAME = FspServiceName.NEUTRON;

    /**
     * List routers.
     *
     * @param pod
     *            If null, query from cascading layer. Other query from specify
     *            pod.
     * @param token
     *            Token.
     * @param tenantId
     *            Tenant id.
     * @return Result.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public Routers listRouter(String pod, String token, String tenantId) throws ClientException, ConfigLostException {

        String url = getUrlRouter(getEndpoint(pod), null);
        LOG.info("list tenant router url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        Parameter param = new Parameter();
        param.put(TENANT_ID, tenantId);

        return RestClientExt.get(url, param, Routers.class, header);
    }

    /**
     * List ports filtered after specified condition.
     *
     * @param pod
     *            If null, query from cascading layer. Other query from specify
     *            pod.
     * @param token
     *            Token.
     * @param tenantId
     *            Tenant id.
     * @param networkId
     *            Network id.
     * @param deviceId
     *            Device id.
     * @param portName
     *            Port name.
     * @return Result.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public Ports listPorts(String pod, String token, String tenantId, String networkId, String deviceId, String portName)
            throws ClientException, ConfigLostException {

        Parameter param = new Parameter();
        if (tenantId != null) {
            param.put(TENANT_ID, tenantId);
        }
        if (networkId != null) {
            param.put(NETWORK_ID, networkId);
        }
        if (deviceId != null) {
            param.put(DEVICE_ID, deviceId);
        }
        if (portName != null) {
            param.put(NAME, portName);
        }

        return listPorts(getEndpoint(pod), token, param);
    }

    /**
     * List ports filtered after specified condition.
     *
     * @param pod
     *            If null, query from cascading layer. Other query from specify
     *            pod.
     * @param token
     *            Token.
     * @param tenantId
     *            Tenant id.
     * @param networkId
     *            Network id.
     * @param deviceId
     *            Device id.
     * @param portName
     *            Port name.
     * @param deviceOwner
     *            Device owner.
     * @return Result.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public Ports listPorts(String pod, String token, String tenantId, String networkId, String deviceId,
            String portName, String deviceOwner, String subnetId) throws ClientException, ConfigLostException {

        Parameter param = new Parameter();
        if (tenantId != null) {
            param.put(TENANT_ID, tenantId);
        }
        if (networkId != null) {
            param.put(NETWORK_ID, networkId);
        }
        if (deviceId != null) {
            param.put(DEVICE_ID, deviceId);
        }
        if (portName != null) {
            param.put(NAME, portName);
        }
        if (deviceOwner != null) {
            param.put(DEVICE_OWNER, deviceOwner);
        }
        if (subnetId != null) {
            param.put(FIXED_IPS_SUBNET, subnetId);
        }

        return listPorts(getEndpoint(pod), token, param);
    }

    public Ports listPorts(String pod, String token, Map<String, String> params) throws ClientException,
            ConfigLostException {
        Parameter parameter = new Parameter();
        for (Entry<String, String> param : params.entrySet()) {
            parameter.put(param.getKey(), param.getValue());
        }
        return listPorts(getEndpoint(pod), token, parameter);
    }

    private Ports listPorts(String endpoint, String token, Parameter param) throws ClientException {
        String url = getUrlPorts(endpoint, null);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, param, Ports.class, header);
    }

    /**
     * Get network detail.
     *
     * @param pod
     *            If null, query from cascading layer. Other query from specify
     *            pod.
     * @param token
     *            Token.
     * @param networkId
     *            Network id.
     * @return Result.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public Network getNetworkDetail(String pod, String token, String networkId) throws ClientException,
            ConfigLostException {
        String url = getUrlNetwork(getEndpoint(pod), networkId);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, Network.class, header);
    }

    public FloatingIps listFloatingIps(String pod, String token, String tenantId) throws ClientException,
            ConfigLostException {
        Parameter param = new Parameter();
        param.put(TENANT_ID, tenantId);

        return this.listFloatingIps(getEndpoint(pod), token, param);
    }

    public Agents listAgents(String pod, String token, String host, String agentType) throws ConfigLostException,
            ClientException {
        Parameter param = new Parameter();
        param.put(HOST, host);
        param.put(AGENT_TYPE, agentType);

        return this.listAgents(getEndpoint(pod), token, param);

    }

    private Agents listAgents(String endpoint, String token, Parameter param) throws ClientException {
        String url = getUrlAgents(endpoint);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, param, Agents.class, header);
    }

    public FloatingIps listFloatingIps(String pod, String token, String floatingIpAddress, String fixedIpAddress)
            throws ApplicationException, ConfigLostException, ClientException {

        Parameter param = new Parameter();
        if (floatingIpAddress != null) {
            param.put(FLOATING_IP_ADDR, floatingIpAddress);
        }
        if (fixedIpAddress != null) {
            param.put(FIXED_IP_ADDR, fixedIpAddress);
        }

        FloatingIps fips = listFloatingIps(getEndpoint(pod), token, param);
        if (fips == null || fips.getFloatingIpList() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find floatingIps");
        }

        if (fips.getFloatingIpList().size() == 0) {
            String errMsg = String.format("can not find floatingIps with floating_ip_address[%s] fixed_ip_address[%s]",
                    floatingIpAddress, fixedIpAddress);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        if (fips.getFloatingIpList().size() != 1) {
            String errMsg = String.format(
                    "floatingIps with floating_ip_address[%s] fixed_ip_address[%s] is not unique", floatingIpAddress,
                    fixedIpAddress);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        return fips;
    }

    /**
     * 获取EIP 外部可访问IP
     * 
     * @param endpoint
     * @param token
     * @param param
     * @return
     * @throws ClientException
     * @throws ConfigLostException
     */
    public EipFloatingFip listEipFloatingFip(String endpoint, String token, Parameter param) throws ClientException,
            ConfigLostException {
        String url = getEipUrlFloatingIps(getEndpoint(endpoint));

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, param, EipFloatingFip.class, header);
    }

    private FloatingIps listFloatingIps(String endpoint, String token, Parameter param) throws ClientException {
        String url = getUrlFloatingIps(endpoint, null);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, param, FloatingIps.class, header);
    }

    /**
     * Get port detail.
     *
     * @param pod
     *            If null, query from cascading layer. Other query from specify
     *            pod.
     * @param token
     *            Token.
     * @param portId
     *            Port id.
     * @return Result.
     * @throws ConfigLostException
     * @throws ClientException
     */
    public PortDetail getPortDetail(String pod, String token, String portId) throws ConfigLostException,
            ClientException {

        String url = getUrlPorts(getEndpoint(pod), portId);
        LOG.info("getPortDetail url = {}", url);

        Map<String, String> header = new HashMap<>();
        header.put(Const.X_AUTH_TOKEN, token);

        return RestClientExt.get(url, null, PortDetail.class, header);
    }

    /**
     * Return /v2.0/routers or /v2.0/routers/{router_id} if routerId specified.
     *
     * @param endpoint
     *            Neutron endpoint.
     * @param routerId
     *            Optional.
     * @return Completed URL.
     */
    private String getUrlRouter(String endpoint, String routerId) {
        String url = endpoint + V2_VERSION + "/routers";
        if (routerId == null) {
            return url;
        }
        return url + "/" + routerId;
    }

    private String getUrlPorts(String endpoint, String portId) {
        String url = endpoint + V2_VERSION + "/ports";
        if (portId == null) {
            return url;
        }
        return url + "/" + portId;
    }

    private String getUrlNetwork(String endpoint, String networkId) {
        String url = endpoint + V2_VERSION + "/networks";
        if (networkId == null) {
            return url;
        }
        return url + "/" + networkId;
    }

    private String getUrlFloatingIps(String endpoint, String floatingIpId) {
        String url = endpoint + V2_VERSION + "/floatingips";
        if (floatingIpId == null) {
            return url;
        }
        return url + "/" + floatingIpId;
    }

    private String getEipUrlFloatingIps(String endpoint) {
        return endpoint + V2_VERSION + "/floatingips.json";
    }

    private String getUrlAgents(String endpoint) {
        return endpoint + V2_VERSION + "/agents";
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
