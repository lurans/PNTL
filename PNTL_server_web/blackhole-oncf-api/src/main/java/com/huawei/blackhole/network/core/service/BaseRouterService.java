package com.huawei.blackhole.network.core.service;

import ch.ethz.ssh2.Connection;
import com.huawei.blackhole.network.api.bean.NodeInfo;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.FspServiceName;
import com.huawei.blackhole.network.common.constants.HostType;
import com.huawei.blackhole.network.common.constants.ResultTag;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.exception.ScriptException;
import com.huawei.blackhole.network.common.utils.AuthUtil;
import com.huawei.blackhole.network.common.utils.JschUtil;
import com.huawei.blackhole.network.common.utils.ThreadUtil;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;
import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.RouterContext;
import com.huawei.blackhole.network.core.thread.JobThreadPool;
import com.huawei.blackhole.network.extention.bean.openstack.cps.HostInfo;
import com.huawei.blackhole.network.extention.bean.openstack.cps.HostInfo.HostNetwork;
import com.huawei.blackhole.network.extention.bean.openstack.cps.Instances;
import com.huawei.blackhole.network.extention.bean.openstack.cps.Instances.Instance;
import com.huawei.blackhole.network.extention.bean.openstack.cps.NeutronTemplateParam;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Agents;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.EipFloatingFip;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.EipFloatingFip.EipFloatingIp;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.FloatingIps;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.FloatingIps.FloatingIp;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Network;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.PortDetail;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports.Port;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports.Port.Ip;
import com.huawei.blackhole.network.extention.bean.openstack.nova.OsInterfaces;
import com.huawei.blackhole.network.extention.bean.openstack.nova.OsInterfaces.OsInterface;
import com.huawei.blackhole.network.extention.bean.openstack.nova.ServerDetail;
import com.huawei.blackhole.network.extention.bean.vrm.Sites;
import com.huawei.blackhole.network.extention.bean.vrm.Sites.Site;
import com.huawei.blackhole.network.extention.bean.vrm.Vms;
import com.huawei.blackhole.network.extention.bean.vrm.Vms.VmInfo;
import com.huawei.blackhole.network.extention.service.openstack.Cps;
import com.huawei.blackhole.network.extention.service.openstack.Keystone;
import com.huawei.blackhole.network.extention.service.openstack.Neutron;
import com.huawei.blackhole.network.extention.service.openstack.Nova;
import com.huawei.blackhole.network.extention.service.vrm.Vrm;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class BaseRouterService {
    private static final Logger LOG = LoggerFactory.getLogger(BaseRouterService.class);

    protected static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");

    protected static String adminProjectId = null;

    protected static boolean isHostSshPathExist = false;

    protected final static Map<String, Future<BaseFutureCallableResult>> taskFutureMap = new ConcurrentHashMap<String, Future<BaseFutureCallableResult>>();

    @Resource(name = "keystoneService")
    protected Keystone identityWrapperService;
    @Resource(name = "neutronService")
    protected Neutron neutron;
    @Resource(name = "vrmService")
    protected Vrm vrmWrapperService;
    @Resource(name = "novaService")
    protected Nova nova;
    @Resource(name = "cpsService")
    private Cps cpsService;
    @Resource(name = "jobThreadPool")
    protected JobThreadPool jobThreadPool;

    protected ExecutorService resultService = Executors.newFixedThreadPool(Constants.RESULT_SERVICE_MAX_SIZE);

    private static final Set<String> defaultDeviceFilter = new HashSet<String>();

    static {
        defaultDeviceFilter.add("OVS");
    }

    protected static String getAdminProjectId() {
        return adminProjectId;
    }

    protected static void setAdminProjectId(String adminProjectId) {
        BaseRouterService.adminProjectId = adminProjectId;
    }

    protected static boolean isHostSshPathExist(Connection connection) {
        return false;
    }


    /**
     * get cascade port by input vmId and ipAddress
     *
     * @param token
     * @param routerContext
     * @return boolean
     */
    protected void getCascadePortIdByVmIdAndIP(String token, String ipAddress, RouterContext routerContext)
            throws ApplicationException {
        String cascadeVmId = routerContext.getCascadeVmId();
        ServerDetail serverDetail = null;
        try {
            serverDetail = nova.getServerDetail(null, token, adminProjectId, cascadeVmId);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get vm details for vm %s : %s", cascadeVmId,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (serverDetail == null) {
            String errMsg = String.format("fail to get vm details for vm %s", cascadeVmId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        if (!StringUtils.equalsIgnoreCase(serverDetail.getVmStatus(), Constants.VM_STATUS_ACTIVE)) {
            String errMsg = String.format("fail to get vm details for vm %s, invalid vm status : %s", cascadeVmId,
                    serverDetail.getVmStatus());
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        if (null != serverDetail.getTaskState()) {
            String errMsg = String.format("fail to get vm details for vm %s, invaild task state : %s", cascadeVmId,
                    serverDetail.getTaskState());
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }

        String tenantId = serverDetail.getTenantId();
        routerContext.setTenantId(tenantId);
        OsInterfaces osInterfaces = null;
        try {
            osInterfaces = nova.getServerOsInterfaces(null, token, adminProjectId, routerContext.getCascadeVmId());
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get vm %s os-interface : %s", cascadeVmId, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (osInterfaces == null || osInterfaces.getOsInterfaces() == null) {
            String errMsg = String.format("fail to get vm %s os-interface", cascadeVmId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        for (OsInterface osInterface : osInterfaces.getOsInterfaces()) {
            for (Ip ip : osInterface.getIpList()) {
                if (StringUtils.equals(ip.getIpAddress(), ipAddress)) {
                    routerContext.setCascadePortId(osInterface.getPortId());
                    routerContext.setCascadeNetworkId(osInterface.getNetId());
                    routerContext.setCascadeSubnetId(ip.getSubnetId());
                    // LMX Az set
                    routerContext.setAvailabiltyZone(serverDetail.getAz());
                    return;
                }
            }
        }
        String errMsg = String.format("vmId %s and ipAddress %s are not matched", cascadeVmId, ipAddress);
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
    }

    protected void getCascadeNetworkId(String token, RouterContext routerContext) {

    }

    /**
     * get cascade port by input networkId and ipAddress
     *
     * @param token
     * @param routerContext
     * @return boolean
     */
    protected void getCascadePortIdByNetwork(String token, String ipAddress, RouterContext routerContext)
            throws ApplicationException {
        String cascadeNetworkId = routerContext.getCascadeNetworkId();
        Network network = null;
        try {
            network = neutron.getNetworkDetail(null, token, cascadeNetworkId);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get network details for network %s : %s", cascadeNetworkId,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (network == null || network.getNetworkDetail() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to get cascade port id");
        }
        String tenantId = network.getNetworkDetail().getTenant_id();
        routerContext.setTenantId(tenantId);
        String provdSegmtIdTag = "0x" + Integer.toHexString(Integer.parseInt(network.getNetworkDetail().getProvdSegmtId())).toLowerCase();
        routerContext.setProvdSegmtId(provdSegmtIdTag);

        Ports ports = null;
        try {
            ports = neutron.listPorts(null, token, tenantId, cascadeNetworkId, null, null);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get cascading network details for network %s : %s", cascadeNetworkId,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (ports == null || ports.getList() == null) {
            String errMsg = String.format("fail to get cascaded port for network %s", cascadeNetworkId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        for (Port port : ports.getList()) {
            if (port == null || port.getList() == null) {
                continue;
            }
            for (Ip ip : port.getList()) {
                if (StringUtils.equals(ipAddress, ip.getIpAddress())) {
                    routerContext.setCascadePortId(port.getId());
                    routerContext.setCascadeVmId(port.getDeviceId());
                    routerContext.setCascadeSubnetId(ip.getSubnetId());
                    return;
                }
            }
        }
        String errMsg = String.format("networkId %s and ipAddress %s are not matched", cascadeNetworkId, ipAddress);
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
    }

    protected void getMacInfobyCascadePort(String token, RouterContext routerContext) throws ApplicationException {
        PortDetail portDetail = null;
        String portId = routerContext.getCascadePortId();
        try {
            portDetail = neutron.getPortDetail(null, token, portId);
        } catch (ConfigLostException | ClientException e) {
            String errMsg = String.format("fail to get cascading port details for port %s : %s", portId,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (portDetail == null || portDetail.getPortDetail() == null) {
            String errMsg = String.format("fail to get cascading port details for port %s", portId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        routerContext.setVmMac(portDetail.getPortDetail().getMacAddress());
    }

    /**
     * get exact cascaded az by quering vm detail of given vmId
     *
     * @param token
     * @param routerContext
     * @return boolean
     */
    protected void getAzInfoByCascadeVmId(String token, RouterContext routerContext) throws ApplicationException {
        ServerDetail serverDetail = null;
        String vmId = routerContext.getCascadeVmId();
        try {
            serverDetail = nova.getServerDetail(null, token, adminProjectId, vmId);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get cascading vm details for vm %s : %s", vmId,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (null == serverDetail || null == serverDetail.getPod()) {
            String errMsg = String.format("cascading vm %s has no pods", vmId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        if (!StringUtils.equalsIgnoreCase(serverDetail.getVmStatus(), Constants.VM_STATUS_ACTIVE)) {
            String errMsg = String.format("fail to get vm details for vm %s, invalid status : %s", vmId,
                    serverDetail.getVmStatus());
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        if (null != serverDetail.getTaskState()) {
            String errMsg = String.format("fail to get vm details for vm %s, invaild task state : %s", vmId,
                    serverDetail.getTaskState());
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        routerContext.setTenantId(serverDetail.getTenantId());
        routerContext.setAvailabiltyZone(serverDetail.getAz());
        routerContext.setPod(serverDetail.getPod());
    }

    /**
     * get physic host ip of given vm
     *
     * @param token
     * @param routerContext
     */
    protected void getPhyHostIPbyCascadeVmId(String token, RouterContext routerContext) throws ApplicationException {
        String pod = routerContext.getPod();
        String vrmToken = null;
        try {
            vrmToken = vrmWrapperService.getToken(pod);
        } catch (ConfigLostException | ClientException e) {
            String errMsg = "fail to get physic host ip of given vm or pod : " + e.getLocalizedMessage();
            throw new ApplicationException(e.getType(), errMsg);
        }
        String ipAddress = routerContext.getVmIp();

        String vmName = "server@" + routerContext.getCascadeVmId();
        String hostIp = getHostIpByVmName(vrmToken, pod, vmName, ipAddress);

        if (hostIp == null) {
            String errMsg = String.format("fail to get host ip of tenant %s with ip address %s",
                    routerContext.getTenantId(), ipAddress);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        LOG.info("vm {} in host {}", routerContext.getCascadeVmId(), hostIp);
        routerContext.setHostIp(hostIp);
    }

    protected void getCNAVtepIp(String token, RouterContext routerContext) throws ApplicationException {
        AuthUser authUser = AuthUtil.getKeyFile(HostType.CNA);
        List<String> lines = JschUtil.submitCommand(routerContext.getHostIp(), authUser, "hostname");
        String host = null;
        if (lines != null && lines.size() == 1) {
            host = lines.get(0);
        }
        if (host == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR,
                    "can not find hostname for " + routerContext.getHostIp());
        }
        String pod = routerContext.getPod();
        Agents agents = null;
        try {
            agents = neutron.listAgents(pod, token, host, "Open%20vSwitch%20agent");
        } catch (ConfigLostException | ClientException e) {
            String errMsg = String.format("fail to get agent for host %s, pod %s : %s", routerContext.getHostIp(),
                    routerContext.getPod(), e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (agents != null && agents.getAgents() != null && agents.getAgents().size() == 1
                && agents.getAgents().get(0) != null && agents.getAgents().get(0).getConfigurations() != null) {
            String ip = agents.getAgents().get(0).getConfigurations().getTunnelingIp();
            if (ip != null) {
                int iSlash = ip.indexOf("/");
                if (iSlash != -1) {
                    ip = ip.substring(0, iSlash);
                }
                routerContext.setVtepIp(ip);
                return;
            }
        }
        String errMsg = String.format("fail to get agent for host %s, pod %s", routerContext.getHostIp(),
                routerContext.getPod());
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);

    }

    protected void getGatewayPortInfo(String token, RouterContext routerContext) throws ApplicationException {
        AuthUser authUser = AuthUtil.getKeyFile(HostType.CNA);
        List<String> lines = JschUtil.submitCommand(routerContext.getHostIp(), authUser, "hostname");
        String host = null;
        if (lines != null && lines.size() == 1) {
            host = lines.get(0);
        }
        if (host == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR,
                    "can not find hostname for " + routerContext.getHostIp());
        }

        String pod = routerContext.getPod();
        Map<String, String> param = new HashMap<String, String>();
        param.put("binding:host_id", host);
        param.put("device_owner", "network:floatingip_agent_gateway");
        Ports ports = null;
        try {
            ports = neutron.listPorts(pod, token, param);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get port for [ip %s, host %s] : %s", routerContext.getHostIp(), host,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }

        if (ports != null && ports.getList() != null && ports.getList().size() == 1) {
            Port port = ports.getList().get(0);
            if (port != null && port.getId() != null && port.getMacAddress() != null && port.getNetworkId() != null) {
                routerContext.setFipNsId(port.getNetworkId());
                routerContext.setFipPortId(port.getId());
                routerContext.setFgMac(port.getMacAddress());
                return;
            }
        }
        String errMsg = String.format("fail to get port for [ip %s, host %s]", routerContext.getHostIp(), host);
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);

    }

    /**
     * 获取l2gw的VtepIp
     *
     * @param token
     * @throws ApplicationException
     * @throws ConfigLostException
     * @throws ClientException
     */
    protected void getL2gwVtepIp(String token, RouterContext ctx)
            throws ApplicationException, ClientException, ConfigLostException {
        String pod = ctx.getPod();
        NeutronTemplateParam lgwInfo = cpsService.getCpsNeutronTemplateParam(pod, Constants.TEMPLATE_L2GW, token);
        if (lgwInfo != null && lgwInfo.getVtepIp() != null) {
            String ip = lgwInfo.getVtepIp();
            int iSlash = ip.indexOf("/");
            if (iSlash != -1) {
                ip = ip.substring(0, iSlash);
            }
            ctx.setL2gwVtepIp(ip);
            return;
        }
        String errMsg = "fail to get vtep ip for pod:" + pod;
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
    }

    protected void getVrouterVtepIp(String token, RouterContext ctx)
            throws ApplicationException, ClientException, ConfigLostException {
        String region = null;
        NeutronTemplateParam lgwInfo = cpsService.getCpsNeutronTemplateParam(region, Constants.TEMPLATE_VROUTER, token);
        if (lgwInfo != null && lgwInfo.getVtepIp() != null) {
            String ip = lgwInfo.getVtepIp();
            int iSlash = ip.indexOf("/");
            if (iSlash != -1) {
                ip = ip.substring(0, iSlash);
            }
            ctx.setVrouterVtepIp(ip);
            return;
        }

        String errMsg = "fail to get vtep ip";
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
    }

    /**
     * 获取l2gw的VtepIp
     *
     * @param token
     * @param ctx
     * @throws ApplicationException
     */
    protected void getL2gwVtepIp2(String token, RouterContext ctx) throws ApplicationException {
        AuthUser authUser = AuthUtil.getKeyFile(HostType.L2GW);
        for (String lgwIp : ctx.getL2gwIps()) {
            List<String> lines = JschUtil.submitCommand(ctx.getHostIp(), authUser, "hostname");
            String host = null;
            if (lines != null && lines.size() == 1) {
                host = lines.get(0);
            }
            if (host == null) {
                throw new ApplicationException(ExceptionType.ENV_ERR, "can not find hostname for : " + lgwIp);
            }
        }
    }

    /**
     * get physic host ip by given cascaded vmName
     *
     * @throws ApplicationException
     */
    protected String getHostIpByVmName(String token, String pod, String vmName, String vmIp)
            throws ApplicationException {
        Sites sites = null;
        try {
            sites = vrmWrapperService.getSites(pod, token);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = "fail to get host ip : " + e.getLocalizedMessage();
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (sites == null || sites.getSites() == null) {
            return null;
        }
        for (Site site : sites.getSites()) {
            String[] siteInfo = site.getUrn().split(":");
            String siteStr = siteInfo[siteInfo.length - 1];
            Vms vms = null;
            try {
                vms = vrmWrapperService.getVmInfoByVmName(pod, token, siteStr, vmName);
            } catch (ConfigLostException | ClientException e) {
                String errMsg = String.format("fail to get host ip with pod %s siteStr %s and vmName %s : %s", pod,
                        siteStr, vmName, e.getLocalizedMessage());
                throw new ApplicationException(e.getType(), errMsg);
            }
            if (null == vms || null == vms.getVms()) {
                continue;
            }
            if (vms.getTotal() != 1) {
                String errMsg = String.format("fail to get %s hostIps with pod %s site %s and vmName %s", pod,
                        vms.getTotal(), siteStr, vmName);
                throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
            }
            String[] urn = vms.getVms().get(0).getUrn().split(":");
            String urnStr = urn[urn.length - 1];

            VmInfo vmInfo = null;
            try {
                vmInfo = vrmWrapperService.getVmInfo(pod, token, siteStr, urnStr);
            } catch (ConfigLostException | ClientException e) {
                String errMsg = String.format("fail to get host ip with pod %s siteStr %s and urnStr %s : %s", pod,
                        siteStr, urnStr, e.getLocalizedMessage());
                throw new ApplicationException(e.getType(), errMsg);
            }

            if (null != vmInfo && null != vmInfo.getVncAcessInfo()) {
                return vmInfo.getVncAcessInfo().getHostIp();
            }
        }
        return null;
    }

    /**
     * get cascaded router of src and dest
     *
     * @param token
     * @param srcRouterContext
     * @param destRouterContext
     */
    protected void findCascadedRouterofSrcAndDest(String token, RouterContext srcRouterContext,
                                                  RouterContext destRouterContext) throws ApplicationException {
        this.findDvrRouter(token, srcRouterContext);
        this.findDvrRouter(token, destRouterContext);
        srcRouterContext.setDvrDestRouteIp(destRouterContext.getDvrSrcRouteIp());
        destRouterContext.setDvrDestRouteIp(srcRouterContext.getDvrSrcRouteIp());
    }

    protected void setSgMac(String token, RouterContext routerContext) throws ApplicationException {
        Ports ports = null;
        String pod = routerContext.getPod();
        String networkId = routerContext.getCascadedNetworkId();
        String subnetId = routerContext.getSubnetId();
        try {
            ports = neutron.listPorts(pod, token, null, networkId, null, null, Constants.VPN_PORT_SG_DEVICE_OWNER,
                    subnetId);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to find router with pod=%s, networkId=%s, device_owner=%s : %s", pod,
                    networkId, Constants.ROUTER_DEVICE_OWNER, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (null == ports || null == ports.getList() || 0 == ports.getList().size()) {
            String errMsg = String.format(
                    "can not find router with param: pod=[%s], networkId=[%s], " + "device_owner=[%s]", pod, networkId,
                    Constants.ROUTER_DEVICE_OWNER);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        // TODO 当前认为上面查询条件会返回一个，因为加了subnet——id过滤条件
        if (1 != ports.getList().size()) {
            String errMsg = String.format(
                    "fail to find [%d] router with param: pod=[%s], networkId=[%s], " + "device_owner=[%s]",
                    ports.getList().size(), pod, networkId, Constants.ROUTER_DEVICE_OWNER);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        Port port = ports.getList().get(0);
        if (Constants.VPN_PORT_SG_DEVICE_OWNER.equals(port.getDeviceOwner())) {
            routerContext.setSgMac(port.getMacAddress());
        }
    }

    protected void findDvrRouter(String token, RouterContext routerContext) throws ApplicationException {
        Ports ports = null;
        String pod = routerContext.getPod();
        String tenantId = routerContext.getTenantId();
        String networkId = routerContext.getCascadedNetworkId();
        try {
            ports = neutron.listPorts(pod, token, tenantId, networkId, null, null, Constants.ROUTER_DEVICE_OWNER, null);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to find router with pod=%s, networkId=%s, device_owner=%s : %s", pod,
                    networkId, Constants.ROUTER_DEVICE_OWNER, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (null == ports || null == ports.getList() || 0 == ports.getList().size()) {
            String errMsg = String.format(
                    "can not find router with param: pod=[%s], networkId=[%s], " + "device_owner=[%s]", pod, networkId,
                    Constants.ROUTER_DEVICE_OWNER);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        if (1 != ports.getList().size()) {
            String errMsg = String.format(
                    "fail to find [%d] router with param: pod=[%s], networkId=[%s], " + "device_owner=[%s]",
                    ports.getList().size(), pod, networkId, Constants.ROUTER_DEVICE_OWNER);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        Port port = ports.getList().get(0);
        for (Ip ip : port.getList()) {
            if (StringUtils.equals(routerContext.getSubnetId(), ip.getSubnetId())) {
                routerContext.setDvrSrcPort(port.getId());
                routerContext.setDvrDevId(port.getDeviceId());
                routerContext.setDvrSrcRouteIp(ip.getIpAddress());
                routerContext.setDvrSrcMac(port.getMacAddress());
                routerContext.setDvrMac(port.getMacAddress());
                return;
            }
        }
        String errMsg = String.format("can not find router with pod=%s, networkId=%s, device_owner=%s, subnet=%s", pod,
                networkId, Constants.ROUTER_DEVICE_OWNER, routerContext.getSubnetId());
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
    }

    /**
     * get dvr dest Port of given dvr dev id
     *
     * @param token
     * @param routerContext
     */
    protected void findSrcAndDestPortOfOneRouter(String token, RouterContext routerContext)
            throws ApplicationException {
        Ports ports = null;
        String pod = routerContext.getPod();
        String tenantId = routerContext.getTenantId();
        String dvrDevId = routerContext.getDvrDevId();
        String dvrRouteIp = routerContext.getDvrDestRouteIp();
        try {
            ports = neutron.listPorts(pod, token, tenantId, null, dvrDevId, null);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get dvr port with pod=%s, dvrDevId=%s : %s", pod, dvrDevId,
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (ports == null || ports.getList() == null) {
            return;
        }
        for (Port port : ports.getList()) {
            if (port == null || port.getList() == null) {
                continue;
            }
            for (Ip ip : port.getList()) {
                if (StringUtils.equals(ip.getIpAddress(), dvrRouteIp)) {
                    routerContext.setDvrDestPort(port.getId());
                    routerContext.setDvrDstMac(port.getMacAddress());
                    return;
                }
            }
        }
    }

    protected void getPublicIpByFloatingIp(String token, RouterContext routerContext) throws ApplicationException {
        FloatingIps cascadeFIPs = null;
        try {
            cascadeFIPs = neutron.listFloatingIps(null, token, null, routerContext.getFip());
        } catch (ConfigLostException | ClientException e) {
            String errMsg = String.format("fail to get public ip by floating ip %s : %s", routerContext.getFip(),
                    e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        routerContext.setPublicIp(cascadeFIPs.getFloatingIpList().get(0).getFloatingIpAddress());
    }

    /**
     * 判断是不是有snat，根据查询eip和fip的方法确认。 如果可以一次获取到eip和fip，则有snat。
     *
     * @param token
     * @param routerContext
     * @return
     * @throws ApplicationException
     */
    protected boolean getHasSnat(String token, RouterContext routerContext) throws ApplicationException {
        EipFloatingFip eIpFloatingIps = null;
        try {
            Parameter param = new Parameter();
            param.put("port_id", routerContext.getCascadePortId());
            eIpFloatingIps = neutron.listEipFloatingFip(null, token, param);
        } catch (ClientException | ConfigLostException e) {
            return false;
        }
        if (eIpFloatingIps == null || eIpFloatingIps.getFloatingIpList() == null) {
            return false;
        }
        List<EipFloatingIp> eipFipList = eIpFloatingIps.getFloatingIpList();
        if (eipFipList.size() < 2) {
            return false;
        }
        boolean findEip = false;
        boolean findFip = false;
        for (EipFloatingIp eipFip : eipFipList) {
            String fIp = eipFip.getFloatingIpAddress();
            if (fIp != null && fIp.startsWith("10.")) { // 对外可访问
                findEip = true;
            }
            if (fIp != null && fIp.startsWith("100.")) { // 对内的FIP
                findFip = true;
            }
        }
        return findEip && findFip;
    }

    /**
     * get cascade port and FIP by input vmId
     *
     * @param token
     * @param routerContext
     */
    protected void getCascadePortAndFIPByVmId(String token, RouterContext routerContext) throws ApplicationException {
        OsInterfaces osInterfaces = null;
        String vmId = routerContext.getCascadeVmId();
        String tenantId = routerContext.getTenantId();
        try {
            osInterfaces = nova.getServerOsInterfaces(null, token, adminProjectId, vmId);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get vm %s os-interface : %s", vmId, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (osInterfaces == null || osInterfaces.getOsInterfaces() == null) {
            String errMsg = String.format("fail to get vm %s os-interface", vmId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }

        List<String> portIdList = new ArrayList<String>();
        for (OsInterface osInterface : osInterfaces.getOsInterfaces()) {
            portIdList.add(osInterface.getPortId());
        }
        FloatingIps fipList = null;
        try {
            fipList = neutron.listFloatingIps(null, token, tenantId);
        } catch (ClientException | ConfigLostException e) {
            throw new ApplicationException(e.getType(), "fail to get floating ip : " + e.getLocalizedMessage());
        }
        if (fipList == null || fipList.getFloatingIpList() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to get cascade port");
        }
        for (FloatingIp fip : fipList.getFloatingIpList()) {
            if (portIdList.contains(fip.getPortId())) {
                routerContext.setFip(fip.getFloatingIpAddress());
                routerContext.setCascadePortId(fip.getPortId());
            }
        }
        if (StringUtils.isEmpty(routerContext.getFip())) {
            String errMsg = String.format("port list of vm=%s and fipList of tenant=%s are not matched", vmId,
                    tenantId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }

        // eip
        EipFloatingFip eIpFloatingIps = null;
        try {
            Parameter param = new Parameter();
            param.put("fixed_ip_address", routerContext.getFip());
            eIpFloatingIps = neutron.listEipFloatingFip(null, token, param);
        } catch (ClientException | ConfigLostException e) {
            throw new ApplicationException(e.getType(), "get eip failed : " + e.getLocalizedMessage());
        }
        if (eIpFloatingIps == null || eIpFloatingIps.getFloatingIpList() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to get cascade port");
        }
        for (EipFloatingIp eip : eIpFloatingIps.getFloatingIpList()) {
            if (eip.getFloatingIpAddress() != null) {
                routerContext.setEip(eip.getFloatingIpAddress());
                break;
            }
        }
        if (StringUtils.isEmpty(routerContext.getEip())) {
            String errMsg = String.format("eip of vm=%s and fipList of tenant=%s are not matched", vmId, tenantId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }

        // VmIp
        LOG.info("fip = {}; port id = {}", routerContext.getFip(), routerContext.getCascadePortId());
        FloatingIps cascadedFIPs = null;
        try {
            cascadedFIPs = neutron.listFloatingIps(null, token, routerContext.getFip(), null);
        } catch (ConfigLostException | ClientException e) {
            String errMsg = String.format("fail to get fixed ip address : %s", e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        FloatingIp cascadedFIP = cascadedFIPs.getFloatingIpList().get(0);
        routerContext.setVmIp(cascadedFIP.getFixedIpAddress());
    }

    protected void getCascadePortAndFIPByVmIdU30(String token, RouterContext routerContext)
            throws ApplicationException {
        OsInterfaces osInterfaces = null;
        String vmId = routerContext.getCascadeVmId();
        String tenantId = routerContext.getTenantId();
        try {
            osInterfaces = nova.getServerOsInterfaces(null, token, adminProjectId, vmId);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get vm %s os-interface : %s", vmId, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (osInterfaces == null || osInterfaces.getOsInterfaces() == null) {
            String errMsg = String.format("fail to get vm %s os-interface", vmId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }

        List<String> portIdList = new ArrayList<String>();
        for (OsInterface osInterface : osInterfaces.getOsInterfaces()) {
            portIdList.add(osInterface.getPortId());
        }
        FloatingIps fipList = null;
        try {
            fipList = neutron.listFloatingIps(null, token, tenantId);
        } catch (ClientException | ConfigLostException e) {
            throw new ApplicationException(e.getType(), "fail to get floating ip : " + e.getLocalizedMessage());
        }
        if (fipList == null || fipList.getFloatingIpList() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to get cascade port");
        }
        for (FloatingIp fip : fipList.getFloatingIpList()) {
            if (portIdList.contains(fip.getPortId())) {
                routerContext.setCascadePortId(fip.getPortId());
            }
        }
        EipFloatingFip eIpFloatingIps = null;
        try {
            Parameter param = new Parameter();
            param.put("port_id", routerContext.getCascadePortId());
            eIpFloatingIps = neutron.listEipFloatingFip(null, token, param);
        } catch (ClientException | ConfigLostException e) {
            throw new ApplicationException(e.getType(), "fail to get eip : " + e.getLocalizedMessage());
        }
        if (eIpFloatingIps == null || eIpFloatingIps.getFloatingIpList() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find eIP");
        }
        List<EipFloatingIp> eipFipList = eIpFloatingIps.getFloatingIpList();
        if (eipFipList.size() < 2) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find eIP");
        }
        for (EipFloatingIp eipFip : eipFipList) {
            String fIp = eipFip.getFloatingIpAddress();
            if (fIp != null && fIp.startsWith("10.")) { // 对外可访问
                routerContext.setEip(fIp);
            }
            if (fIp != null && fIp.startsWith("100.")) { // 对内的FIP
                routerContext.setFip(fIp);
            }
        }
        if (routerContext.getFip() == null || routerContext.getEip() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find eIp");
        }
        FloatingIps cascadedFIPs = null;
        try {
            cascadedFIPs = neutron.listFloatingIps(null, token, routerContext.getFip(), null);
        } catch (ConfigLostException | ClientException e) {
            String errMsg = String.format("fail to get fixed ip address : %s", e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        FloatingIp cascadedFIP = cascadedFIPs.getFloatingIpList().get(0);
        routerContext.setVmIp(cascadedFIP.getFixedIpAddress());

    }

    /**
     * get cascade port and FIP by input publicIp
     *
     * @param token
     * @param routerContext
     */
    protected void getCascadePortAndFIPByEIP(String token, RouterContext routerContext) throws ApplicationException {
        try {
            FloatingIps cascadeFIPs = null;
            cascadeFIPs = neutron.listFloatingIps(null, token, routerContext.getPublicIp(), null);
            String fip = cascadeFIPs.getFloatingIpList().get(0).getFixedIpAddress();
            routerContext.setFip(fip);
            FloatingIps cascadedFIPs = neutron.listFloatingIps(null, token, fip, null);
            FloatingIp cascadedFIP = cascadedFIPs.getFloatingIpList().get(0);
            routerContext.setVmIp(cascadedFIP.getFixedIpAddress());
            routerContext.setCascadePortId(cascadedFIP.getPortId());
            routerContext.setTenantId(cascadedFIP.getTenantId());
        } catch (ConfigLostException | ClientException e) {
            e.printStackTrace();
            throw new ApplicationException(e.getType(),
                    "fail to get cascade port and fip : " + e.getLocalizedMessage());
        }
    }

    protected void getCascadePortAndFIPByEIPU30(String token, RouterContext routerContext) throws ApplicationException {
        try {
            // 获取port信息
            FloatingIps cascadeFIPs = neutron.listFloatingIps(null, token, routerContext.getPublicIp(), null);
            String pordId = cascadeFIPs.getFloatingIpList().get(0).getPortId();
            if (pordId == null) {
                throw new ApplicationException(ExceptionType.ENV_ERR, "fail to get cascade port and fip");
            }
            routerContext.setCascadePortId(pordId);

            // 获取FIP EIP
            EipFloatingFip eIpFloatingIps = null;
            try {
                Parameter param = new Parameter();
                param.put("port_id", routerContext.getCascadePortId());
                eIpFloatingIps = neutron.listEipFloatingFip(null, token, param);
            } catch (ClientException | ConfigLostException e) {
                throw new ApplicationException(e.getType(), "fail to get eip : " + e.getLocalizedMessage());
            }
            if (eIpFloatingIps == null || eIpFloatingIps.getFloatingIpList() == null) {
                throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find eIP");
            }
            List<EipFloatingIp> eipFipList = eIpFloatingIps.getFloatingIpList();
            if (eipFipList.size() < 2) {
                throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find eIP");
            }
            for (EipFloatingIp eipFip : eipFipList) {
                String fIp = eipFip.getFloatingIpAddress();
                if (fIp != null && fIp.startsWith("10.")) { // 对外可访问
                    routerContext.setEip(fIp);
                }
                if (fIp != null && fIp.startsWith("100.")) { // 对内的FIP
                    routerContext.setFip(fIp);
                }
            }
            if (routerContext.getFip() == null || routerContext.getEip() == null) {
                throw new ApplicationException(ExceptionType.ENV_ERR, "fail to find eIp");
            }
            FloatingIps cascadedFIPs = null;
            try {
                cascadedFIPs = neutron.listFloatingIps(null, token, routerContext.getFip(), null);
            } catch (ConfigLostException | ClientException e) {
                String errMsg = String.format("fail to get fixed ip address : %s", e.getLocalizedMessage());
                throw new ApplicationException(e.getType(), errMsg);
            }
            FloatingIp cascadedFIP = cascadedFIPs.getFloatingIpList().get(0);
            routerContext.setVmIp(cascadedFIP.getFixedIpAddress());

        } catch (ConfigLostException | ClientException e) {
            LOG.info("getCascadePortAndFIPByEIPU30 fail", e);
        }

    }

    /**
     * get cascading vmId by query cascading port detail
     *
     * @param token
     * @param routerContext
     */
    protected void getCascadeVmIdByCascadePort(String token, RouterContext routerContext) throws ApplicationException {
        PortDetail portDetail = null;
        String portId = routerContext.getCascadePortId();
        try {
            portDetail = neutron.getPortDetail(null, token, portId);
        } catch (ConfigLostException | ClientException e) {
            String errMsg = String.format("fail to get cascading port %s detail : %s", portId, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (portDetail == null || portDetail.getPortDetail() == null) {
            String errMsg = String.format("fail to get cascading port %s detail ", portId);
            throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
        }
        String cascadeVmId = portDetail.getPortDetail().getDeviceId();
        routerContext.setCascadeVmId(cascadeVmId);
    }

    /**
     * get cascaded port by cascading port, which portName of cascaded layer is:
     * "port@" add portId of cascading layer
     *
     * @param token
     * @param routerContext
     */
    protected void getCascadedPortbyCascadePort(String token, RouterContext routerContext) throws ApplicationException {

        String cascadedPortName = "port@" + routerContext.getCascadePortId();
        Port port = null;
        String pod = routerContext.getPod();
        try {
            port = neutron.listPorts(pod, token, null, null, null, cascadedPortName).getList().get(0);
        } catch (ClientException | ConfigLostException e) {
            String errMsg = String.format("fail to get ports with pod=%s, cascaded portName=%s : %s", pod,
                    cascadedPortName, e.getLocalizedMessage());
            throw new ApplicationException(e.getType(), errMsg);
        }
        if (port == null || port.getList() == null) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "get cascaded port failed");
        }
        String cascadedPortId = port.getId();// 就是vm 的 port id
        routerContext.setQvmPort(cascadedPortId);
        String vmIp = routerContext.getVmIp();
        for (Ip ip : port.getList()) {
            if (StringUtils.equals(vmIp, ip.getIpAddress())) {
                routerContext.setSubnetId(ip.getSubnetId());
                routerContext.setCascadedNetworkId(port.getNetworkId());
                return;
            }
        }
        String errMsg = String.format(
                "port=[%s] detail with pod=%s, cascaded portName=%s, " + "but this port has no ip matched with vmIp=%s",
                cascadedPortId, pod, cascadedPortName, vmIp);
        throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
    }

    /**
     * 获取L2GW(负载均衡)节点Ip
     *
     * @param token
     * @param routerContext
     * @throws ApplicationException
     */
    protected void getL2GWIp(String token, RouterContext routerContext) throws ApplicationException {
        List<String> hostIps = getInstanceHostIps(token, routerContext.getPod(), FspServiceName.NEUTRON,
                Constants.TEMPLATE_L2GW);
        if (hostIps == null || hostIps.size() == 0) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "can not find L2-gateway ip");
        }
        routerContext.setL2gwIps(hostIps);
    }

    /**
     * 新版本的FusionNetwork，RouterForward由原来的主备模式更改为双主模式，故不再判断主节点
     *
     * @param token
     * @param routerContext
     * @throws ApplicationException
     */
    protected void getRouterForwardIp(String token, RouterContext routerContext) throws ApplicationException {
        List<String> hostIps = getInstanceHostIps(token, null, FspServiceName.NEUTRON, Constants.TEMPLATE_VROUTER);
        if (hostIps == null || hostIps.size() == 0) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "can not find routerFoward ip");
        }
        routerContext.setRouterForwarderIps(hostIps);
    }

    protected void getSnatIp(String token, RouterContext routerContext) throws ApplicationException {
        List<String> snatIps = getInstanceHostIps(token, null, FspServiceName.NEUTRON, Constants.TEMPLATE_SNAT);
        if (snatIps == null || snatIps.size() == 0) {
            throw new ApplicationException(ExceptionType.ENV_ERR, "can not find snat ip");
        }
        routerContext.setSnatIps(snatIps);
    }

    /**
     * @param taskId
     * @param realTaskIds
     * @param logFileFormat 日志文件格式
     * @param hostType
     * @return
     * @throws ApplicationException
     */
    protected Map<String, List<NodeInfo>> getHostLogInfo(String taskId, List<String> realTaskIds, String logFileFormat,
                                                         String hostType) throws ApplicationException {
        Map<String, List<NodeInfo>> hostNodeInfo = new HashMap<String, List<NodeInfo>>(); // 最终返回信息
        Map<String, List<NodeInfo>> emptyNodeInfo = null; // 空结果
        List<NodeInfo> orderInputNodeList = null; // 有序结果
        List<NodeInfo> orderOutputNodeList = null; // 有序结果
        List<NodeInfo> inputNodeList = null; // 优选结果
        List<NodeInfo> outputNodeList = null; // 优选结果
        boolean findInput = false;
        boolean findOutput = false;

        String errMsg = "";

        int ipNum = realTaskIds.size();
        Set<String> filterNodeTypes = getFilteNodeTypes(hostType);

        for (int index = 0; index < ipNum; index++) {
            Map<String, List<NodeInfo>> scriptInfo = null;
            String realTaskId = realTaskIds.get(index);
            String logFileName = String.format(logFileFormat, realTaskId);

            try { // 远程执行结果
                scriptInfo = ThreadUtil.getOneHostLogInfo(realTaskId, logFileName, taskFutureMap.get(realTaskId),
                        hostType);
            } catch (ScriptException e) {
                errMsg = e.getLocalizedMessage();
                LOG.warn("skip script exception. task id : " + realTaskId + ". reason : " + errMsg, e);
                continue;
            }

            if (scriptInfo == null) {
                return null; // when there are unfinished task, return null
            }

            // in case when there are no reasonable result
            if (pkgFound(scriptInfo.get(ResultTag.FLOW_TAG_INPUT), filterNodeTypes, false)) {
                orderInputNodeList = scriptInfo.get(ResultTag.FLOW_TAG_INPUT);
            }
            if (pkgFound(scriptInfo.get(ResultTag.FLOW_TAG_OUTPUT), filterNodeTypes, true)) {
                orderOutputNodeList = scriptInfo.get(ResultTag.FLOW_TAG_OUTPUT);
            }
            // update emptyNodeInfo, in case when there are no reasonable result
            if (emptyNodeInfo == null) {
                emptyNodeInfo = scriptInfo;
            } else {
                if (pkgFound(scriptInfo.get(ResultTag.FLOW_TAG_INPUT), null)) {
                    emptyNodeInfo.put(ResultTag.FLOW_TAG_INPUT, scriptInfo.get(ResultTag.FLOW_TAG_INPUT));
                }
                if (pkgFound(scriptInfo.get(ResultTag.FLOW_TAG_OUTPUT), null)) {
                    emptyNodeInfo.put(ResultTag.FLOW_TAG_OUTPUT, scriptInfo.get(ResultTag.FLOW_TAG_OUTPUT));
                }
            }

            // update hostNodeInfo, when there are reasonable result
            if (pkgFound(scriptInfo.get(ResultTag.FLOW_TAG_INPUT), filterNodeTypes)) {
                findInput = true;
                inputNodeList = scriptInfo.get(ResultTag.FLOW_TAG_INPUT);
            }
            if (pkgFound(scriptInfo.get(ResultTag.FLOW_TAG_OUTPUT), filterNodeTypes)) {
                findOutput = true;
                outputNodeList = scriptInfo.get(ResultTag.FLOW_TAG_OUTPUT);
            }
            if (findInput && findOutput) {
                hostNodeInfo.put(ResultTag.FLOW_TAG_INPUT, inputNodeList);
                hostNodeInfo.put(ResultTag.FLOW_TAG_OUTPUT, outputNodeList);
                return hostNodeInfo;
            }
        }

        if (emptyNodeInfo == null) {
            throw new ApplicationException(ExceptionType.SCRIPT_ERR, errMsg);
        }

        if (hostNodeInfo.isEmpty() && emptyNodeInfo != null) {
            if (orderInputNodeList != null) {
                emptyNodeInfo.put(ResultTag.FLOW_TAG_INPUT, orderInputNodeList);
            }
            if (findInput) {
                emptyNodeInfo.put(ResultTag.FLOW_TAG_INPUT, inputNodeList);
            }
            if (orderOutputNodeList != null) {
                emptyNodeInfo.put(ResultTag.FLOW_TAG_OUTPUT, orderOutputNodeList);
            }
            if (findOutput) {
                emptyNodeInfo.put(ResultTag.FLOW_TAG_OUTPUT, outputNodeList);
            }
            return emptyNodeInfo;
        }

        if (findInput) {
            hostNodeInfo.put(ResultTag.FLOW_TAG_INPUT, inputNodeList);
        }
        if (findOutput) {
            hostNodeInfo.put(ResultTag.FLOW_TAG_OUTPUT, outputNodeList);
        }

        return hostNodeInfo;
    }

    private Set<String> getFilteNodeTypes(String hostType) {
        if (HostType.L2GW.equals(hostType)) {
            return defaultDeviceFilter;
        }
        if (HostType.ROUTERFORWARDER.equals(hostType)) {
            return defaultDeviceFilter;
        }
        if (HostType.SNAT.equals(hostType)) {
            return defaultDeviceFilter;
        }
        return null;
    }

    /**
     * @param token
     * @param region   pod
     * @param service  OpenStack服务
     * @param template 节点类型
     * @return
     * @throws ApplicationException
     */
    private List<String> getInstanceHostIps(String token, String region, String service, String template)
            throws ApplicationException {
        List<String> hostIpList = new ArrayList<>();
        try {
            Instances instances = cpsService.getInstances(region, service, template, token);
            if (null == instances || null == instances.getInstances()) {
                String errMsg = String.format("fail to get instances with template %s", Constants.TEMPLATE_VROUTER);
                throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
            }
            for (Instance instance : instances.getInstances()) {
                if (!instance.getHastatus().equals(Constants.INSTANCE_HASTATUS_ACTIVE)) {
                    continue;
                }
                String hostId = instance.getRunsonhost();
                if (StringUtils.isEmpty(hostId)) {
                    throw new ApplicationException(ExceptionType.ENV_ERR, "host id in instance is null");
                }
                HostInfo hostInfo = cpsService.getHostInfo(region, hostId, token);
                if (null == hostInfo || null == hostInfo.getNetwork()) {
                    String errMsg = String.format("fail to get host details of %s ", instance.getRunsonhost());
                    throw new ApplicationException(ExceptionType.ENV_ERR, errMsg);
                }
                for (HostNetwork network : hostInfo.getNetwork()) {
                    if (network.getName().equals(Constants.NETWORK_NAME_EXTERNAL_OM)) {
                        hostIpList.add(network.getIp());
                    }
                }
            }
        } catch (ClientException | ConfigLostException e) {
            String errMsg = "fail to get instance : " + e.getLocalizedMessage();
            throw new ApplicationException(e.getType(), errMsg);
        }
        return hostIpList;
    }

    protected boolean needL2Info(Map<String, List<NodeInfo>> routerInfo) {
        for (Map.Entry<String, List<NodeInfo>> entry : routerInfo.entrySet()) {
            for (NodeInfo nodeInfo : entry.getValue()) {
                if (nodeInfo.getPacketNum() != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean needL2Info(List<NodeInfo> nodeInfos) {
        if (nodeInfos == null) {
            return true;
        }
        for (NodeInfo nodeInfo : nodeInfos) {
            if (nodeInfo.getPacketNum() != 0) {
                return false;
            }
        }
        return true;
    }

    private boolean pkgFound(List<NodeInfo> nodeInfos, Set<String> filterNodeTypes) {
        if (nodeInfos == null) {
            return false;
        }
        for (NodeInfo nodeInfo : nodeInfos) {
            if (filterNodeTypes == null && nodeInfo.getPacketNum() != 0) {
                return true;
            }
            if (filterNodeTypes != null && filterNodeTypes.contains

                    (nodeInfo.getType().toUpperCase()) && nodeInfo.getPacketNum() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean pkgFound(List<NodeInfo> nodeInfos, Set<String> filterNodeTypes, boolean beforeOvs) {
        if (nodeInfos == null || filterNodeTypes == null) {
            return false;
        }
        int idx = -1;
        if (beforeOvs) {
            for (int i = 0; i < nodeInfos.size(); ++i) {
                NodeInfo nf = nodeInfos.get(i);
                if (nf != null && nf.getType() != null && nf.getType

                        ().toUpperCase().equals("OVS")) {
                    break;
                }
                idx = i;
            }
        } else {
            for (int i = nodeInfos.size() - 1; i >= 0; --i) {
                NodeInfo nf = nodeInfos.get(i);
                if (nf != null && nf.getType() != null && nf.getType

                        ().toUpperCase().equals("OVS")) {
                    break;
                }
                idx = i;
            }
        }
        if (idx != -1) {
            return nodeInfos.get(idx).getPacketNum() != 0;
        }
        return false;
    }

}