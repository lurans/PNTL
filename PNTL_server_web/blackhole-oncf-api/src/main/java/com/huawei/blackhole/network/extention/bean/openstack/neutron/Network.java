package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Network implements Serializable {
    private static final long serialVersionUID = -524343579288673886L;

    @JsonProperty("network")
    private NetworkDetail networkDetail;


    public NetworkDetail getNetworkDetail() {
        return networkDetail;
    }


    public void setNetworkDetail(NetworkDetail networkDetail) {
        this.networkDetail = networkDetail;
    }

    @Override
    public String toString() {
        return "Network [networkDetail=" + networkDetail + "]";
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class NetworkDetail implements Serializable {
        private static final long serialVersionUID = 942113041857050155L;

        @JsonProperty("name")
        private String name;

        @JsonProperty("status")
        private String status;

        @JsonProperty("tenant_id")
        private String tenant_id;

        @JsonProperty("provider:segmentation_id")
        private String provdSegmtId;

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

        public String getTenant_id() {
            return tenant_id;
        }

        public void setTenant_id(String tenant_id) {
            this.tenant_id = tenant_id;
        }

        public String getProvdSegmtId() {
            return provdSegmtId;
        }

        public void setProvdSegmtId(String provdSegmtId) {
            this.provdSegmtId = provdSegmtId;
        }

        @Override
        public String toString() {
            return String.format("network details : [name:%s, status:%s, tenant_id:%s, provider:segmentation_id:%s]",
                    name, status, tenant_id, provdSegmtId);
        }
    }
}
