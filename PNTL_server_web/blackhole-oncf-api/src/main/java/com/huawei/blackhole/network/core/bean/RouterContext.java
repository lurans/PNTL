package com.huawei.blackhole.network.core.bean;

import com.huawei.blackhole.network.common.utils.JsonConvertUtils;

import java.io.Serializable;
import java.util.List;

public class RouterContext implements Serializable {
    private static final long serialVersionUID = 2082697497347713054L;

    private String tenantId;

    private String availabiltyZone;

    private String pod;

    private String cascadeVmId;

    private String cascadeNetworkId;

    private String cascadePortId;

    private String cascadeSubnetId;

    private String cascadedNetworkId;

    private List<String> cascadedRouterIdList;

    private String publicIp;

    private String vmIp;

    private String eip;

    private boolean hasSnat;

    private String hostIp;

    // 被级联层的pord id
    private String qvmPort;

    private String dvrDevId;

    private String dvrSrcPort;

    private String dvrDestPort;

    private String dvrSrcRouteIp;

    private String dvrDestRouteIp;

    private String dvrSrcMac;

    private String dvrDstMac;

    private String subnetId;

    private String fip;

    private String vmMac;

    private String dvrMac;

    private String sgMac;

    private String remoteIp;

    private List<String> routerForwarderIps;

    private List<String> l2gwIps;

    private String vrouterVtepIp;

    private String vtepIp;

    private String fipNsId;

    private String fipPortId;

    private String fgMac;

    private List<String> snatIps;

    private String l2gwVtepIp;

    private String isInSameAz;

    private String provdSegmtId;

    public RouterContext() {
        super();
    }

    public RouterContext(String vmIp) {
        super();
        this.vmIp = vmIp;
    }

    public RouterContext(String vmIp, String remoteIp) {
        super();
        this.vmIp = vmIp;
        this.remoteIp = remoteIp;
    }

    public String getProvdSegmtId() {
        return provdSegmtId;
    }

