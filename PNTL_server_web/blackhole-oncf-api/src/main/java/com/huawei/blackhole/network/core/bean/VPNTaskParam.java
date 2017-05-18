package com.huawei.blackhole.network.core.bean;

public class VPNTaskParam {

    private String qvmPort;

    private String qvmMac;

    private String dvrDevId;

    private String dvrPortId;

    private String dvrPortMac;

    private String dvrMac;

    private String remoteIp;

    private String localFile;

    private String dstFileName;

    private CommonCallableParam commonParam;

    private String sgMac;

    private String cnaVtepIp;

    private String vrouteVtepIP;

    private String l2gwVtepIp;

    private String vmPortId;

    private String provdSegmtId;

    public VPNTaskParam(CommonCallableParam commonParam) {
        super();
        this.commonParam = commonParam;
    }

    public String getProvdSegmtId() {
        return provdSegmtId;
    }

    public void setProvdSegmtId(String provdSegmtId) {
        this.provdSegmtId = provdSegmtId;
    }

    public String getQvmPort() {
        return qvmPort;
    }

    public void setQvmPort(String qvmPort) {
        this.qvmPort = qvmPort;
    }

    public String getQvmMac() {
        return qvmMac;
    }

    public void setQvmMac(String qvmMac) {
        this.qvmMac = qvmMac;
    }

    public String getDvrDevId() {
        return dvrDevId;
    }

    public void setDvrDevId(String dvrDevId) {
        this.dvrDevId = dvrDevId;
    }

    public String getDvrPortId() {
        return dvrPortId;
    }

    public void setDvrPortId(String dvrPortId) {
        this.dvrPortId = dvrPortId;
    }

    public String getDvrMac() {
        return dvrMac;
    }

    public void setDvrMac(String dvrMac) {
        this.dvrMac = dvrMac;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public CommonCallableParam getCommonParam() {
        return commonParam;
    }

    public void setCommonParam(CommonCallableParam commonParam) {
        this.commonParam = commonParam;
    }

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }

    public String getDstFileName() {
        return dstFileName;
    }

    public void setDstFileName(String dstFileName) {
        this.dstFileName = dstFileName;
    }

    public String getSgMac() {
        return sgMac;
    }

    public void setSgMac(String sgMac) {
        this.sgMac = sgMac;
    }

    public String getVrouteVtepIP() {
        return vrouteVtepIP;
    }

    public void setVrouteVtepIP(String vrouteVtepIP) {
        this.vrouteVtepIP = vrouteVtepIP;
    }

    public String getVmPortId() {
        return vmPortId;
    }

    public void setVmPortId(String vmPortId) {
        this.vmPortId = vmPortId;
    }

    public String getL2gwVtepIp() {
        return l2gwVtepIp;
    }

    public void setL2gwVtepIp(String l2gwVtepIp) {
        this.l2gwVtepIp = l2gwVtepIp;
    }

    public String getCnaVtepIp() {
        return cnaVtepIp;
    }

    public void setCnaVtepIp(String cnaVtepIp) {
        this.cnaVtepIp = cnaVtepIp;
    }

    public String getDvrPortMac() {
        return dvrPortMac;
    }

    public void setDvrPortMac(String dvrPortMac) {
        this.dvrPortMac = dvrPortMac;
    }
}
