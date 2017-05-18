package com.huawei.blackhole.network.extention.bean.vrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Hosts implements Serializable {
    private static final long serialVersionUID = -354518374152482341L;

    private int total;

    private List<Host> hosts;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Host> getHosts() {
        return hosts;
    }

    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static class Host implements Serializable {
        private static final long serialVersionUID = 6554981083185622062L;

        private String name;

        private String status;

        private String clusterName;

        private String ip;

        private String bmcIp;

        private String bmcUserName;

        private String clusterUrn;

        private String computeResourceStatics;

        private int cpuMHz;

        private int cpuQuantity;

        private String hostMultiPathMode;

        private boolean isMaintaining;

        private int memQuantityMB;

        private String multiPathMode;

        private int nicQuantity;

        private String uri;

        private String urn;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getClusterName() {
            return clusterName;
        }

        public void setClusterName(String clusterName) {
            this.clusterName = clusterName;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getBmcIp() {
            return bmcIp;
        }

        public void setBmcIp(String bmcIp) {
            this.bmcIp = bmcIp;
        }

        public String getBmcUserName() {
            return bmcUserName;
        }

        public void setBmcUserName(String bmcUserName) {
            this.bmcUserName = bmcUserName;
        }

        public String getClusterUrn() {
            return clusterUrn;
        }

        public void setClusterUrn(String clusterUrn) {
            this.clusterUrn = clusterUrn;
        }

        public String getComputeResourceStatics() {
            return computeResourceStatics;
        }

        public void setComputeResourceStatics(String computeResourceStatics) {
            this.computeResourceStatics = computeResourceStatics;
        }

        public int getCpuMHz() {
            return cpuMHz;
        }

        public void setCpuMHz(int cpuMHz) {
            this.cpuMHz = cpuMHz;
        }

        public int getCpuQuantity() {
            return cpuQuantity;
        }

        public void setCpuQuantity(int cpuQuantity) {
            this.cpuQuantity = cpuQuantity;
        }

        public String getHostMultiPathMode() {
            return hostMultiPathMode;
        }

        public void setHostMultiPathMode(String hostMultiPathMode) {
            this.hostMultiPathMode = hostMultiPathMode;
        }

        public boolean isMaintaining() {
            return isMaintaining;
        }

        public void setMaintaining(boolean isMaintaining) {
            this.isMaintaining = isMaintaining;
        }

        public int getMemQuantityMB() {
            return memQuantityMB;
        }

        public void setMemQuantityMB(int memQuantityMB) {
            this.memQuantityMB = memQuantityMB;
        }

        public String getMultiPathMode() {
            return multiPathMode;
        }

        public void setMultiPathMode(String multiPathMode) {
            this.multiPathMode = multiPathMode;
        }

        public int getNicQuantity() {
            return nicQuantity;
        }

        public void setNicQuantity(int nicQuantity) {
            this.nicQuantity = nicQuantity;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUrn() {
            return urn;
        }

        public void setUrn(String urn) {
            this.urn = urn;
        }


    }


}
