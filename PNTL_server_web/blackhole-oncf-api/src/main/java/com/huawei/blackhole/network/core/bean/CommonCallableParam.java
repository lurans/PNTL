package com.huawei.blackhole.network.core.bean;

public class CommonCallableParam {

    private String vmIp;

    private String hostIp;

    private String hostType;

    private String az;

    private String pod;

    private String publicIp;

    public CommonCallableParam() {
        super();
    }

    public CommonCallableParam(String vmIp, String hostIp, String hostType,
            String az, String pod) {
        super();
        this.vmIp = vmIp;
        this.hostIp = hostIp;
        this.hostType = hostType;
        this.az = az;
        this.pod = pod;
    }

    public CommonCallableParam(String vmIp, String hostIp, String hostType,
            String az, String pod, String publicIp) {
        super();
        this.vmIp = vmIp;
        this.hostIp = hostIp;
        this.hostType = hostType;
        this.az = az;
        this.pod = pod;
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

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }

    public String getAz() {
        return az;
    }

    public void setAz(String az) {
        this.az = az;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    @Override
    public String toString() {
        return "CommonCallableParam [vmIp=" + vmIp + ", hostIp=" + hostIp
                + ", hostType=" + hostType + ", az=" + az + ", pod=" + pod
                + ", publicIp=" + publicIp + "]";
    }

}
