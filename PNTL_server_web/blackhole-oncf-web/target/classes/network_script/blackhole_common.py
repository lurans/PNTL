# -*- coding: utf-8 -*-
import codecs
import threading
import os
import re
import subprocess as shell
import time

# configuration
TIME_OUT_TCPDUMP = 7  # second
INTERVAL_DUMP_FLOW = 5  # second
INTERVAL_DUMP_FLOW_L2_GW = 2

THREAD_NUM = 20
TIME_FORMAT = '%Y-%m-%d %X'
INVALID_PARAM = 'None'

CMD_OVS_DPCTL_DUMP_FLOWS = 'sudo ovs-dpctl dump-flows'
CMD_OVS_GET_BR_BOND_PORT = 'sudo ovs-vsctl list-ports br-bond'
CMD_OVS_GET_ETH_PORTS = 'sudo ovs-vsctl list-br'

DEV_T_TAP = 'TAPport'
DEV_T_QVM = 'VMport'
DEV_T_OVS = 'OVS'
DEV_T_DVR = 'DVR'
DEV_T_FIP = 'FIP'
DEV_T_BOND = 'Bond'
DEV_T_V_BOND = 'vBond'
DEV_T_EHT = 'ETH'

thread_num = threading.BoundedSemaphore(THREAD_NUM)


def check_output(*popenargs, **kwargs):
    if 'stdout' in kwargs:
        raise ValueError('stdout argument not allowed, it will be overridden.')
    process = shell.Popen(stdout=shell.PIPE, *popenargs, **kwargs)
    output, unused_err = process.communicate()
    retcode = process.poll()
    if retcode:
        cmd = kwargs.get("args")
        if cmd is None:
            cmd = popenargs[0]
        raise shell.CalledProcessError(retcode, cmd)
    return output


def send_shell_cmd(cmd):
    """
    :param cmd: String. Command to be executed.
    :return: String. Result of the command.
    :raise Exception: When command executed fail. Note: Time out is not fail.
    :raise CalledProcessError: Command like 'grep' when has no result will throw this exception.
    """
    # This is dependent on python2.7 or above. For compatibility, we'd better not use this.
    # return shell.check_output(cmd, shell=True).splitlines()
    return check_output(cmd, shell=True).splitlines()


def shell_cmd_prefix(cmd, root=None, timeout=None, flag=False):
    """
    Support add 'sudo' or 'timeout' ahead of shell command.
    :param cmd: Original command.
    :param root: Bool. True will add 'sudo'. False will not.
    :param timeout: Time out value.
    :return: Shell command after factory.
    """
    if timeout is not None and not flag:
        cmd = 'timeout %d %s' % (timeout, cmd)
    if root is True:
        cmd = 'sudo %s' % cmd
    return cmd


def shell_cmd_postfix(cmd, redirect_file=None):
    """
    Support appending redirect for shell command.
    :param cmd: Original command.
    :param redirect_file: File path to be redirected. None means do not redirect.
    :return: Shell command after factory.
    """
    if redirect_file is not None:
        cmd += " > " + redirect_file
    return cmd


def tcp_dump(task_id, dev, pro, src_ip=None, dst_ip=None, ns=None, l2=False, src_mac=None, dst_mac=None, to=TIME_OUT_TCPDUMP):
    """
    Use tcpdump tool to get flow info. src_ip and dst_ip can't all be None.
    :param task_id:
    :param dev:
    :param pro: Protocol. Could be 'icmp', 'tcp', 'udp' or None.
    :param src_ip:
    :param dst_ip:
    :param ns: Namespace if you need.
    :param l2: Only arp counted if True. Otherwise not count arp.
    :param src_mac:
    :param dst_mac:
    :param to: Timeout value in second.
    :return: Package number captured in a certain time.
    """
    # check parameter
    if dev is None:
        raise Exception('device is indispensable.')
    if src_ip is None and dst_ip is None:
        raise Exception('source ip and destination ip can not all be None.')

    # prepare command
    cmd = '/home/oncf/tcpdump -s 60 -c 1 -nne -i %s ' % (dev)
    if l2:
        exp = 'arp'
    else:
        exp = 'not arp'
    if pro is not None:
        exp += ' and %s' % pro
    if src_ip is not None:
        exp += ' and src host %s' % src_ip
    if dst_ip is not None:
        exp += ' and dst host %s' % dst_ip
    if src_mac is not None:
        exp += ' and ether src %s' % src_mac
    if dst_mac is not None:
        exp += ' and ether dst %s' % dst_mac

    cmd += exp
    flag = False
    if ns is not None:
        cmd = 'ip netns exec %s timeout %s %s' % (ns, to, cmd)
        flag = True

    tmp_file_name = 'tmp_%s.log' % task_id
    cmd = shell_cmd_postfix(cmd, tmp_file_name)
    cmd = shell_cmd_prefix(cmd, True, to, flag)

    # execute command
    try:
        send_shell_cmd(cmd)
    except shell.CalledProcessError:
        pass

    # calc
    lines = open(tmp_file_name).readlines()
    pkg = len(lines)
    if pkg == 0 or (pkg == 1 and lines[0] == '\n'):
        pkg = 0
        print('no pkg found: %s' % cmd)

    del_if_exist(tmp_file_name)
    return pkg, lines[0]


