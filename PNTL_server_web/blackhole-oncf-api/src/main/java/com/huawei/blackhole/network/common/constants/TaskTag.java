package com.huawei.blackhole.network.common.constants;

public class TaskTag {
    public static final String SRC = "src";
    public static final String DST = "dst";

    // ele 用来查找task id 对应的节点类型
    // vm
    public static final String EW_ELE_CNA_TAG = "@ew@cna";
    public static final String EW_CNA_SRC_TAG = "@ew@cna@src";
    public static final String EW_CNA_DST_TAG = "@ew@cna@dst";
    public static final String EW_ELE_L2GW_TAG = "@ew@l2gw";
    public static final String EW_L2GW_WILD_TAG = "%s@ew@l2gw@%s@%d";
    public static final String EW_L2GW_START_TAG = "@ew@l2gw@src@0";

    // vpn
    public static final String VPN_CNA_TAG = "@vpn@cna";
    public static final String VPN_ELE_CNA_TAG = "@vpn@cna";
    public static final String VPN_L2GW_WILD_TAG = "%s@vpn@l2gw@%d";
    public static final String VPN_ELE_L2GW_TAG = "@vpn@l2gw";
    public static final String VPN_VROUTER_WILD_TAG = "%s@vpn@rf@%d";
    public static final String VPN_ELE_VROUTER_TAG = "@vpn@rf";

    // eip
    public static final String EIP_CNA_TAG = "@eip@cna";
    public static final String EIP_ELE_CNA_TAG = "@eip@cna";
    public static final String EIP_SNAT_WILD_TAG = "%s@eip@snat@%d";
    public static final String EIP_ELE_SNAT_TAG = "@eip@snat";
    public static final String EIP_SNAT_START_TAG = "@eip@snat@0";

}
