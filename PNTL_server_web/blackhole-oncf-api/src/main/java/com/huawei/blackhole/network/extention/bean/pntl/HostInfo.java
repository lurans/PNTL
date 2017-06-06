package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by y00214328 on 2017/5/19.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HostInfo implements Iterable<HostInfo.HostListInfo>, Serializable {
    private static final long serialVersionUID = -1952026214678135454L;

    @JsonProperty("total")
    private int total;

    @JsonProperty("result")
    private List<HostListInfo> list;

    public Iterator<HostInfo.HostListInfo> iterator() {
        return list.iterator();
    }

    public List<HostListInfo> getHostsInfoList() {
        return list;
    }

    public void setHostsInfoList(List<HostListInfo> list) {
        this.list = list;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @JsonRootName("result")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class HostListInfo implements Serializable {
        private static final long serialVersionUID = -4088801235353341150L;

        @JsonProperty("id")
        private String id;

        @JsonProperty("hostInstanceSN")
        private String hostInstanceSN;

        @JsonProperty("poolId")
        private String poolId;

        @JsonProperty("hostName")
        private String hostName;

        @JsonProperty("ip")
        private String ip;

        @JsonProperty("allIp")
        private String allIp;

        @JsonProperty("bmcIp")
        private String bmcIp;

        @JsonProperty("os")
        private String os;

        @JsonProperty("resourceStatus")
        private String resourceStatus;

        @JsonProperty("agentSN")
        private String agentSN;

        @JsonProperty("agentStatus")
        private String agentStatus;

        @JsonProperty("hostType")
        private String hostType;

        @JsonProperty("hostClass")
        private String hostClass;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHostInstanceSN() {
            return hostInstanceSN;
        }

        public void setHostInstanceSN(String hostInstanceSN) {
            this.hostInstanceSN = hostInstanceSN;
        }

        public String getPoolId() {
            return poolId;
        }

        public void setPoolId(String poolId) {
            this.poolId = poolId;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getAllIp() {
            return allIp;
        }

        public void setAllIp(String allIp) {
            this.allIp = allIp;
        }

        public String getBmcIp() {
            return bmcIp;
        }

        public void setBmcIp(String bmcIp) {
            this.bmcIp = bmcIp;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getResourceStatus() {
            return resourceStatus;
        }

        public void setResourceStatus(String resourceStatus) {
            this.resourceStatus = resourceStatus;
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

        public String getHostType() {
            return hostType;
        }

        public void setHostType(String hostType) {
            this.hostType = hostType;
        }

        public String getHostClass() {
            return hostClass;
        }

        public void setHostClass(String hostClass) {
            this.hostClass = hostClass;
        }
    }
}
