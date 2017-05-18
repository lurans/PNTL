package com.huawei.blackhole.network.extention.bean.openstack.cps;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Instances implements Serializable{        
    private static final long serialVersionUID = -5822536658633844314L;
    
    @JsonProperty("instances")
    private List<Instance> instances;

    public List<Instance> getInstances() {
        return instances;
    }

    public void setInstances(List<Instance> instances) {
        this.instances = instances;
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Instance implements Serializable {
        private static final long serialVersionUID = 6253995831363809836L;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("runsonhost")
        private String runsonhost;

        @JsonProperty("hastatus")
        private String hastatus;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRunsonhost() {
            return runsonhost;
        }

        public void setRunsonhost(String runsonhost) {
            this.runsonhost = runsonhost;
        }

        public String getHastatus() {
            return hastatus;
        }

        public void setHastatus(String hastatus) {
            this.hastatus = hastatus;
        }
    }
}
