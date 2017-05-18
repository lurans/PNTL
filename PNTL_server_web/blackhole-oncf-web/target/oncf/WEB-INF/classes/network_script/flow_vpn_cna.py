# -*- coding: utf-8 -*-
import os
import re
import sys

import blackhole_common as util
from blackhole_common import DEV_T_TAP, DEV_T_QVM, DEV_T_OVS, DEV_T_DVR, DEV_T_V_BOND

# usage
# ./flow_vpn_cna.py task_id, vm_ip, vm_port, dvr_id, dvr_port, sg_mac, remote_ip


# configuration
TIME_OUT_TCPDUMP = 5  # second
INTERVAL_DUMP_FLOW = 7  # second

task_id = sys.argv[1]
vm_fixed_ip = sys.argv[2]
port_id_vm = sys.argv[3]  # 被级联层port_id
dvr_id = sys.argv[4]
port_id_dvr = sys.argv[5]
dvr_port_mac = sys.argv[6]
sg_mac = sys.argv[7]
remote_ip = sys.argv[8]
cna_vtep_ip = sys.argv[9]
vrouter_vtep_ip = sys.argv[10]
l2_gateway_vtep_ip = sys.argv[11]

dev_tap = 'tap%s' % port_id_vm[0:11]
dev_qvm = 'qvm%s' % port_id_vm[0:11]
ns_dvr = 'qrouter-%s' % dvr_id
dev_dvr_2_ovs = 'qr-%s' % port_id_dvr[0:11]
dev_ovs = 'ovs'

result_file_name = 'vpn_cna_%s.log' % task_id


def get_dev_veth():
    """
    Get v_eth port's name.
    :return: v_eth port's name.
    """
    cmd = 'ifconfig|grep v_bond'
    lines = util.send_shell_cmd(cmd)

    if not lines:
        raise Exception('can not find v_bond port.')
    for line in lines:
        rst = re.match(r'v_(bond[0-9]+) .+', line)
        if rst is not None:
            print('v_bond port info: %s' % line)
            bond = rst.group(1)
            return bond

    raise Exception('can not find v_eth port.')


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


def get_flow_by_tcpdump(dev_type, dev_name, pro, src_ip=None, dst_ip=None, src_mac=None, dst_mac=None, ns=None,
                        timeout=TIME_OUT_TCPDUMP):
    pkg, rst = util.tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, None, None, None, to=timeout)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)


def get_flow_by_flowtable(dev_type, dev_name, src_ip=None, src_mac=None, dst_ip=None, dst_mac=None, timeout=INTERVAL_DUMP_FLOW):
    pkg, rst = util.calc_flow_table(src_ip, src_mac, dst_ip, dst_mac, timeout)
    util.generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip, src_mac, dst_ip, dst_mac)
    return pkg, rst


def main():
    util.del_if_exist(result_file_name)
    try:
        """ direction: output"""
        util.flag_direction_output(result_file_name)
        # 1. tap
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_TAP, dev_tap, None, src_ip=vm_fixed_ip, dst_ip=remote_ip)
        # 2. qvm
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_QVM, dev_qvm, None, src_ip=vm_fixed_ip, dst_ip=remote_ip)
        # 3. ovs : qvm -> qr
        # ip, qrdev_mac = util.get_gateway_info(task_id, ns_dvr, dev_dvr_2_ovs[0:9])
        # 4. qr : dvr-in        
        get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_ovs, None, src_ip=vm_fixed_ip, dst_ip=remote_ip, dst_mac=dvr_port_mac,
                            ns=ns_dvr, timeout=INTERVAL_DUMP_FLOW)
        # 5. dvr-out 
        get_flow_by_tcpdump(DEV_T_DVR, dev_dvr_2_ovs, None, src_ip=vm_fixed_ip, dst_ip=remote_ip, src_mac=dvr_port_mac,
                            ns=ns_dvr, timeout=INTERVAL_DUMP_FLOW)
        # 6. ovs : qr->veth
        get_flow_by_flowtable(DEV_T_OVS, dev_ovs, src_ip=vm_fixed_ip, dst_ip=remote_ip, src_mac=dvr_port_mac,
                              timeout=INTERVAL_DUMP_FLOW)
        # 7. v_eth1
        dev_veth = get_dev_veth()
        get_flow_by_tcpdump(DEV_T_V_BOND, dev_veth, None, src_ip=cna_vtep_ip, dst_ip=vrouter_vtep_ip,
                            timeout=TIME_OUT_TCPDUMP)
        """ direction: input"""
        util.flag_direction_input(result_file_name)
        # 1. tap
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_TAP, dev_tap, None, src_ip=remote_ip, dst_ip=vm_fixed_ip)
        # 2. qvm
        util.get_flow_by_tcpdump_from_vm_port(
                task_id, result_file_name, DEV_T_QVM, dev_qvm, None, src_ip=remote_ip, dst_ip=vm_fixed_ip)
        # 3.ovs
        get_flow_by_flowtable(DEV_T_OVS, dev_ovs, src_mac=sg_mac, dst_ip=vm_fixed_ip)
        # 4.v_eth1
        dev_veth = get_dev_veth()
        get_flow_by_tcpdump(DEV_T_V_BOND, dev_veth, None, src_ip=l2_gateway_vtep_ip, dst_ip=cna_vtep_ip,
                            timeout=TIME_OUT_TCPDUMP)
        util.flag_process_over(result_file_name)
    except Exception, e:
        util.flag_process_over(result_file_name, str(e))


""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
main()
