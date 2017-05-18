package com.huawei.blackhole.network.extention.bean.openstack.cps;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HostInfo implements Serializable {
    private static final long serialVersionUID = 592137532155687202L;
    
    @JsonProperty("network")
    private List<HostNetwork> network;    

    public List<HostNetwork> getNetwork() {
        return network;
    }

    public void setNetwork(List<HostNetwork> network) {
        this.network = network;
    }


    public static final class HostNetwork implements Serializable {
        private static final long serialVersionUID = 2078667120813192840L;
        
        @JsonProperty("ip")
        private String ip;
        
        @JsonProperty("name")
        private String name;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
    }

}
