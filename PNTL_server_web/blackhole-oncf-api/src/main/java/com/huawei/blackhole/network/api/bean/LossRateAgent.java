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

        @JsonProperty("time")
        private String time;

        @JsonProperty("packet-sent")
        private String packetSent;

        @JsonProperty("packet-drops")
        private String packetDrops;

        @JsonProperty("package-size")
        private String packageSize;

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

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
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

        public String getPackageSize() {
            return packageSize;
        }

        public void setPackageSize(String packageSize) {
            this.packageSize = packageSize;
        }
    }

}
