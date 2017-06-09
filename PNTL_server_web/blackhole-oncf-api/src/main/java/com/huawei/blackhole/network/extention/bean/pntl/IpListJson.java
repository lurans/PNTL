package com.huawei.blackhole.network.extention.bean.pntl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IpListJson implements Serializable{
    private static final long serialVersionUID = 605780660975330041L;
    @JsonProperty("az_id")
    private String azId;

    @JsonProperty("pod_id")
    private String podId;

    @JsonProperty("result")
    private List<IpList> list;

    public String getAzId() {
        return azId;
    }

    public void setAzId(String azId) {
        this.azId = azId;
    }

    public String getPodId() {
        return podId;
    }

    public void setPodId(String podId) {
        this.podId = podId;
    }

    public List<IpList> getList() {
        return list;
    }

    public void setList(List<IpList> list) {
        this.list = list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class IpList implements Serializable{
        private static final long serialVersionUID = -4981859300324503308L;
        @JsonProperty("ip")
        private String ip;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }
    }
}
