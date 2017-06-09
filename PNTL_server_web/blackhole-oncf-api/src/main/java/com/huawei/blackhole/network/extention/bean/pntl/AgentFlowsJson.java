package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgentFlowsJson implements Serializable {
    private static final long serialVersionUID = -1806885486879192482L;
/*
    @JsonProperty("orgnizationSignature")
    private String orgnizationSignature;

    @JsonProperty("serverIp")
    private String serverIp;

    @JsonProperty("action")
    private String action;

    @JsonProperty("content")
    private String content;
*/
    @JsonProperty("flows")
    private List<FlowList> list;
/*
    public String getOrgnizationSignature() {
        return orgnizationSignature;
    }

    public void setOrgnizationSignature(String orgnizationSignature) {
        this.orgnizationSignature = orgnizationSignature;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
*/
    public List<FlowList> getList() {
        return list;
    }

    public void setList(List<FlowList> list) {
        this.list = list;
    }

    @JsonRootName("flows")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class FlowList implements Serializable {

        private static final long serialVersionUID = 4709870765271194078L;

        @JsonProperty("urgent")
        private String urgent;

        @JsonProperty("sip")
        private String sip;

        @JsonProperty("dip")
        private String dip;

        @JsonProperty("ip-protocol")
        private String ip_protocol;

        @JsonProperty("sport-min")
        private int sport_min;

        @JsonProperty("sport-max")
        private int sport_max;

        @JsonProperty("sport-range")
        private int sport_range;

        @JsonProperty("dscp")
        private int dscp;

        @JsonProperty("topology-tag")
        private TopologyTag topologyTag;

        public String getUrgent() {
            return urgent;
        }

        public void setUrgent(String urgent) {
            this.urgent = urgent;
        }

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

        public String getIp_protocol() {
            return ip_protocol;
        }

        public void setIp_protocol(String ip_protocol) {
            this.ip_protocol = ip_protocol;
        }

        public int getSport_min() {
            return sport_min;
        }

        public void setSport_min(int sport_min) {
            this.sport_min = sport_min;
        }

        public int getSport_max() {
            return sport_max;
        }

        public void setSport_max(int sport_max) {
            this.sport_max = sport_max;
        }

        public int getSport_range() {
            return sport_range;
        }

        public void setSport_range(int sport_range) {
            this.sport_range = sport_range;
        }

        public int getDscp() {
            return dscp;
        }

        public void setDscp(int dscp) {
            this.dscp = dscp;
        }

        public TopologyTag getTopologyTag() {
            return topologyTag;
        }

        public void setTopologyTag(TopologyTag topologyTag) {
            this.topologyTag = topologyTag;
        }

        public static final class TopologyTag implements Serializable {

            private static final long serialVersionUID = 6030718351544239955L;
            @JsonProperty("level")
            private int level;

            @JsonProperty("src-id")
            private int srcId;

            @JsonProperty("dst-id")
            private int dstId;

            public int getLevel() {
                return level;
            }

            public void setLevel(int level) {
                this.level = level;
            }

            public int getSrcId() {
                return srcId;
            }

            public void setSrcId(int srcId) {
                this.srcId = srcId;
            }

            public int getDstId() {
                return dstId;
            }

            public void setDstId(int dstId) {
                this.dstId = dstId;
            }
        }
    }

    @Override
    public String toString() {
        String str = "{\"flows\": [";
        for (FlowList flowlist : list) {
            str += "{";
            str += "\"urgent\":" + "\"" + flowlist.getUrgent() + "\""
                    + ",\"sip\":" + "\"" + flowlist.getSip() + "\""
                    + ",\"dip\":" + "\"" + flowlist.getDip() + "\""
                    + ",\"ip-protocol\":" + "\"" + flowlist.getIp_protocol() + "\""
                    + ",\"sport-min\":" + flowlist.getSport_min()
                    + ",\"sport-max\":" + flowlist.getSport_max()
                    + ",\"sport-range\":" + flowlist.getSport_range()
                    + ",\"dscp\":" + flowlist.getDscp()
                    + ",\"topology-tag\":{"
                    + "\"level\":" + flowlist.getTopologyTag().getLevel()
                    + ",\"src-id\":" + flowlist.getTopologyTag().getSrcId()
                    + ",\"dst-id\":" + flowlist.getTopologyTag().getDstId() + "}}";
            if (list.lastIndexOf(flowlist) != list.size() - 1){
                str += ",";
            }
        }
        str += "]}";
        return str;
    }
}
