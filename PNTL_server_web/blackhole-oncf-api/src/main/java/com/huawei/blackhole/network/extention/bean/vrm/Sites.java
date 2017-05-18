package com.huawei.blackhole.network.extention.bean.vrm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Sites implements Serializable {
    private static final long serialVersionUID = 7747961557399496744L;

    private List<Site> sites;

    public List<Site> getSites() {
        return sites;
    }

    public void setSites(List<Site> sites) {
        this.sites = sites;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(Include.NON_NULL)
    public static class Site implements Serializable {
        private static final long serialVersionUID = 6523268206792725360L;

        private String ntpIp;

        private String uri;

        private String urn;

        private String timeZone;

        private int ntpCycle;

        private String name;

        public String getNtpIp() {
            return ntpIp;
        }

        public void setNtpIp(String ntpIp) {
            this.ntpIp = ntpIp;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getUrn() {
            return urn;
        }

        public void setUrn(String urn) {
            this.urn = urn;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public void setTimeZone(String timeZone) {
            this.timeZone = timeZone;
        }

        public int getNtpCycle() {
            return ntpCycle;
        }

        public void setNtpCycle(int ntpCycle) {
            this.ntpCycle = ntpCycle;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


    }

}
