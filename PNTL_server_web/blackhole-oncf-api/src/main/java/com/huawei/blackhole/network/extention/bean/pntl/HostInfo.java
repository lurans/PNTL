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
        @JsonProperty("hostName")
        private String hostName;
        @JsonProperty("hostInstanceSN")
        private String hostInstanceSN;

        @JsonProperty("poolId")
        private String poolId;

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
        @JsonProperty("lastUpdateTime")
        private String lastUpdateTime;
        @JsonProperty("description")
        private String description;
        @JsonProperty("environmentName")
        private String environmentName;
        @JsonProperty("regionName")
        private String regionName;
        @JsonProperty("zoneName")
        private String zoneName;
        @JsonProperty("podName")
        private String podName;
        @JsonProperty("clusterName")
        private String clusterName;
        @JsonProperty("serviceName")
        private String serviceName;
        @JsonProperty("domainName")
        private String domainName;
        @JsonProperty("environmentId")
        private String environmentId;
        @JsonProperty("regionId")
        private String regionId;
        @JsonProperty("zoneId")
        private String zoneId;
        @JsonProperty("podId")
        private String podId;
        @JsonProperty("clusterId")
        private String clusterId;
        @JsonProperty("serviceId")
        private String serviceId;
        @JsonProperty("hostTags")
        private String hostTags;
        @JsonProperty("hostInstanceId")
        private String hostInstanceId;
        @JsonProperty("belongComponents")
        private String belongComponents;

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

        public String getLastUpdateTime() {
            return lastUpdateTime;
        }

        public void setLastUpdateTime(String lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getEnvironmentName() {
            return environmentName;
        }

        public void setEnvironmentName(String environmentName) {
            this.environmentName = environmentName;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getZoneName() {
            return zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public String getPodName() {
            return podName;
        }

        public void setPodName(String podName) {
            this.podName = podName;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getDomainName() {
            return domainName;
        }

        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }

        public String getEnvironmentId() {
            return environmentId;
        }

        public void setEnvironmentId(String environmentId) {
            this.environmentId = environmentId;
        }

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
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

        public String getClusterId() {
            return clusterId;
        }

        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getHostTags() {
            return hostTags;
        }

        public void setHostTags(String hostTags) {
            this.hostTags = hostTags;
        }

        public String getHostInstanceId() {
            return hostInstanceId;
        }

        public void setHostInstanceId(String hostInstanceId) {
            this.hostInstanceId = hostInstanceId;
        }

        public String getBelongComponents() {
            return belongComponents;
        }

        public void setBelongComponents(String belongComponents) {
            this.belongComponents = belongComponents;
        }
    }
}
