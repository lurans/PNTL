package com.huawei.blackhole.network.ssh;


public class CmdResolve {
    public static void main(String[] arg) {
        String[] args = {"user@xxx.xxx.xxx.xxx:hh", "dd"};
        run(args);
    }

    public static void run(String[] arg) {
        if (arg.length != 2) {
            System.err.println("usage: java ScpFrom user@remotehost:file1 file2");
            System.exit(-1);
        }
        String user = arg[0].substring(0, arg[0].indexOf('@'));
        arg[0] = arg[0].substring(arg[0].indexOf('@') + 1);
        String host = arg[0].substring(0, arg[0].indexOf(':'));
        String rfile = arg[0].substring(arg[0].indexOf(':') + 1);
        String lfile = arg[1];

        String info = String.format("user:%s, host:%s, rfile:%s, lfile:%s", user, host, rfile, lfile);
        System.out.println(info);

    }
}