    public void setProvdSegmtId(String provdSegmtId) {
        this.provdSegmtId = provdSegmtId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAvailabiltyZone() {
        return availabiltyZone;
    }

    public void setAvailabiltyZone(String availabiltyZone) {
        this.availabiltyZone = availabiltyZone;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getCascadeVmId() {
        return cascadeVmId;
    }

    public void setCascadeVmId(String cascadeVmId) {
        this.cascadeVmId = cascadeVmId;
    }

    public String getCascadePortId() {
        return cascadePortId;
    }

    public void setCascadePortId(String cascadePortId) {
        this.cascadePortId = cascadePortId;
    }

    public String getCascadeSubnetId() {
        return cascadeSubnetId;
    }

    public void setCascadeSubnetId(String cascadeSubnetId) {
        this.cascadeSubnetId = cascadeSubnetId;
    }

    public String getCascadeNetworkId() {
        return cascadeNetworkId;
    }

    public void setCascadeNetworkId(String cascadeNetworkId) {
        this.cascadeNetworkId = cascadeNetworkId;
    }

    public String getCascadedNetworkId() {
        return cascadedNetworkId;
    }

    public void setCascadedNetworkId(String cascadedNetworkId) {
        this.cascadedNetworkId = cascadedNetworkId;
    }

    public List<String> getCascadedRouterIdList() {
        return cascadedRouterIdList;
    }

    public void setCascadedRouterIdList(List<String> cascadedRouterIdList) {
        this.cascadedRouterIdList = cascadedRouterIdList;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getQvmPort() {
        return qvmPort;
    }

    public void setQvmPort(String qvmPort) {
        this.qvmPort = qvmPort;
    }

    public String getDvrDevId() {
        return dvrDevId;
    }

    public void setDvrDevId(String dvrDevId) {
        this.dvrDevId = dvrDevId;
    }

    public String getDvrSrcPort() {
        return dvrSrcPort;
    }

    public void setDvrSrcPort(String dvrSrcPort) {
        this.dvrSrcPort = dvrSrcPort;
    }

    public String getDvrDestPort() {
        return dvrDestPort;
    }

    public void setDvrDestPort(String dvrDestPort) {
        this.dvrDestPort = dvrDestPort;
    }

    public String getDvrSrcRouteIp() {
        return dvrSrcRouteIp;
    }

    public void setDvrSrcRouteIp(String dvrSrcRouteIp) {
        this.dvrSrcRouteIp = dvrSrcRouteIp;
    }

    public String getDvrDestRouteIp() {
        return dvrDestRouteIp;
    }

    public void setDvrDestRouteIp(String dvrDestRouteIp) {
        this.dvrDestRouteIp = dvrDestRouteIp;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getVmMac() {
        return vmMac;
    }

    public void setVmMac(String vmMac) {
        this.vmMac = vmMac;
    }

    public String getFip() {
        return fip;
    }

    public void setFip(String fip) {
        this.fip = fip;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public String getDvrMac() {
        return dvrMac;
    }

    public void setDvrMac(String dvrMac) {
        this.dvrMac = dvrMac;
    }

    public List<String> getRouterForwarderIps() {
        return routerForwarderIps;
    }

    public void setRouterForwarderIps(List<String> routerForwarderIps) {
        this.routerForwarderIps = routerForwarderIps;
    }

    public List<String> getL2gwIps() {
        return l2gwIps;
    }

    public void setL2gwIps(List<String> l2gwIps) {
        this.l2gwIps = l2gwIps;
    }

    public String getSgMac() {
        return sgMac;
    }

    public void setSgMac(String sgMac) {
        this.sgMac = sgMac;
    }

    public String getVrouterVtepIp() {
        return vrouterVtepIp;
    }

    public void setVrouterVtepIp(String vrouteVtep) {
        this.vrouterVtepIp = vrouteVtep;
    }

    public String getVtepIp() {
        return vtepIp;
    }

    public void setVtepIp(String vtepIp) {
        this.vtepIp = vtepIp;
    }

    public List<String> getSnatIps() {
        return snatIps;
    }

    public void setSnatIps(List<String> snatIps) {
        this.snatIps = snatIps;
    }

    public String getEip() {
        return eip;
    }

    public void setEip(String eip) {
        this.eip = eip;
    }

    public String getL2gwVtepIp() {
        return l2gwVtepIp;
    }

    public void setL2gwVtepIp(String l2gwVtepIp) {
        this.l2gwVtepIp = l2gwVtepIp;
    }

    public String getIsInSameAz() {
        return isInSameAz;
    }

    public void setIsInSameAz(boolean isInSameAz) {
        if (isInSameAz) {
            this.isInSameAz = "true";
        } else {
            this.isInSameAz = "false";
        }
    }

    public boolean getHasSnat() {
        return hasSnat;
    }

    public void setHasSnat(boolean hasSnat) {
        this.hasSnat = hasSnat;
    }

    public String getFipPortId() {
        return fipPortId;
    }

    public void setFipPortId(String fipPortId) {
        this.fipPortId = fipPortId;
    }

    public String getFgMac() {
        return fgMac;
    }

    public void setFgMac(String fgMac) {
        this.fgMac = fgMac;
    }

    public String getFipNsId() {
        return fipNsId;
    }

    public void setFipNsId(String fipNsId) {
        this.fipNsId = fipNsId;
    }

    public String getDvrSrcMac() {
        return dvrSrcMac;
    }

    public void setDvrSrcMac(String dvrSrcMac) {
        this.dvrSrcMac = dvrSrcMac;
    }

    public String getDvrDstMac() {
        return dvrDstMac;
    }

    public void setDvrDstMac(String dvrDstMac) {
        this.dvrDstMac = dvrDstMac;
    }

    public String toString() {
        return JsonConvertUtils.convertBean2Json(this);
    }

}