def tcp_dump_for_ARP(task_id, dev, pro, src_ip=None, dst_ip=None, ns=None, l2=False, src_mac=None, dst_mac=None,
                     to=TIME_OUT_TCPDUMP):
    """
    Use tcpdump tool to get flow info. src_ip and dst_ip can't all be None.
    :param task_id:
    :param dev:
    :param pro: Protocol. Could be 'icmp', 'tcp', 'udp' or None.
    :param src_ip:
    :param dst_ip:
    :param ns: Namespace if you need.
    :param l2: Only arp counted if True. Otherwise not count arp.
    :param src_mac:
    :param dst_mac:
    :param to: Timeout value in second.
    :return: Package number captured in a certain time.
    """
    # check parameter
    if dev is None:
        raise Exception('device is indispensable.')
    if src_ip is None and dst_ip is None:
        raise Exception('source ip and destination ip can not all be None.')

    # prepare command
    cmd = '/home/oncf/tcpdump -s 60 -c 3 -nne -i %s ' % (dev)
    if l2:
        exp = 'arp'
    else:
        exp = 'not arp'
    if pro is not None:
        exp += ' and %s' % pro
    if src_ip is not None:
        exp += ' and src host %s' % src_ip
    if dst_ip is not None:
        exp += ' and dst host %s' % dst_ip
    if src_mac is not None:
        exp += ' and ether src %s' % src_mac
    if dst_mac is not None:
        exp += ' and ether dst %s' % dst_mac

    cmd += exp
    if ns is not None:
        cmd = 'ip netns exec %s %s' % (ns, cmd)

    tmp_file_name = 'tmp_%s.log' % task_id
    cmd = shell_cmd_postfix(cmd, tmp_file_name)
    cmd = shell_cmd_prefix(cmd, True, to)

    # execute command
    try:
        send_shell_cmd(cmd)
    except shell.CalledProcessError:
        pass

    # calc
    lines = open(tmp_file_name).readlines()
    pkg = len(lines)
    if pkg == 0 or (pkg < 3):
        pkg = 0
        print('no pkg found: %s' % cmd)

    del_if_exist(tmp_file_name)
    return pkg


def tcpdum_thread(retPkg, task_id, dev, pro, src_ip=None, dst_ip=None, \
                  ns=None, l2=False, src_mac=None, dst_mac=None, to=TIME_OUT_TCPDUMP):
    """
    Use tcpdump tool to get flow info. src_ip and dst_ip can't all be None.
    :param task_id:
    :param dev:
    :param pro: Protocol. Could be 'icmp', 'tcp', 'udp' or None.
    :param src_ip:
    :param dst_ip:
    :param ns: Namespace if you need.
    :param l2: Only arp counted if True. Otherwise not count arp.
    :param src_mac:
    :param dst_mac:
    :param to: Timeout value in second.
    :return: Package number captured in a certain time.
    """
    # check parameter
    if dev is None:
        raise Exception('device is indispensable.')
    if src_ip is None and dst_ip is None:
        raise Exception('source ip and destination ip can not all be None.')

    # prepare command
    cmd = '/home/oncf/tcpdump -s 60 -c 1 -nne -i %s ' % (dev)
    if l2:
        exp = 'arp'
    else:
        exp = 'not arp'
    if pro is not None:
        exp += ' and %s' % pro
    if src_ip is not None:
        exp += ' and src host %s' % src_ip
    if dst_ip is not None:
        exp += ' and dst host %s' % dst_ip
    if src_mac is not None:
        exp += ' and ether src %s' % src_mac
    if dst_mac is not None:
        exp += ' and ether dst %s' % dst_mac

    cmd += exp
    if ns is not None:
        cmd = 'ip netns exec %s %s' % (ns, cmd)

    tmp_file_name = 'tmp_%s.log' % dev
    cmd = shell_cmd_postfix(cmd, tmp_file_name)
    cmd = shell_cmd_prefix(cmd, True, to)

    # execute command
    try:
        send_shell_cmd(cmd)
    except shell.CalledProcessError:
        pass

    # calc
    lines = open(tmp_file_name).readlines()
    pkg = len(lines)
    if pkg == 0 or (pkg == 1 and lines[0] == '\n'):
        pkg = 0
        print('no pkg found: %s' % cmd)

    del_if_exist(tmp_file_name)
    retPkg[dev] = pkg


