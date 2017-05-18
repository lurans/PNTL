# -*- coding: utf-8 -*-
import os
import re
import sys

import blackhole_common as util
from blackhole_common import DEV_T_TAP, DEV_T_QVM, DEV_T_OVS, DEV_T_DVR, DEV_T_FIP, DEV_T_V_BOND

# usage
# ./flow_fip_cna.py task_id, vm_ip, FIP, vm_port, dvr_port, dvr_id, remote_ip

# configuration
TIME_OUT_TCPDUMP = 5  # second
INTERVAL_DUMP_FLOW = 7  # second

task_id = sys.argv[1]
vm_fixed_ip = sys.argv[2]
floating_ip = sys.argv[3]
local_eip = sys.argv[4]
port_id_vm = sys.argv[5]
port_id_dvr = sys.argv[6]
dvr_id = sys.argv[7]
remote_ip = sys.argv[8]
gateway_mac = sys.argv[9]
fip_ns_id = sys.argv[10]
fip_port_id = sys.argv[11]
fg_mac = sys.argv[12]
isSnat = sys.argv[13].lower() == 'true'

ns_dvr = 'qrouter-%s' % dvr_id
ns_fip = 'fip-%s' % fip_ns_id
dev_tap = 'tap%s' % port_id_vm[0:11]
dev_qvm = 'qvm%s' % port_id_vm[0:11]
dev_ovs_bef_dvr = 'ovs'
dev_ovs_aft_dvr = 'ovs'
dev_dvr_2_ovs = 'qr-%s' % port_id_dvr[0:11]
dev_dvr_2_fip = 'rfp-%s' % dvr_id[0:10]
dev_fip_2_dvr = 'fpr-%s' % dvr_id[0:10]
dev_fip_port = 'fg-%s' % fip_port_id[0:11]
dev_ovs_bef_fip = 'ovs'
dev_ovs_aft_fip = 'ovs'
result_file_name = 'fip_%s.log' % task_id


def get_dev_bond():
    """
    Get bond port's name.
    :return: Bond port's name.
    """
    cmd = 'ip addr | grep v_'
    lines = util.send_shell_cmd(cmd)
    # get v_bond by name
    print('get v_bond by name')
    v_bond = None
    for line in lines:
        rst = re.match(r'[0-9]+: (v_[a-zA-Z0-9@]+):.+', line)
        if rst is not None:
            v_bond = rst.group(1)
    if v_bond is None:
        raise Exception('can not find v_bond port by name')

    # get bond name from v_bond name
    bond = None
    if v_bond.find("@") != -1:
        bond = v_bond.split("@")[1]
    else:
        bond = v_bond.strip("v_")
    print('bond port maybe: %s' % bond)

    # find bond port according v_bond port
    cmd = 'ip addr | awk {\'print $2\'} | grep -w %s' % bond
    lines = util.send_shell_cmd(cmd)
    if len(lines) == 0:
        raise Exception('can not find %s port' % bond)
    print('bond port: %s' % bond)
    return bond

def get_mac_from_tcmpdump_flow(flow_info):
    rst = re.match(r'([0-9:]{8}\.[0-9]+) ([0-9a-zA-Z:]{17}) >.+',flow_info)
    if not rst:
        return None
    mac = rst.group(2)
    if mac is None:
        return None
    return mac

def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip=None, dst_ip=None, ns=None, timeout=TIME_OUT_TCPDUMP):
    pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, to=timeout)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)
    return pkg, rst

def get_flow_by_tcpdump_not_write(dev_type, dev_name, pro, src_ip=None, dst_ip=None, ns=None, timeout=TIME_OUT_TCPDUMP):
    pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, to=timeout)
    return pkg, rst



def get_flow_by_flowtable(src_ip=None, src_mac=None, dst_ip=None, dst_mac=None, in_action=False):
    pkg, rst = util.calc_flow_table(src_ip, src_mac, dst_ip, dst_mac, in_action, INTERVAL_DUMP_FLOW)
    return pkg


