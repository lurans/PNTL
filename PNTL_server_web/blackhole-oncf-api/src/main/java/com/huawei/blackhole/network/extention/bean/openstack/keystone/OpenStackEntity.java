package com.huawei.blackhole.network.extention.bean.openstack.keystone;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface OpenStackEntity extends Serializable {

}