def cur_flow_table(src_ip=None, src_mac=None, dst_ip=None, dst_mac=None, in_action=False, tun_id=None):
    """
    Use OVS dump flow get package number in current time.
    :param src_ip:
    :param src_mac:
    :param dst_ip:
    :param dst_mac:
    :param in_action: Bool. True will only count records satisfy filter in 'actions' field.
    :return: Package number in current time, and matched flow table records.
    """
    # prepare command
    cmd = CMD_OVS_DPCTL_DUMP_FLOWS
    if src_mac is not None:
        cmd += '|grep src=%s' % src_mac
    if dst_mac is not None:
        cmd += '|grep dst=%s' % dst_mac
    if src_ip is not None:
        cmd += '|grep src=%s' % src_ip
    if dst_ip is not None:
        cmd += '|grep dst=%s' % dst_ip
    if tun_id is not None:
        cmd += '|grep tun_id=%s' % tun_id

    cmd += '|grep -v arp|grep -v used:never |grep -v drop'

    # execute command
    try:
        lines = send_shell_cmd(cmd)
    except shell.CalledProcessError:
        print('can not find matched flow record.')
        return 0, None

    # filter the result
    records = lines
    # calc
    pkg = 0
    for record in records:
        rst = re.match(r'.+packets:([0-9]+).+', record)
        if rst is None:
            raise Exception('can not find packets')
        pkg += int(rst.group(1))
    print('current package num: %d' % pkg)
    return pkg, records


def cur_flow_table_for_arp_ovs(src_ip=None, dst_ip=None):
    """
    Use OVS dump flow get package number in current time.
    :param src_ip:
    :param src_mac:
    :param dst_ip:
    :param dst_mac:
    :param in_action: Bool. True will only count records satisfy filter in 'actions' field.
    :return: Package number in current time, and matched flow table records.
    """
    # prepare command
    cmd = CMD_OVS_DPCTL_DUMP_FLOWS
    if src_ip is not None:
        cmd += '|grep sip=%s' % src_ip
    if dst_ip is not None:
        cmd += '|grep tip=%s' % dst_ip

    cmd += '|grep arp|grep -v used:never |grep -v drop'

    # execute command
    try:
        lines = send_shell_cmd(cmd)
    except shell.CalledProcessError:
        print('can not find matched flow record.')
        return 0, None

    # filter the result
    records = lines
    # calc
    pkg = 0
    for record in records:
        rst = re.match(r'.+packets:([0-9]+).+', record)
        if rst is None:
            raise Exception('can not find packets')
        pkg += int(rst.group(1))
    print('current package num: %d' % pkg)
    return pkg, records


def calc_flow_table(src_ip=None, src_mac=None, dst_ip=None, dst_mac=None, in_action=False, to=INTERVAL_DUMP_FLOW, tun_id = None):
    """
    Use OVS ovs-dpctl dump-flows get package number in a certain time.
    :param src_ip:
    :param src_mac:
    :param dst_ip:
    :param dst_mac:
    :param in_action: Bool. True will only count records satisfy filter in 'actions' field.
    :param to: Timeout value in second.
    :return: Package number in a certain time, and matched flow table records.
    """
    # check parameter
    if src_ip is None and dst_ip is None and src_mac is None and dst_mac is None:
        raise Exception('at')
    # dump 1st time
    pkg_pre, rst_pre = cur_flow_table(src_ip, src_mac, dst_ip, dst_mac, in_action, tun_id)
    # sleep for awhile
    time.sleep(to)
    # dump 2nd time
    pkg_post, rst_post = cur_flow_table(src_ip, src_mac, dst_ip, dst_mac, in_action, tun_id)

    # if no new packet in, flow table will be ageing, then pkg_post may be zero.
    pkg_num = pkg_post - pkg_pre
    if 0 != pkg_num:
        pkg_num = 1
    return pkg_num, rst_pre or rst_post


