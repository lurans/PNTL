package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class FloatingIps implements Serializable {
    private static final long serialVersionUID = -446256992071642394L;

    @JsonProperty("floatingips")
    private List<FloatingIp> floatingIpList;


    public List<FloatingIp> getFloatingIpList() {
        return floatingIpList;
    }

    public void setFloatingIpList(List<FloatingIp> floatingIpList) {
        this.floatingIpList = floatingIpList;
    }


    @Override
    public String toString() {
        return "FloatingIps [floatingIpList=" + floatingIpList + "]";
    }


    @JsonRootName("floatingip")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static final class FloatingIp implements Serializable {
        private static final long serialVersionUID = 1526677024529005440L;

        @JsonProperty("floating_ip_address")
        private String floatingIpAddress;

        @JsonProperty("fixed_ip_address")
        private String fixedIpAddress;

        @JsonProperty("status")
        private String status;

        @JsonProperty("tenant_id")
        private String tenantId;

        @JsonProperty("port_id")
        private String portId;

        @JsonProperty("id")
        private String id;

        public String getFloatingIpAddress() {
            return floatingIpAddress;
        }

        public void setFloatingIpAddress(String floatingIpAddress) {
            this.floatingIpAddress = floatingIpAddress;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getTenantId() {
            return tenantId;
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
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

        public String getFixedIpAddress() {
            return fixedIpAddress;
        }

        public void setFixedIpAddress(String fixedIpAddress) {
            this.fixedIpAddress = fixedIpAddress;
        }

        @Override
        public String toString() {
            return "FloatingIp [floatingIpAddress=" + floatingIpAddress
                    + ", fixedIpAddress=" + fixedIpAddress + ", status="
                    + status + ", tenantId=" + tenantId + ", portId=" + portId
                    + ", id=" + id + "]";
        }
    }
}
