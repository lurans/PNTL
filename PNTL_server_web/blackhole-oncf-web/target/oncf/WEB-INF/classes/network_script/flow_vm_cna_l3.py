# -*- coding: utf-8 -*-
import os
import re
import sys

import blackhole_common as util
from blackhole_common import DEV_T_TAP, DEV_T_QVM, DEV_T_OVS, DEV_T_V_BOND, DEV_T_DVR, TIME_OUT_TCPDUMP

# usage
# python flow_vm_cna_l3.py task_id, same_host, vm1_ip, vm1_mac, vm2_ip, vm2_mac, vm1_port, dvr_id, dvr_port1, dvr_port2

# configuration
task_id = sys.argv[1]
flag_same_host = sys.argv[2].lower() == 'true'
vm1_ip = sys.argv[3]
vm1_mac = sys.argv[4].lower()
vm2_ip = sys.argv[5]
vm2_mac = sys.argv[6].lower()
port_id_vm1 = sys.argv[7]
dvr_id = sys.argv[8]
port1_id_dvr = sys.argv[9]
port1_id_dvr_mac = sys.argv[10]
port2_id_dvr = sys.argv[11]
port2_id_dvr_mac = sys.argv[12]
cna_vtep_ip = sys.argv[13]
cna2_vtep_ip = sys.argv[14]
in_gateway_vtep_ip = sys.argv[15]
out_gateay_vtep_ip = sys.argv[16]
flag_same_az = sys.argv[17].lower() == 'true'
flag_same_subnet = sys.argv[18].lower() == 'true'

ns_dvr = 'qrouter-%s' % dvr_id
dev_tap = 'tap%s' % port_id_vm1[0:11]
dev_qvm = 'qvm%s' % port_id_vm1[0:11]
dev_ovs = 'ovs'
dev_ovs_bef_dvr = 'ovs'
dev_ovs_aft_dvr = 'ovs'
dev_dvr_2_ovs_1 = 'qr-%s' % port1_id_dvr[0:11]
dev_dvr_2_ovs_2 = 'qr-%s' % port2_id_dvr[0:11]
result_file_name = 'vm_l3_%s.log' % task_id


def get_phy_ip_by_flow_records(records):
    if records is None:
        return None, None

    for record in records:
        rst = re.match(r'.+tunnel.+src=([0-9.]+),dst=([0-9.]+),.+', record)
        if rst is not None:
            src_ip = rst.group(1)
            dst_ip = rst.group(2)
            return src_ip, dst_ip

    return None, None


def get_dev_vbond():
    cmd = 'ip addr | grep v_'
    lines = util.send_shell_cmd(cmd)

    # get v_bond by name
    print('get v_bond by name')
    for line in lines:
        rst = re.match(r'[0-9]+: (v_[a-zA-Z0-9]+)[:@].+', line)
        if rst is not None:
            return rst.group(1)
    raise Exception('can not find v_bond port by name')


def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip, dst_ip, ns=None, timeout=TIME_OUT_TCPDUMP):
    if src_ip is None and dst_ip is None:
        util.generate_path_record(result_file_name, dev_type, dev_name, 0)
    else:
        pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, to=timeout)
        util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def get_flow_by_flowtable(src_ip=None, src_mac=None, dst_ip=None, dst_mac=None):
    pkg, rst = util.calc_flow_table(src_ip, src_mac, dst_ip, dst_mac)
    if not pkg:
        pkg, rst = util.calc_flow_table_for_ovs(src_ip, dst_ip)
    return pkg


def get_flow_by_data(dev_type, dev_name, pkg, src_ip, dst_ip):
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def main():
    util.del_if_exist(result_file_name)
    try:
        """ direction: output"""
        util.flag_direction_output(result_file_name)
        # tap
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_TAP, dev_tap, None, src_ip=vm1_ip, dst_ip=vm2_ip)
        # qvm
        qvm_pkg = util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_QVM, dev_qvm, None, src_ip=vm1_ip, dst_ip=vm2_ip)

        if flag_same_subnet:
            pkg = get_flow_by_flowtable(src_ip=vm1_ip, dst_ip=vm2_ip)
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, src_ip=vm1_ip, dst_ip=vm2_ip)
        else:
            """ New demand. Do not show DVR and OVS before DVR."""
            # ovs-bef-dvr
            pkg = get_flow_by_flowtable(src_mac=vm1_mac, src_ip=vm1_ip, dst_mac=port1_id_dvr_mac)
            if qvm_pkg and pkg:
                util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_dvr, pkg, src_mac=vm1_mac,
                                          src_ip=vm1_ip, dst_mac=port1_id_dvr_mac)
            else:
                util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_bef_dvr, 0, src_mac=vm1_mac,
                                          src_ip=vm1_ip, dst_mac=port1_id_dvr_mac)
            # dvr-in
            get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_ovs_1, None, src_ip=vm1_ip, dst_ip=vm2_ip, ns=ns_dvr)
            # dvr-out
            get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_ovs_2, None, src_ip=vm1_ip, dst_ip=vm2_ip, ns=ns_dvr)
            # ovs-aft-dvr
            pkg = get_flow_by_flowtable(src_mac=port2_id_dvr_mac, src_ip=vm1_ip, dst_ip=vm2_ip, dst_mac=vm2_mac)
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs_aft_dvr, pkg, src_ip=vm1_ip, dst_ip=vm2_ip)
        # v_bond
        dev_vbond = get_dev_vbond()
        if not flag_same_az:
            """ New demand. Use flow table replace v_bond flow info. """
            get_flow_by_tcpdump(DEV_T_V_BOND, dev_vbond, 'udp', src_ip=cna_vtep_ip, dst_ip=out_gateay_vtep_ip,
                                timeout=TIME_OUT_TCPDUMP)
        if flag_same_az and not flag_same_host:
            get_flow_by_tcpdump(DEV_T_V_BOND, dev_vbond, 'udp', src_ip=cna_vtep_ip, dst_ip=cna2_vtep_ip,
                                timeout=TIME_OUT_TCPDUMP)

        """ direction: input"""
        util.flag_direction_input(result_file_name)
        # tap
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_TAP, dev_tap, None, src_ip=vm2_ip, dst_ip=vm1_ip)
        # qvm
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_QVM, dev_qvm, None, src_ip=vm2_ip, dst_ip=vm1_ip)

        # ovs
        pkg = get_flow_by_flowtable(src_ip=vm2_ip, dst_ip=vm1_ip, dst_mac=vm1_mac)
        util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, src_ip=vm2_ip, dst_ip=vm1_ip)

        if not flag_same_az:
            get_flow_by_tcpdump(DEV_T_V_BOND, dev_vbond, 'udp', src_ip=in_gateway_vtep_ip, dst_ip=cna_vtep_ip,
                                timeout=TIME_OUT_TCPDUMP)

        if flag_same_az and not flag_same_host:
            get_flow_by_tcpdump(DEV_T_V_BOND, dev_vbond, 'udp', src_ip=cna2_vtep_ip, dst_ip=cna_vtep_ip,
                                timeout=TIME_OUT_TCPDUMP)
        # end
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
