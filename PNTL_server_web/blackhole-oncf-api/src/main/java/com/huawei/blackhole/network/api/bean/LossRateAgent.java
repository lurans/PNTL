package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LossRateAgent implements Serializable {
    private static final long serialVersionUID = -6384635186269797829L;

    @JsonProperty("orgnizationSignature")
    private String orgnizationSignature;

    @JsonProperty("flow")
    private List<Flow> flow;

    public String getOrgnizationSignature() {
        return orgnizationSignature;
    }

    public void setOrgnizationSignature(String orgnizationSignature) {
        this.orgnizationSignature = orgnizationSignature;
    }

    public List<Flow> getFlow() {
        return flow;
    }

    public void setFlow(List<Flow> flow) {
        this.flow = flow;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class Flow implements Serializable{
        private static final long serialVersionUID = 1086864843898328813L;

        @JsonProperty("sip")
        private String sip;

        @JsonProperty("dip")
        private String dip;

        @JsonProperty("sport")
        private String sport;

        @JsonProperty("dport")
        private String dport;

        @JsonProperty("ip-protocol")
        private String ipProtocol;

        @JsonProperty("dscp")
        private String dscp;

        @JsonProperty("urgent-flag")
        private String urgentFlag;

        @JsonProperty("topology-tag")
        private TopologyTag topo;

        @JsonProperty("statistics")
        private Statistics st;

        public String getSip() {
            return sip;
        }

        public void setSip(String sip) {
            this.sip = sip;
        }

        public String getDip() {
            return dip;
        }

        public void setDip(String dip) {
            this.dip = dip;
        }

        public String getSport() {
            return sport;
        }

        public void setSport(String sport) {
            this.sport = sport;
        }

        public String getDport() {
            return dport;
        }

        public void setDport(String dport) {
            this.dport = dport;
        }

        public String getIpProtocol() {
            return ipProtocol;
        }

        public void setIpProtocol(String ipProtocol) {
            this.ipProtocol = ipProtocol;
        }

        public String getDscp() {
            return dscp;
        }

        public void setDscp(String dscp) {
            this.dscp = dscp;
        }

        public String getUrgentFlag() {
            return urgentFlag;
        }

        public void setUrgentFlag(String urgentFlag) {
            this.urgentFlag = urgentFlag;
        }

        public TopologyTag getTopo() {
            return topo;
        }

        public void setTopo(TopologyTag topo) {
            this.topo = topo;
        }

        public Statistics getSt() {
            return st;
        }

        public void setSt(Statistics st) {
            this.st = st;
        }
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static final class TopologyTag implements Serializable{
            private static final long serialVersionUID = 1759625151186404547L;

            @JsonProperty("level")
            private String level;

            @JsonProperty("sid")
            private String sid;

            @JsonProperty("did")
            private String did;

            public String getLevel() {
                return level;
            }

            public void setLevel(String level) {
                this.level = level;
            }

            public String getSid() {
                return sid;
            }

            public void setSid(String sid) {
                this.sid = sid;
            }

            public String getDid() {
                return did;
            }

            public void setDid(String did) {
                this.did = did;
            }
        }
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static final class Statistics implements Serializable{
            private static final long serialVersionUID = 8880859779396250759L;

            @JsonProperty("t")
            private String t;

            @JsonProperty("packet-sent")
            private String packetSent;

            @JsonProperty("packet-drops")
            private String packetDrops;

            public String getT() {
                return t;
            }

            public void setT(String t) {
                this.t = t;
            }

            public String getPacketSent() {
                return packetSent;
            }

            public void setPacketSent(String packetSent) {
                this.packetSent = packetSent;
            }

            public String getPacketDrops() {
                return packetDrops;
            }

            public void setPacketDrops(String packetDrops) {
                this.packetDrops = packetDrops;
            }
        }
    }

}
