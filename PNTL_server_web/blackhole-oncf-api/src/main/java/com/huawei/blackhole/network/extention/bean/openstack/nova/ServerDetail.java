package com.huawei.blackhole.network.extention.bean.openstack.nova;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@JsonRootName("server")
public class ServerDetail implements Serializable {
    private static final long serialVersionUID = 3011984247524594600L;

    @JsonProperty("name")
    private String name;

    @JsonProperty("hostId")
    private String hostId;

    @JsonProperty("OS-EXT-AZ:availability_zone")
    private String az;

    @JsonProperty("OS-EXT-SRV-ATTR:host")
    private String pod;

    @JsonProperty("tenant_id")
    private String tenantId;
    
    @JsonProperty("OS-EXT-STS:task_state")
    private String taskState;
    
    @JsonProperty("status")
    private String vmStatus;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getAz() {
        return az;
    }

    public void setAz(String az) {
        this.az = az;
    }

    public String getPod() {
        return pod;
    }

    public void setPod(String pod) {
        this.pod = pod;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTaskState() {
        return taskState;
    }

    public void setTaskState(String taskState) {
        this.taskState = taskState;
    }

    public String getVmStatus() {
        return vmStatus;
    }

    public void setVmStatus(String vmStatus) {
        this.vmStatus = vmStatus;
    }

    @Override
    public String toString() {
        return "ServerDetail [name=" + name + ", hostId=" + hostId + ", az="
                + az + ", pod=" + pod + ", tenantId=" + tenantId
                + ", taskState=" + taskState + ", vmStatus=" + vmStatus + "]";
    }
}
