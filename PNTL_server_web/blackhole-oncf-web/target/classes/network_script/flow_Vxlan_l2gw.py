# -*- coding: utf-8 -*-
import os
import re
import sys

import blackhole_common as util
from blackhole_common import DEV_T_V_BOND, DEV_T_OVS, DEV_T_EHT

# usage
# ./flow_vpn_cascade.py task_id, vm_ip

# configuration
TIME_OUT_TCPDUMP = 6  # second
INTERVAL_DUMP_FLOW = 7  # second

task_id = sys.argv[1]
vm_ip = sys.argv[2]
vm_mac = sys.argv[3]
remote_vm_ip = sys.argv[4]
remote_vm_mac = sys.argv[5]
node_flag_str = sys.argv[6]
src_vm_vtep_ip = sys.argv[7]
dst_vm_vtep_ip = sys.argv[8]
l2_gateway_vtep_ip = sys.argv[9]
tun_id_in = sys.argv[10]
tun_id_out = sys.argv[11]


dev_tunnel = 'tunnel_bearing'
dev_ovs = 'ovs'
eth = 'eth'

result_file_name = 'Vxlan_l2gw_%s.log' % task_id


def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip=None, dst_ip=None, ns=None):
    pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, None, None, None, to=TIME_OUT_TCPDUMP)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def main():
    if not node_flag_str:
        print 'Please input exec node info!'
        return

    util.del_if_exist(result_file_name)
    try:
        if 'output' == node_flag_str:
            """ direction: output"""
            util.flag_direction_output(result_file_name)
            # 1. tunnel_bearing
            # l2_vtep_ip, cna_vtep_ip = get_ip_by_port_options(remote_vm_mac)
            l2_vtep_ip = l2_gateway_vtep_ip
            if l2_vtep_ip and src_vm_vtep_ip:
                ret = util.get_flow_from_eth_port \
                    (task_id=task_id, src_ip=src_vm_vtep_ip, dst_ip=l2_vtep_ip, file_name=result_file_name)
                if not ret:
                    return
            else:
                error_info = 'Can not find vtep ip(src_ip=%s,dst_ip=%s)' % (src_vm_vtep_ip, l2_vtep_ip)
                print error_info
                util.flag_process_over(result_file_name, error_info)
                return
            pkg = util.calc_flow_table_for_l2_gateway(src_ip=vm_ip, dst_ip=remote_vm_ip)
            tmp_src_ip = vm_ip
            tmp_dst_ip = remote_vm_ip
            if 0 == pkg:
                pkg = util.calc_flow_table_for_l2_gateway(src_ip=src_vm_vtep_ip, dst_ip=l2_vtep_ip, tun_id=tun_id_out)
                tmp_src_ip = src_vm_vtep_ip
                tmp_dst_ip = l2_vtep_ip
            # 2. ovs
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, src_ip=tmp_src_ip, dst_ip=tmp_dst_ip)

        if 'input' == node_flag_str:
            # direction: input
            util.flag_direction_input(result_file_name)
            # l2_vtep_ip, cna_vtep_ip = get_ip_by_port_options(vm_mac)
            l2_vtep_ip = l2_gateway_vtep_ip
            # 1. tunnel_bearing
            if dst_vm_vtep_ip and l2_vtep_ip:
                ret = util.get_flow_from_eth_port \
                    (task_id=task_id, src_ip=dst_vm_vtep_ip, dst_ip=l2_vtep_ip, file_name=result_file_name)
                if not ret:
                    return
            else:
                error_info = 'Can not find vtep ip(src_ip=%s,dst_ip=%s)' % (dst_vm_vtep_ip, l2_vtep_ip)
                print error_info
                util.flag_process_over(result_file_name, error_info)
                return
            tmp_src_ip = remote_vm_ip
            tmp_dst_ip = l2_vtep_ip
            pkg = util.calc_flow_table_for_l2_gateway(src_ip=remote_vm_ip, dst_ip=l2_vtep_ip, tun_id=None)
            if 0 == pkg:
                pkg = util.calc_flow_table_for_l2_gateway(src_ip=dst_vm_vtep_ip, dst_ip=l2_vtep_ip, tun_id=tun_id_in)
                tmp_src_ip = dst_vm_vtep_ip
                tmp_dst_ip = l2_vtep_ip
            # 2. ovs
            util.generate_path_record(result_file_name, DEV_T_OVS, dev_ovs, pkg, dst_ip=tmp_dst_ip, src_ip=tmp_src_ip)

                # end
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
