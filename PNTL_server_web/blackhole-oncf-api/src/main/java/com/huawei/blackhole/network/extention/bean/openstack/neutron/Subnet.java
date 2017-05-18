package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Subnet implements Serializable {
    private static final long serialVersionUID = -5459479266630293335L;

    @JsonProperty("subnet")
    private SubnetDetatil subnetDetail;

    public SubnetDetatil getSubnetDetail() {
        return subnetDetail;
    }

    public void setSubnetDetail(SubnetDetatil subnetDetail) {
        this.subnetDetail = subnetDetail;
    }

    @Override
    public String toString() {
        return "Subnet [subnetDetail=" + subnetDetail + "]";
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class SubnetDetatil implements Serializable {
        private static final long serialVersionUID = -7237526302843625630L;
        @JsonProperty("name")
        private String name;

        @JsonProperty("network_id")
        private String network_id;

        @JsonProperty("tenant_id")
        private String tenant_id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNetwork_id() {
            return network_id;
        }

        public void setNetwork_id(String network_id) {
            this.network_id = network_id;
        }

        public String getTenant_id() {
            return tenant_id;
        }

        public void setTenant_id(String tenant_id) {
            this.tenant_id = tenant_id;
        }

        @Override
        public String toString() {
            return "SubnetDetatil [name=" + name + ", network_id=" + network_id
                    + ", tenant_id=" + tenant_id + "]";
        }
    }
}