def calc_flow_table_for_l2_gateway(src_ip=None, src_mac=None, dst_ip=None, dst_mac=None, in_action=False, to=2, tun_id = None):
    """
    Use OVS ovs-dpctl dump-flows get package number in a certain time.
    :param src_ip:
    :param src_mac:
    :param dst_ip:
    :param dst_mac:
    :param in_action: Bool. True will only count records satisfy filter in 'actions' field.
    :param to: Timeout value in second.
    :return: Package number in a certain time, and matched flow table records.
    """
    # check parameter
    pkg_list = []
    if src_ip is None and dst_ip is None and src_mac is None and dst_mac is None:
        raise Exception('at')
    # dump 1st time
    pkg_pre, rst_pre = cur_flow_table(src_ip, src_mac, dst_ip, dst_mac, in_action, tun_id)
    pkg_list.append(pkg_pre)
    # sleep for awhile
    time.sleep(to)
    # dump 2nd time
    pkg_mid, rst_mid = cur_flow_table(src_ip, src_mac, dst_ip, dst_mac, in_action, tun_id)
    pkg_list.append(pkg_mid)
    time.sleep(to)
    pkg_post, rst_post = cur_flow_table(src_ip, src_mac, dst_ip, dst_mac, in_action, tun_id)
    pkg_list.append(pkg_post)

    # if no new packet in, flow table will be ageing, then pkg_post may be zero.
    tmp = pkg_list[0]
    pkg_num = 0
    for node in pkg_list:
        if tmp != node:
            pkg_num = 1

    return pkg_num


def calc_flow_table_for_ovs(src_ip=None, dst_ip=None, to=2):
    """
    Use OVS ovs-dpctl dump-flows get package number in a certain time.
    :param src_ip:
    :param src_mac:
    :param dst_ip:
    :param dst_mac:
    :param in_action: Bool. True will only count records satisfy filter in 'actions' field.
    :param to: Timeout value in second.
    :return: Package number in a certain time, and matched flow table records.
    """
    # check parameter
    pkg_list = []
    if src_ip is None and dst_ip is None:
        raise Exception('at')
    # dump 1st time
    pkg_pre, rst_pre = cur_flow_table_for_arp_ovs(src_ip, dst_ip)
    pkg_list.append(pkg_pre)
    # sleep for awhile
    time.sleep(to)
    # dump 2nd time
    pkg_mid, rst_mid = cur_flow_table_for_arp_ovs(src_ip, dst_ip)
    pkg_list.append(pkg_mid)
    time.sleep(to)
    pkg_post, rst_post = cur_flow_table_for_arp_ovs(src_ip, dst_ip)
    pkg_list.append(pkg_post)

    # if no new packet in, flow table will be ageing, then pkg_post may be zero.
    tmp = pkg_list[0]
    pkg_num = 0
    for node in pkg_list:
        if tmp != node:
            pkg_num = 1

    return pkg_num, rst_post


def get_current_time():
    return time.strftime(TIME_FORMAT, time.localtime())


def generate_path_record(file_name, dev_t, dev_name, pkg_sum, src_ip=None, src_mac=None, dst_ip=None, dst_mac=None,
                         timestamp=get_current_time()):
    """
    Record flow info to specified file.
    :param file_name
    :param dev_t:
    :param dev_name:
    :param pkg_sum:
    :param src_ip:
    :param src_mac:
    :param dst_ip:
    :param dst_mac:
    :param timestamp:
    :return: None
    """
    record = '%s %s:%s %d' % (timestamp, dev_t, dev_name, pkg_sum)
    if src_ip is not None:
        record += ' s_ip:%s' % src_ip
    if dst_ip is not None:
        record += ' d_ip:%s' % dst_ip
    if src_mac is not None:
        record += ' s_mac:%s' % src_mac
    if dst_mac is not None:
        record += ' d_mac:%s' % dst_mac
    record += '\n'
    p_file = codecs.open(file_name, 'a+', 'utf-8')
    p_file.write(record)
    p_file.close()


def del_if_exist(file_name):
    if os.path.exists(file_name):
        os.remove(file_name)


def flag_direction_input(file_name):
    p_file = codecs.open(file_name, 'a+', 'utf-8')
    p_file.write('INPUT:\n')
    p_file.close()


def flag_direction_output(file_name):
    p_file = codecs.open(file_name, 'a+', 'utf-8')
    p_file.write('OUTPUT:\n')
    p_file.close()


