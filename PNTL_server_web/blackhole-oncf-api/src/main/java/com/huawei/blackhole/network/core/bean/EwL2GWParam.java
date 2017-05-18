package com.huawei.blackhole.network.core.bean;

public class EwL2GWParam {
    private CommonCallableParam commonParam;

    private String srcVmIp;
    private String srcVmMac;
    private String dstVmIp;
    private String dstVmMac;
    private String nodeFlag;// input / output
    private String srcVtepIp;
    private String dstVtepIp;
    private String l2gwVtepIp;
    private String srcProvdSegmtId;
    private String dstProvdSegmtId;

    public EwL2GWParam(CommonCallableParam commonParam) {
        super();
        this.commonParam = commonParam;
    }

    public String getDstProvdSegmtId() {
        return dstProvdSegmtId;
    }

    public void setDstProvdSegmtId(String dstProvdSegmtId) {
        this.dstProvdSegmtId = dstProvdSegmtId;
    }

    public String getSrcProvdSegmtId() {
        return srcProvdSegmtId;
    }

    public void setSrcProvdSegmtId(String srcProvdSegmtId) {
        this.srcProvdSegmtId = srcProvdSegmtId;
    }

    public CommonCallableParam getCommonParam() {
        return commonParam;
    }

    public void setCommonParam(CommonCallableParam commonParam) {
        this.commonParam = commonParam;
    }

    public String getSrcVmIp() {
        return srcVmIp;
    }

    public void setSrcVmIp(String srcVmIp) {
        this.srcVmIp = srcVmIp;
    }

    public String getSrcVmMac() {
        return srcVmMac;
    }

    public void setSrcVmMac(String srcVmMac) {
        this.srcVmMac = srcVmMac;
    }

    public String getDstVmIp() {
        return dstVmIp;
    }

    public void setDstVmIp(String dstVmIp) {
        this.dstVmIp = dstVmIp;
    }

    public String getDstVmMac() {
        return dstVmMac;
    }

    public void setDstVmMac(String dstVmMac) {
        this.dstVmMac = dstVmMac;
    }

    public String getNodeFlag() {
        return nodeFlag;
    }

    public void setNodeFlag(String nodeFlag) {
        this.nodeFlag = nodeFlag;
    }

    public String getSrcVtepIp() {
        return srcVtepIp;
    }

    public void setSrcVtepIp(String srcVtepIp) {
        this.srcVtepIp = srcVtepIp;
    }

    public String getDstVtepIp() {
        return dstVtepIp;
    }

    public void setDstVtepIp(String dstVtepIp) {
        this.dstVtepIp = dstVtepIp;
    }

    public String getL2gwVtepIp() {
        return l2gwVtepIp;
    }

    public void setL2gwVtepIp(String l2gwVtepIp) {
        this.l2gwVtepIp = l2gwVtepIp;
    }

}
