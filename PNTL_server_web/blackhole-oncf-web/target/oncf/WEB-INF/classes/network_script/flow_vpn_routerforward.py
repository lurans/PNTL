# -*- coding: utf-8 -*-
import os
import re
import sys
import blackhole_common as util
from blackhole_common import DEV_T_OVS, DEV_T_EHT

# usage
# ./flow_vpn_routerforward.py task_id, vm_ip, vm_mac, sg_mac

# configuration
TIME_OUT_TCPDUMP = 6  # second

task_id = sys.argv[1]
vm_fixed_ip = sys.argv[2]
remote_ip = sys.argv[3]
sg_mg = sys.argv[4]
cna_vtep_ip = sys.argv[5]
vrouter_vtep_ip = sys.argv[6]
tun_id = sys.argv[7]

dev_eth = 'eth'
dev_bond = 'trunk0'
dev_ovs = 'ovs'

result_file_name = 'vpn_routerforward_%s.log' % task_id


def get_ip_by_flow_records(records):
    if records is None:
        return None, None

    for record in records:
        rst = re.match(r'.+tunnel.+src=([0-9.]+),dst=([0-9.]+),.+', record)
        if rst is not None:
            src_ip = rst.group(1)
            dst_ip = rst.group(2)
            return src_ip, dst_ip

    return None, None


def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip=None, dst_ip=None, ns=None, src_mac=None, dst_mac=None):
    pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, None, src_mac, dst_mac, to=TIME_OUT_TCPDUMP)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def get_flow_from_eth_port(src_ip=None, dst_ip=None):
    eth_list, cmd = util.get_eth_port_from_br_bond()
    if not eth_list:
        error_info = "can not find br-bond(cmd=%s)" % cmd
        util.flag_process_over(result_file_name, error_info)
        return
    for eth in eth_list:
        pkg, rst = util.tcp_dump(task_id, eth, None, src_ip, dst_ip, None, None, None, None, to=TIME_OUT_TCPDUMP)
        if pkg:
            util.generate_path_record(result_file_name, DEV_T_EHT, eth, pkg, src_ip=src_ip, dst_ip=dst_ip)
            return
    util.generate_path_record(result_file_name, DEV_T_EHT, DEV_T_EHT, 0, src_ip=src_ip, dst_ip=dst_ip)
    return


def main():
    util.del_if_exist(result_file_name)
    try:
        """ direction: output"""
        util.flag_direction_output(result_file_name)
        # 1.eth
        # get_flow_from_eth_port(src_ip = vm_fixed_ip, dst_ip = remote_ip)
        ret = util.get_flow_from_eth_port(task_id, src_ip=cna_vtep_ip, dst_ip=vrouter_vtep_ip,
                                          file_name=result_file_name)
        if not ret:
            return
        # 2.OVS
        pkg, flow_table_records = util.calc_flow_table(src_ip=vm_fixed_ip, dst_mac=sg_mg)
        tmp_src_ip = vm_fixed_ip
        tmp_dst_ip = None
        if 0 == pkg:
            pkg, flow_table_records = util.calc_flow_table(src_ip=cna_vtep_ip, dst_ip=vrouter_vtep_ip, tun_id=tun_id)
            tmp_src_ip = cna_vtep_ip
            tmp_dst_ip = vrouter_vtep_ip
        util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, src_ip=tmp_src_ip, dst_ip=tmp_dst_ip)
        # eth
        ret = util.get_flow_from_eth_port(task_id, src_ip=vm_fixed_ip, dst_ip=remote_ip, file_name=result_file_name)
        if not ret:
            return

        """ direction: input"""
        util.flag_direction_input(result_file_name)
        # 1. eth
        # get_flow_from_eth_port(dst_ip = vm_fixed_ip,src_ip = remote_ip)
        ret = util.get_flow_from_eth_port(task_id, dst_ip=vm_fixed_ip, src_ip=remote_ip, file_name=result_file_name)
        if not ret:
            return
        # 2. ovs
        pkg, flow_table_records = util.calc_flow_table(dst_ip=vm_fixed_ip, src_mac=sg_mg)
        util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, dst_ip=vm_fixed_ip)
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
