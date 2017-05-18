package com.huawei.blackhole.network.extention.bean.openstack.nova;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class OsAvailabilityZones implements Serializable {
    private static final long serialVersionUID = 7649691720006394638L;

    @JsonProperty("availabilityZoneInfo")
    private List<AvailabilityZone> availabilityZones;

    public List<AvailabilityZone> getAvailabilityZones() {
        return availabilityZones;
    }

    public void setAvailabilityZones(List<AvailabilityZone> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    @Override
    public String toString() {
        return "OsAvailabilityZones [availabilityZones=" + availabilityZones + "]";
    }

    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public final static class AvailabilityZone implements Serializable {
        private static final long serialVersionUID = -8282027237838375322L;

        @JsonProperty("zoneName")
        private String zoneName;

        public String getZoneName() {
            return zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        @Override
        public String toString() {
            return "AvailabilityZone [zoneName=" + zoneName + "]";
        }

    }

}
