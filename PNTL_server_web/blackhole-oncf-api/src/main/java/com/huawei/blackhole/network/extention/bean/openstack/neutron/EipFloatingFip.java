package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class EipFloatingFip implements Serializable {
    private static final long serialVersionUID = -5008720904058772513L;

    @JsonProperty("floatingips")
    private List<EipFloatingIp> floatingIpList;

    public List<EipFloatingIp> getFloatingIpList() {
        return floatingIpList;
    }

    public void setFloatingIpList(List<EipFloatingIp> floatingIpList) {
        this.floatingIpList = floatingIpList;
    }

    @Override
    public String toString() {
        return "FloatingIps [floatingIpList=" + floatingIpList + "]";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static final class EipFloatingIp implements Serializable {
        private static final long serialVersionUID = 6471417253276093471L;
        @JsonProperty("floating_network_id")
        private String floatingNetworkId;

        @JsonProperty("router_id")
        private String routerId;

        @JsonProperty("fixed_ip_address")
        private String fixedIpAddress;

        @JsonProperty("floating_ip_address")
        private String floatingIpAddress;

        @JsonProperty("tenant_id")
        private String tenantId;

        @JsonProperty("status")
        private String status;

        @JsonProperty("port_id")
        private String portId;

        @JsonProperty("id")
        private String id;

        public String getFloatingNetworkId() {
            return floatingNetworkId;
        }

        public void setFloatingNetworkId(String floatingNetworkId) {
            this.floatingNetworkId = floatingNetworkId;
        }

        public String getRouterId() {
            return routerId;
        }

        public void setRouterId(String routerId) {
            this.routerId = routerId;
        }

        public String getFixedIpAddress() {
            return fixedIpAddress;
        }

        public void setFixedIpAddress(String fixedIpAddress) {
            this.fixedIpAddress = fixedIpAddress;
        }

        public String getFloatingIpAddress() {
            return floatingIpAddress;
        }

        public void setFloatingIpAddress(String floatingIpAddress) {
            this.floatingIpAddress = floatingIpAddress;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getPortId() {
            return portId;
        }

        public void setPortId(String portId) {
            this.portId = portId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

}