def main():
    util.del_if_exist(result_file_name)
    try:
        """ direction: output"""
        util.flag_direction_output(result_file_name)
        # 1. tap
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_TAP, dev_tap, None, src_ip=vm_fixed_ip, dst_ip=remote_ip)
        # 2. qvm
        qvmPkg = util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_QVM, dev_qvm, None, src_ip=vm_fixed_ip, dst_ip=remote_ip)
        # 3. br-int-bef-dvr
        pkg = get_flow_by_flowtable(src_ip=vm_fixed_ip, dst_mac=gateway_mac)
        if qvmPkg and pkg:
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_dvr, pkg, src_ip=vm_fixed_ip,
                                      dst_mac=gateway_mac)
        else:
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_dvr, 0, src_ip=vm_fixed_ip,
                                      dst_mac=gateway_mac)

        # 4. dvr-in
        get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_ovs, None, src_ip=vm_fixed_ip, dst_ip=remote_ip, ns=ns_dvr,
                            timeout=INTERVAL_DUMP_FLOW)
        # 5. dvr-out
        get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_fip, None, src_ip=vm_fixed_ip, dst_ip=remote_ip, ns=ns_dvr,
                            timeout=INTERVAL_DUMP_FLOW)
        # 6. fip-in
        get_flow_by_tcpdump(DEV_T_FIP, dev_fip_2_dvr, None, src_ip=vm_fixed_ip, dst_ip=remote_ip, ns=ns_fip,
                            timeout=INTERVAL_DUMP_FLOW)
        # 7. fip-out
        get_flow_by_tcpdump(DEV_T_FIP, dev_fip_port, None, src_ip=vm_fixed_ip, dst_ip=remote_ip, ns=ns_fip,
                            timeout=INTERVAL_DUMP_FLOW)

         # 9. bond
        dev_bond = get_dev_bond()
        if isSnat:
            bond_pkg, rst = get_flow_by_tcpdump_not_write(DEV_T_V_BOND, dev_bond, None, src_ip=local_eip, dst_ip=remote_ip, timeout=TIME_OUT_TCPDUMP)
        else:
            bond_pkg, rst = get_flow_by_tcpdump_not_write(DEV_T_V_BOND, dev_bond, None, src_ip=floating_ip, dst_ip=remote_ip, timeout=TIME_OUT_TCPDUMP)
        # 8. br-int-aft-fip
        pkg = get_flow_by_flowtable(src_ip=vm_fixed_ip, dst_ip=remote_ip, in_action=True, src_mac=fg_mac)
        if not pkg:
            pkg = bond_pkg
        util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_aft_fip, pkg, src_ip=vm_fixed_ip,
                                  dst_ip=remote_ip, src_mac=fg_mac)
        # 9. bond
        util.generate_path_record(result_file_name, DEV_T_V_BOND, dev_bond, bond_pkg, src_ip=floating_ip,
                                  dst_ip=remote_ip)

        """ direction: input"""
        util.flag_direction_input(result_file_name)
        # 1. bond
        bond_pkg, rst = get_flow_by_tcpdump(DEV_T_V_BOND, dev_bond, None, src_ip=remote_ip, dst_ip=floating_ip,
                                       timeout=TIME_OUT_TCPDUMP)
        # 2. br-int-bef-fip
        if rst:
            mac = get_mac_from_tcmpdump_flow(rst)
            pkg = get_flow_by_flowtable(src_mac=mac, dst_mac=fg_mac)

            if bond_pkg and pkg:
                util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_fip, pkg, src_ip=remote_ip,
                                      dst_ip=floating_ip, dst_mac=fg_mac)
            else:
                util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_fip, 0, src_ip=remote_ip,
                                      dst_ip=floating_ip, dst_mac=fg_mac)
        else:
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_fip, 0, src_ip=remote_ip,
                                      dst_ip=floating_ip, dst_mac=fg_mac)
         # 3. fip-in
        get_flow_by_tcpdump(DEV_T_FIP, dev_fip_port, None, src_ip=remote_ip, dst_ip=floating_ip, ns=ns_fip,
                            timeout=INTERVAL_DUMP_FLOW)
        # 4. fip-out
        get_flow_by_tcpdump(DEV_T_FIP, dev_fip_2_dvr, None, src_ip=remote_ip, dst_ip=floating_ip, ns=ns_fip,
                            timeout=INTERVAL_DUMP_FLOW)
        # 5. dvr-in
        get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_fip, None, src_ip=remote_ip, dst_ip=floating_ip, ns=ns_dvr,
                            timeout=INTERVAL_DUMP_FLOW)
        # 6. dvr-out
        get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_ovs, None, src_ip=remote_ip, dst_ip=floating_ip, ns=ns_dvr,
                            timeout=INTERVAL_DUMP_FLOW)

        pkg_vm_port = util.get_flow_by_tcpdump_from_vm_port_for_ovs(
                task_id, dev_qvm, None, src_ip=remote_ip, dst_ip=vm_fixed_ip)

        if pkg_vm_port:
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_aft_dvr, 1, src_ip=remote_ip,
                                  dst_ip=vm_fixed_ip, src_mac=gateway_mac)
        else:
            pkg = get_flow_by_flowtable(src_ip=remote_ip, dst_ip=vm_fixed_ip, src_mac=gateway_mac)
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_aft_dvr, pkg, src_ip=remote_ip,
                                  dst_ip=vm_fixed_ip, src_mac=gateway_mac)
        util.generate_path_record(result_file_name, DEV_T_QVM, dev_qvm, pkg_vm_port, src_ip=remote_ip,
                                  dst_ip=vm_fixed_ip)

        # 9. tap
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_TAP, dev_tap, None, src_ip=remote_ip, dst_ip=vm_fixed_ip)
        # end
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