def flag_process_over(file_name, err_msg=None):
    p_file = codecs.open(file_name, 'a+', 'utf-8')
    if err_msg is not None:
        p_file.truncate()
        p_file.write('ERR:%s\n' % err_msg)
    p_file.write('END')
    p_file.close()
    cmd = 'chmod 600 %s' % file_name
    send_shell_cmd(cmd)


def get_eth_port_from_br_bond():
    filer_conti = '| grep eth |grep -v veth'
    cmd = CMD_OVS_GET_BR_BOND_PORT + filer_conti
    try:
        eth_list = send_shell_cmd(cmd)
    except shell.CalledProcessError:
        print('can not find br-bond')
        return None, cmd
    return eth_list, cmd


def get_eth_port_from_snat():
    filer_conti = '| grep br-eth'
    eth_list = []
    cmd = CMD_OVS_GET_ETH_PORTS + filer_conti
    try:
        tmp_eth_list = send_shell_cmd(cmd)
    except shell.CalledProcessError:
        print('can not find br-bond')
        return None, cmd
    if tmp_eth_list:
        for eth in tmp_eth_list:
            eth_list.append(eth.split('-')[1])
    return eth_list, cmd


def get_eth_port_from_config():
    filer_conti = 'cat /etc/neutron/l3_nat_agent01.ini |grep -w phy_nic_list'
    cmd = filer_conti
    try:
        eth_list = send_shell_cmd(cmd)
    except shell.CalledProcessError:
        print('can not find br-bond')
        return None
    if not eth_list:
        return None
    ret = eth_list[0].split('=')[1].strip().split(',')
    return ret


def get_vtep_ip_from_config(file_path):
    file_list = []
    VTEP_IP = 'virtual_tunneling_ip'
    file = open(file_path, 'r')
    if not file:
        print 'open the file fail!(%s)' % file_path
    for line in file:
        if VTEP_IP in line:
            vtep_ip = line.split('=')[1].strip()
    return vtep_ip


def get_l2_gateway_vtep_ip_from_config(file_path):
    vtep_ip = None
    VTEP_IP = 'vtep_ip'
    file = open(file_path, 'r')
    if not file:
        print 'open the file fail!(%s)' % file_path
    for line in file:
        if VTEP_IP in line:
            vtep_ip = line.split('=')[1].strip()
    return vtep_ip


def get_flow_from_eth_port(task_id=None, src_ip=None, dst_ip=None, file_name=None, flag=None):
    eth_list = []
    cmd = ''
    if not flag:
        eth_list, cmd = get_eth_port_from_br_bond()
    else:
        eth_list, cmd = get_eth_port_from_snat()
    pkgs = {}
    threads = []
    if not eth_list:
        error_info = 'can not find br-bond(cmd=%s)' % cmd
        print error_info
        flag_process_over(file_name, error_info)
        return False
    for eth in eth_list:
        cur_th = threading.Thread(target=tcpdum_thread, args=
        (pkgs, task_id, eth, None, src_ip, dst_ip, None, None, None, None, TIME_OUT_TCPDUMP), )
        thread_num.acquire()
        cur_th.start()
        threads.append(cur_th)

    for th in threads:
        th.join()
    if pkgs:
        for key, value in pkgs.items():
            if value:
                generate_path_record(file_name, DEV_T_EHT, key, value, src_ip=src_ip, dst_ip=dst_ip)
                return True

    generate_path_record(file_name, DEV_T_EHT, DEV_T_EHT, 0, src_ip=src_ip, dst_ip=dst_ip)
    return True


def get_flow_by_tcpdump_from_vm_port(task_id, result_file_name, dev_type, dev_name, pro, src_ip=None, dst_ip=None,
                                     ns=None):
    if src_ip is None and dst_ip is None:
        generate_path_record(result_file_name, dev_type, dev_name, 0)
    pkg, rst= tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, to=TIME_OUT_TCPDUMP)
    if not pkg:
        pkg = tcp_dump_for_ARP(task_id, dev_name, pro, src_ip, ns=ns, l2=True)
    generate_path_record(result_file_name, dev_type, dev_name, pkg, src_ip=src_ip, dst_ip=dst_ip)
    return pkg


def get_flow_by_tcpdump_from_vm_port_for_ovs(task_id, dev_name, pro, src_ip=None, dst_ip=None,
                                     ns=None):
    if src_ip is None and dst_ip is None:
        return 0
    pkg, rst= tcp_dump(task_id, dev_name, pro, src_ip, dst_ip, ns, to=TIME_OUT_TCPDUMP)
    if not pkg:
        pkg = tcp_dump_for_ARP(task_id, dev_name, pro, src_ip, ns=ns, l2=True)
    return pkg
