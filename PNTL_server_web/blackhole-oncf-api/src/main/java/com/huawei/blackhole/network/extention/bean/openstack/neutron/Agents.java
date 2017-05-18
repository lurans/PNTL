package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Agents implements Serializable {

    private static final long serialVersionUID = -1532761747811308274L;
    @JsonProperty("agents")
    private List<Agent> agents;

    public List<Agent> getAgents() {
        return agents;
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static final class Agent implements Serializable {

        private static final long serialVersionUID = -2525792041313432129L;

        @JsonProperty("id")
        private String id;

        @JsonProperty("binary")
        private String binary;

        @JsonProperty("host")
        private String host;

        @JsonProperty("configurations")
        private Configurations configurations;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBinary() {
            return binary;
        }

        public void setBinary(String binary) {
            this.binary = binary;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Configurations getConfigurations() {
            return configurations;
        }

        public void setConfigurations(Configurations configurations) {
            this.configurations = configurations;
        }

        public static final class Configurations implements Serializable {

            private static final long serialVersionUID = 4241723024129475684L;
            @JsonProperty("tunneling_ip")
            private String tunnelingIp;

            public String getTunnelingIp() {
                return tunnelingIp;
            }

            public void setTunnelingIip(String tunnelingIp) {
                this.tunnelingIp = tunnelingIp;
            }

        }

    }
}
