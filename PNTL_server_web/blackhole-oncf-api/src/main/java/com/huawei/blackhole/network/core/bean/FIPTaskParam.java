package com.huawei.blackhole.network.core.bean;

public class FIPTaskParam {
    private CommonCallableParam commonParam;

    private String vmIp;
    private String fIp;
    private String eipFloatingIp;
    private String qvmPort;
    private String dvrPort;
    private String dvrDevId;
    private String dvrMac;
    private String remoteIp;
    private String fipNsId;
    private String fipPortId;
    private String fgMac;
    private String localScriptFile;
    private String remoteScriptName;
    private String provdSegmtId;
    private boolean hasSnat;

    public FIPTaskParam(CommonCallableParam commonCallableParam) {
        this.commonParam = commonCallableParam;
    }


    public String getProvdSegmtId() {
        return provdSegmtId;
    }

    public void setProvdSegmtId(String provdSegmtId) {
        this.provdSegmtId = provdSegmtId;
    }

    public String getRemoteScriptName() {
        return remoteScriptName;
    }

    public void setRemoteScriptName(String remoteScriptName) {
        this.remoteScriptName = remoteScriptName;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getfIp() {
        return fIp;
    }

    public void setfIp(String fIp) {
        this.fIp = fIp;
    }

    public String getQvmPort() {
        return qvmPort;
    }

    public void setQvmPort(String qvmPort) {
        this.qvmPort = qvmPort;
    }

    public String getDvrPort() {
        return dvrPort;
    }

    public void setDvrPort(String dvrPort) {
        this.dvrPort = dvrPort;
    }

    public String getDvrDevId() {
        return dvrDevId;
    }

    public void setDvrDevId(String dvrDevId) {
        this.dvrDevId = dvrDevId;
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

    @Override
    public String toString() {
        return "FIPTaskParam [vmIp=" + vmIp + ", fIp=" + fIp + ", qvmPort=" + qvmPort + ", dvrPort=" + dvrPort
                + ", dvrDevId=" + dvrDevId + ", commonParam=" + commonParam.toString() + "]";
    }

    public String getLocalScriptFile() {
        return localScriptFile;
    }

    public void setLocalScriptFile(String localScriptFile) {
        this.localScriptFile = localScriptFile;
    }

    // public String getEip() {
    // return eip;
    // }
    //
    // public void setEip(String eip) {
    // this.eip = eip;
    // }

    public String getEipFloatingIp() {
        return eipFloatingIp;
    }

    public void setEipFloatingIp(String eipFloatingIp) {
        this.eipFloatingIp = eipFloatingIp;
    }

    public boolean isHasSnat() {
        return hasSnat;
    }

    public void setHasSnat(boolean hasSnat) {
        this.hasSnat = hasSnat;
    }

    public String getDvrMac() {
        return dvrMac;
    }

    public void setDvrMac(String dvrMac) {
        this.dvrMac = dvrMac;
    }

    public String getFipNsId() {
        return fipNsId;
    }

    public void setFipNsId(String fipNsId) {
        this.fipNsId = fipNsId;
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

}
