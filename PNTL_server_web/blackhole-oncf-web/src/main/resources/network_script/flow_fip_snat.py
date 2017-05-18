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
INTERVAL_DUMP_FLOW = 8  # second

task_id = sys.argv[1]
e_ip = sys.argv[2]
f_ip = sys.argv[3]
remote_ip = sys.argv[4]

dev_tunnel = 'tunnel_bearing'
dev_ovs = 'ovs'

result_file_name = 'fip_nat_%s.log' % task_id


def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip=None, dst_ip=None, ns=None):
    pkg, rst= util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, None, None, None, to=TIME_OUT_TCPDUMP)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def get_flow_from_eth_port(src_ip=None, dst_ip=None):
    eth_list = util.get_eth_port_from_config()
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
        if e_ip and remote_ip:
            # get_flow_from_eth_port(src_ip = remote_ip, dst_ip = e_ip)
            ret = util.get_flow_from_eth_port(task_id, src_ip=remote_ip, dst_ip=e_ip, file_name=result_file_name,
                                              flag='snat')
            if not ret:
                return
        else:
            error_info = 'Can not find ip(src_ip=%s,dst_ip=%s)' % (remote_ip, e_ip)
            print error_info
            util.flag_process_over(result_file_name, error_info)
            return

        pkg, flow_table_records = util.calc_flow_table(src_ip=remote_ip, dst_ip=f_ip, to=INTERVAL_DUMP_FLOW)
        # 2. ovs
        util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, dst_ip=remote_ip)
        # 3. tunnel_bearing
        if f_ip and remote_ip:
            # get_flow_from_eth_port(src_ip = remote_ip, dst_ip = f_ip)
            ret = util.get_flow_from_eth_port(task_id, src_ip=remote_ip, dst_ip=f_ip, file_name=result_file_name,
                                              flag='snat')
            if not ret:
                return
        else:
            error_info = 'Can not find ip(src_ip=%s,dst_ip=%s)' % (remote_ip, f_ip)
            print error_info
            util.flag_process_over(result_file_name, error_info)
            return
        # end
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
