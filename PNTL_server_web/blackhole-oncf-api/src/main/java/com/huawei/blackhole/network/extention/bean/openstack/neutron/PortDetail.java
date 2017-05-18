package com.huawei.blackhole.network.extention.bean.openstack.neutron;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.huawei.blackhole.network.extention.bean.openstack.neutron.Ports.Port;

import java.io.Serializable;

public class PortDetail implements Serializable {
    private static final long serialVersionUID = 8777504621341315580L;

    @JsonProperty("port")
    private Port portDetail;

    public Port getPortDetail() {
        return portDetail;
    }

    public void setPortDetail(Port portDetail) {
        this.portDetail = portDetail;
    }

    @Override
    public String toString() {
        return "PortDetail [portDetail=" + portDetail + "]";
    }
}
