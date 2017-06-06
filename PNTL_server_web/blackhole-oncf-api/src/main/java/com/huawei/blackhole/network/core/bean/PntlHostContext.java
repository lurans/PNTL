package com.huawei.blackhole.network.core.bean;

/**
 * Created by y00214328 on 2017/5/19.
 */
public class PntlHostContext {

    private String id;

    private String ip;

    private String os;

    private String zone;

    private String pod;

    private String agentSN;

    private String agentStatus;

    private String hostClass;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getAgentSN() {
        return agentSN;
    }

    public void setAgentSN(String agentSN) {
        this.agentSN = agentSN;
    }

    public String getAgentStatus() {
        return agentStatus;
    }

    public void setAgentStatus(String agentStatus) {
        this.agentStatus = agentStatus;
    }

    public String getHostClass() {
        return hostClass;
    }

    public void setHostClass(String hostClass) {
        this.hostClass = hostClass;
    }
}
