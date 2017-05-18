package com.huawei.blackhole.network.extention.bean.openstack.nova;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports.Port.Ip;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class OsInterfaces implements Serializable {
    private static final long serialVersionUID = -3166345526717737610L;

    @JsonProperty("interfaceAttachments")
    private List<OsInterface> osInterfaces;

    public List<OsInterface> getOsInterfaces() {
        return osInterfaces;
    }

    public void setOsInterfaces(List<OsInterface> osInterfaces) {
        this.osInterfaces = osInterfaces;
    }

    @Override
    public String toString() {
        return "OsInterfaces [osInterfaces=" + osInterfaces + "]";
    }

    @JsonRootName("interfaceAttachment")
    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public final static class OsInterface implements Serializable {
        private static final long serialVersionUID = 7766029521155805169L;

        @JsonProperty("port_state")
        private String portState;

        @JsonProperty("port_id")
        private String portId;

        @JsonProperty("net_id")
        private String netId;

        @JsonProperty("mac_addr")
        private String macAddr;

        @JsonProperty("fixed_ips")
        private List<Ip> ipList;

        public String getPortState() {
            return portState;
        }

        public void setPortState(String portState) {
            this.portState = portState;
        }

        public String getPortId() {
            return portId;
        }

        public void setPortId(String portId) {
            this.portId = portId;
        }

        public String getNetId() {
            return netId;
        }

        public void setNetId(String netId) {
            this.netId = netId;
        }

        public String getMacAddr() {
            return macAddr;
        }

        public void setMacAddr(String macAddr) {
            this.macAddr = macAddr;
        }

        public List<Ip> getIpList() {
            return ipList;
        }

        public void setIpList(List<Ip> ipList) {
            this.ipList = ipList;
        }

        @Override
        public String toString() {
            return "OsInterface [portState=" + portState + ", portId=" + portId + ", netId=" + netId + ", macAddr="
                    + macAddr + ", ipList=" + ipList + "]";
        }

    }

}
