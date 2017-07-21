package com.huawei.blackhole.network.api.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelayInfoAgent implements Serializable{
    private static final long serialVersionUID = -3290461366597121663L;

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
        private static final long serialVersionUID = 7541758710171734304L;

        @JsonProperty("sip")
        private String sip;
        @JsonProperty("dip")
        private String dip;
        @JsonProperty("sport")
        private String sport;
        @JsonProperty("time")
        private String time;
        @JsonProperty("times")
        private Time times;
        @JsonProperty("statistics")
        private Statistics st;
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

        public Statistics getSt() {
            return st;
        }

        public void setSt(Statistics st) {
            this.st = st;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public Time getTimes() {
            return times;
        }

        public void setTimes(Time times) {
            this.times = times;
        }

        public String getPackageSize() {
            return packageSize;
        }

        public void setPackageSize(String packageSize) {
            this.packageSize = packageSize;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static final class Time implements Serializable{
            private static final long serialVersionUID = -6001470612230917928L;
            @JsonProperty("t1")
            private String t1;
            @JsonProperty("t2")
            private String t2;
            @JsonProperty("t3")
            private String t3;
            @JsonProperty("t4")
            private String t4;

            public String getT1() {
                return t1;
            }

            public void setT1(String t1) {
                this.t1 = t1;
            }

            public String getT2() {
                return t2;
            }

            public void setT2(String t2) {
                this.t2 = t2;
            }

            public String getT3() {
                return t3;
            }

            public void setT3(String t3) {
                this.t3 = t3;
            }

            public String getT4() {
                return t4;
            }

            public void setT4(String t4) {
                this.t4 = t4;
            }
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static final class Statistics implements Serializable{
            private static final long serialVersionUID = 5184767604508341986L;
            @JsonProperty("packet-sent")
            private String packetSent;
            @JsonProperty("packet-drops")
            private String packetDrops;
            @JsonProperty("50percentile")
            private String percentile50;
            @JsonProperty("99percentile")
            private String percentile99;
            @JsonProperty("standard-deviation")
            private String standardDeviation;
            @JsonProperty("min")
            private String min;
            @JsonProperty("max")
            private String max;
            @JsonProperty("drop-notices")
            private String dropNotices;

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

            public String getPercentile50() {
                return percentile50;
            }

            public void setPercentile50(String percentile50) {
                this.percentile50 = percentile50;
            }

            public String getPercentile99() {
                return percentile99;
            }

            public void setPercentile99(String percentile99) {
                this.percentile99 = percentile99;
            }

            public String getStandardDeviation() {
                return standardDeviation;
            }

            public void setStandardDeviation(String standardDeviation) {
                this.standardDeviation = standardDeviation;
            }

            public String getDropNotices() {
                return dropNotices;
            }

            public void setDropNotices(String dropNotices) {
                this.dropNotices = dropNotices;
            }

            public String getMin() {
                return min;
            }

            public void setMin(String min) {
                this.min = min;
            }

            public String getMax() {
                return max;
            }

            public void setMax(String max) {
                this.max = max;
            }
        }
    }
}
