package com.huawei.blackhole.network.core.bean;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.extention.bean.pntl.HostInfo;
import com.huawei.blackhole.network.extention.bean.vrm.Hosts;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;

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

    /*install 状态*/
    private String agentStatus;
    /*install 失败原因*/
    private String reason;

    private String hostClass;

    private Map<String, List<String>> pingMeshList;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

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

    public List<Map<String, String>> convertToMap(List<PntlHostContext> pntlHosts){
        List<Map<String, String>> data = new ArrayList<>();

        for (PntlHostContext host : pntlHosts){
            Map<String, String> d = new HashMap<>();
            d.put("ip", host.getAgentIp());
            d.put("pod", host.getPodId());
            d.put("az", host.getZoneId());
            d.put("os", host.getOs());
            data.add(d);
        }
        return data;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof PntlHostContext){
            PntlHostContext p = (PntlHostContext)obj;
            return this.getAgentIp().equals(p.getAgentIp());
        }
        return super.equals(obj);
    }
}
