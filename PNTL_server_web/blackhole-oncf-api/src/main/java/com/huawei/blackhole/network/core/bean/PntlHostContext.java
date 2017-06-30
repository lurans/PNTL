package com.huawei.blackhole.network.core.bean;

import com.huawei.blackhole.network.extention.bean.pntl.HostInfo;
import com.huawei.blackhole.network.extention.bean.vrm.Hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PntlHostContext {
    private String id;

    private String agentIp;

    private String vbondIp;

    private String os;

    private String zoneId;

    private String podId;

    private String agentSN;

    private String agentStatus;

    private String hostClass;

    private Map<String, List<String>> pingMeshList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentIp() {
        return agentIp;
    }

    public void setAgentIp(String agentIp) {
        this.agentIp = agentIp;
    }

    public String getVbondIp() {
        return vbondIp;
    }

    public void setVbondIp(String vbondIp) {
        this.vbondIp = vbondIp;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(String podId) {
        this.podId = podId;
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

    public Map<String, List<String>> getPingMeshList() {
        return pingMeshList;
    }

    public void setPingMeshList(Map<String, List<String>> pingMeshList) {
        this.pingMeshList = pingMeshList;
    }

    public void setPingMeshList(String srcIp, List<PntlHostContext> hostsList){
        Map<String, List<String>> pingMeshList = new HashMap<>();
        List<String> ips = new ArrayList<>();
        for (PntlHostContext host : hostsList){
            if (srcIp.equals(host.getAgentIp())){
                continue;
            }
            if (host.getVbondIp() != null) {
                ips.add(host.getVbondIp());
            }
        }
        if (ips == null || ips.isEmpty()){
            return;
        }
        pingMeshList.put(srcIp, ips);
        setPingMeshList(pingMeshList);
    }
}
