package com.huawei.blackhole.network.core.bean;

import java.util.List;

public class EwTaskParam {
    private CommonCallableParam commonParam;

    private String qvmPort;
    private String dvrDevId;
    private String dvrSrcPort;
    private String dvrDstPort;
    private String dvrSrcMac;
    private String dvrDstMac;
    private String dvrSrcRouteIp;
    private String dvrDstRouteIp;
    private String subnetId;
    private String macAddr;
    private String l2gwVtepIp;
    private String vtepIp;
    private String isInSameAz;
    private List<String> l2gwIps;
    private String provdSegmtId;

    public EwTaskParam(CommonCallableParam commonParam) {
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

    public String getDvrDevId() {
        return dvrDevId;
    }

    public void setDvrDevId(String dvrDevId) {
        this.dvrDevId = dvrDevId;
    }

    public String getDvrSrcRouteIp() {
        return dvrSrcRouteIp;
    }

    public void setDvrSrcRouteIp(String dvrSrcRouteIp) {
        this.dvrSrcRouteIp = dvrSrcRouteIp;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getDvrSrcPort() {
        return dvrSrcPort;
    }

    public void setDvrSrcPort(String dvrSrcPort) {
        this.dvrSrcPort = dvrSrcPort;
    }

    public String getDvrDstPort() {
        return dvrDstPort;
    }

    public void setDvrDstPort(String dvrDstPort) {
        this.dvrDstPort = dvrDstPort;
    }

    public String getDvrDstRouteIp() {
        return dvrDstRouteIp;
    }

    public void setDvrDstRouteIp(String dvrDstRouteIp) {
        this.dvrDstRouteIp = dvrDstRouteIp;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public CommonCallableParam getCommonParam() {
        return commonParam;
    }

    public void setCommonParam(CommonCallableParam commonParam) {
        this.commonParam = commonParam;
    }

    @Override
    public String toString() {
        return "VmTaskParam [qvmPort=" + qvmPort + ", dvrDevId=" + dvrDevId + ", dvrSrcPort=" + dvrSrcPort
                + ", dvrDstPort=" + dvrDstPort + ", dvrSrcRouteIp=" + dvrSrcRouteIp + ", commonParam=" + commonParam
                + ", dvrDstRouteIp=" + dvrDstRouteIp + ", subnetId=" + subnetId + ", macAddr=" + macAddr + "]";
    }

    public List<String> getL2gwIps() {
        return l2gwIps;
    }

    public void setL2gwIp(List<String> l2gwIp) {
        this.l2gwIps = l2gwIp;
    }

    public String getL2gwVtepIp() {
        return l2gwVtepIp;
    }

    public void setL2gwVtepIp(String l2gwVtepIp) {
        this.l2gwVtepIp = l2gwVtepIp;
    }

    public String getVtepIp() {
        return vtepIp;
    }

    public void setVtepIp(String vtepIp) {
        this.vtepIp = vtepIp;
    }

    public String getIsInSameAz() {
        return isInSameAz;
    }

    public void setIsInSameAz(String isInSameAz) {
        this.isInSameAz = isInSameAz;
    }

    public String getDvrDstMac() {
        return dvrDstMac;
    }

    public void setDvrDstMac(String dvrDstMac) {
        this.dvrDstMac = dvrDstMac;
    }

    public String getDvrSrcMac() {
        return dvrSrcMac;
    }

    public void setDvrSrcMac(String dvrSrcMac) {
        this.dvrSrcMac = dvrSrcMac;
    }

}
