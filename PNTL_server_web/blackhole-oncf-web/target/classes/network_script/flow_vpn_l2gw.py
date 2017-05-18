# -*- coding: utf-8 -*-
import os
import re
import sys

import blackhole_common as util
from blackhole_common import DEV_T_V_BOND, DEV_T_OVS, DEV_T_EHT

# usage
# ./flow_vpn_cascade.py task_id, vm_ip, sg_mac

# configuration
TIME_OUT_TCPDUMP = 6  # second
INTERVAL_DUMP_FLOW = 7  # second

task_id = sys.argv[1]
remote_ip = sys.argv[2]
vm_mac = sys.argv[3]
sg_mac = sys.argv[4]
vroute_vtep_ip = sys.argv[5]
l2_gateway_vtep_ip = sys.argv[6]
cna_vtep_ip = sys.argv[7]
tun_id = sys.argv[8]

dev_tunnel = 'tunnel_bearing'
dev_ovs = 'ovs'

result_file_name = 'vpn_l2gw_%s.log' % task_id


def get_ip_by_flow_records(records):
    if records is None:
        return None, None, None, None

    for record in records:
        rst = re.match(r'.+tunnel.+src=([0-9.]+),dst=([0-9.]+),.+actions.+src=([0-9.]+),dst=([0-9.]+),.+', record)
        if rst is not None:
            src_ip = rst.group(1)
            dst_ip = rst.group(2)
            src_ip2 = rst.group(3)
            dst_ip2 = rst.group(4)
            return src_ip, dst_ip, src_ip2, dst_ip2

    return None, None, None, None


def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip=None, dst_ip=None, ns=None):
    pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, None, None, None, to=TIME_OUT_TCPDUMP)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def get_flow_from_eth_port(src_ip=None, dst_ip=None):
    eth_list = util.get_eth_port_from_br_bond()
    if not eth_list:
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
        """ direction: input"""
        util.flag_direction_input(result_file_name)
        # 1. tunnel_bearing
        # l2_vtep_ip, cna_vtep_ip = get_ip_by_port_options()
        if l2_gateway_vtep_ip:
            ret = util.get_flow_from_eth_port(task_id, src_ip=vroute_vtep_ip, dst_ip=l2_gateway_vtep_ip,
                                              file_name=result_file_name)
            if not ret:
                return
        else:
            error_info = 'Can not find vtep ip(src_ip=%s,dst_ip=%s)' % (vroute_vtep_ip, l2_gateway_vtep_ip)
            print error_info
            util.flag_process_over(result_file_name, error_info)
            return

        pkg = util.calc_flow_table_for_l2_gateway(src_mac=sg_mac, src_ip=remote_ip, to=INTERVAL_DUMP_FLOW)
        tmp_src_ip = remote_ip
        tmp_dst_ip = None
        if 0 == pkg:
            pkg = util.calc_flow_table_for_l2_gateway(src_ip=vroute_vtep_ip, dst_ip=l2_gateway_vtep_ip,to=INTERVAL_DUMP_FLOW, tun_id = tun_id)
            tmp_src_ip = vroute_vtep_ip
            tmp_dst_ip = l2_gateway_vtep_ip
        # 2. ovs
        util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, dst_ip=tmp_dst_ip, src_ip=tmp_src_ip)
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
