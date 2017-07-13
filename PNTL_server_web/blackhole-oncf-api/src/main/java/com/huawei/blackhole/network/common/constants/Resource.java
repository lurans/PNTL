package com.huawei.blackhole.network.common.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Resource file or path should be here.
 */
public class Resource {
    public static final String TARGET_PATH = "~/blackhole/chkflow";

    public static final String SCRIPT_PATH = Resource.class.getResource("/").getPath().replace("%20", " ")
            + "network_script" + "/";

    public static final String NAME_FLOW_VM_CNA_L3 = "flow_vm_cna_l3.py";
    public static final String FLOW_VM_CNA_L3 = SCRIPT_PATH + NAME_FLOW_VM_CNA_L3;
    public static final String TARGET_VM_CNA_L3 = TARGET_PATH + "/" + NAME_FLOW_VM_CNA_L3;

    public static final String NAME_FLOW_VM_CNA_L2 = "flow_vm_cna_l2.py";
    public static final String FLOW_VM_CNA_L2 = SCRIPT_PATH + NAME_FLOW_VM_CNA_L2;
    public static final String TARGET_VM_CNA_L2 = TARGET_PATH + "/" + NAME_FLOW_VM_CNA_L2;

    public static final String NAME_FLOW_COMMON = "blackhole_common.py";
    public static final String FLOW_COMMON = SCRIPT_PATH + NAME_FLOW_COMMON;
    public static final String TARGET_COMMON = TARGET_PATH + "/" + NAME_FLOW_COMMON;

    public static final String NAME_FLOW_EIP_CNA = "flow_fip_cna.py";
    public static final String FLOW_EIP_CNA = SCRIPT_PATH + NAME_FLOW_EIP_CNA;
    public static final String TARGET_EIP_CNA = TARGET_PATH + "/" + NAME_FLOW_EIP_CNA;

    public static final String NAME_FLOW_EIP_SNAT = "flow_fip_snat.py";
    public static final String FLOW_EIP_SNAT = SCRIPT_PATH + NAME_FLOW_EIP_SNAT;
    public static final String TARGET_EIP_SNAT = TARGET_PATH + "/" + NAME_FLOW_EIP_SNAT;

    public static final String NAME_FLOW_VPN_CNA = "flow_vpn_cna.py";
    public static final String FLOW_VPN_CNA = SCRIPT_PATH + NAME_FLOW_VPN_CNA;
    public static final String TARGET_VPN_CNA = TARGET_PATH + "/" + NAME_FLOW_VPN_CNA;

    public static final String NAME_FLOW_VPN_L2GW = "flow_vpn_l2gw.py";
    public static final String FLOW_VPN_L2GW = SCRIPT_PATH + NAME_FLOW_VPN_L2GW;
    public static final String TARGET_VPN_L2GW = TARGET_PATH + "/" + NAME_FLOW_VPN_L2GW;

    public static final String NAME_FLOW_VPN_ROUTERFORWARDER = "flow_vpn_routerforward.py";
    public static final String FLOW_VPN_ROUTERFORWARDER = SCRIPT_PATH + NAME_FLOW_VPN_ROUTERFORWARDER;
    public static final String TARGET_VPN_ROUTERFORWARDER = TARGET_PATH + "/" + NAME_FLOW_VPN_ROUTERFORWARDER;

    public static final String NAME_FLOW_VPN_CNA_L2 = "flow_vpn_cna_l2.py";
    public static final String FLOW_VPN_CNA_L2 = SCRIPT_PATH + "flow_vpn_cna_l2.py";
    public static final String TARGET_VPN_CNA_L2 = TARGET_PATH + "/" + "flow_vpn_cna_l2.py";

    public static final String NAME_FLOW_VXLAN_L2GW = "flow_Vxlan_l2gw.py";
    public static final String FLOW_VXLAN_L2GW = SCRIPT_PATH + NAME_FLOW_VXLAN_L2GW;
    public static final String TARGET_VXLAN_L2GW = TARGET_PATH + "/" + NAME_FLOW_VXLAN_L2GW;

    public static final String TCPDUMP_TOOL = Resource.class.getClassLoader().getResource("tcpdump").getFile();

    public static final String NAME_CONF = "config.yml";
    public static final String ROLE_CONF = "role.yml";
}
