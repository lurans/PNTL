package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Ports implements Iterable<Ports.Port>, Serializable {
    private static final long serialVersionUID = 7268762512171109340L;

    @JsonProperty("ports")
    private List<Port> list;

    /**
     * @return the list
     */
    public List<Port> getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(List<Port> list) {
        this.list = list;
    }

    public Iterator<Port> iterator() {
        return list.iterator();
    }

    @Override
    public String toString() {
        return "Ports [list=" + list + "]";
    }


    @JsonRootName("port")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static final class Port implements Serializable {

        private static final long serialVersionUID = -978676476019224773L;
        private String id;
        private String name;
        @JsonProperty("admin_state_up")
        private String adminStateUp;
        @JsonProperty("fixed_ips")
        private List<Ip> list;
        @JsonProperty("mac_address")
        private String macAddress;
        @JsonProperty("network_id")
        private String networkId;
        @JsonProperty("binding:vif_type")
        private String vifType;
        @JsonProperty("binding:profile")
        private Map<String, Object> capabilities;
        private String status;
        @JsonProperty("tenant_id")
        private String tenantId;
        @JsonProperty("device_id")
        private String deviceId;
        @JsonProperty("device_owner")
        private String deviceOwner;
        @JsonProperty("security_groups")
        private List<String> securityGroups;
        @JsonProperty("extra_dhcp_opts")
        private List<Map<String, String>> extraDhcpOpts;
        @JsonProperty("allowed_address_pairs")
        private List<Map<String, String>> allowedAddressPairs;

        public List<Map<String, String>> getAllowedAddressPairs() {
            return allowedAddressPairs;
        }

        public void setAllowedAddressPairs(List<Map<String, String>> allowedAddressPairs) {
            this.allowedAddressPairs = allowedAddressPairs;
        }

        public List<Map<String, String>> getExtraDhcpOpts() {
            return extraDhcpOpts;
        }

        public void setExtraDhcpOpts(List<Map<String, String>> extraDhcpOpts) {
            this.extraDhcpOpts = extraDhcpOpts;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAdminStateUp() {
            return adminStateUp;
        }

        public void setAdminStateUp(String adminStateUp) {
            this.adminStateUp = adminStateUp;
        }

        public List<Ip> getList() {
            return list;
        }

        public void setList(List<Ip> list) {
            this.list = list;
        }

        public String getMacAddress() {
            return macAddress;
        }

        public void setMacAddress(String macAddress) {
            this.macAddress = macAddress;
        }

        public String getNetworkId() {
            return networkId;
        }

        public void setNetworkId(String networkId) {
            this.networkId = networkId;
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

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceOwner() {
            return deviceOwner;
        }

        public void setDeviceOwner(String deviceOwner) {
            this.deviceOwner = deviceOwner;
        }

        public String getVifType() {
            return vifType;
        }

        public void setVifType(String vifType) {
            this.vifType = vifType;
        }

        public Map<String, Object> getCapabilities() {
            return capabilities;
        }

        public void setCapabilities(Map<String, Object> capabilities) {
            this.capabilities = capabilities;
        }

        public List<String> getSecurityGroups() {
            return securityGroups;
        }

        public void setSecurityGroups(List<String> securityGroups) {
            this.securityGroups = securityGroups;
        }

        @Override
        public String toString() {
            return "Port [id=" + id + ", name=" + name + ", list=" + list
                    + ", macAddress=" + macAddress + ", networkId=" + networkId
                    + ", capabilities=" + capabilities + ", status=" + status
                    + ", tenantId=" + tenantId + ", deviceId=" + deviceId + "]";
        }

        public static final class Ip implements Serializable {

            private static final long serialVersionUID = -4519068534307281692L;

            @JsonProperty("ip_address")
            private String ipAaddress;

            @JsonProperty("subnet_id")
            private String subnetId;

            public String getIpAddress() {
                return ipAaddress;
            }

            public void setIpAddress(String address) {
                this.ipAaddress = address;
            }

            public String getSubnetId() {
                return subnetId;
            }

            public void setSubnetId(String subnetId) {
                this.subnetId = subnetId;
            }

            @Override
            public String toString() {
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("Ip [ipAaddress = ");
                strBuilder.append(ipAaddress);
                strBuilder.append(", subnetId=");
                strBuilder.append(subnetId);
                strBuilder.append("]");
                return strBuilder.toString();
            }

        }
    }
}
