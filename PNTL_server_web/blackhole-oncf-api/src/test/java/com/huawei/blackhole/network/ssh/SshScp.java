package com.huawei.blackhole.network.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SshScp {

    private static void ssh() {
        String keyPath = "D:\\projects\\Blackhole_network\\blackhole-oncf-api\\src\\test\\resources\\id_rsa";

        String ip = "xxx.xxx.xxx.xxx";
        String user = "user";
        String command = "ls -a /";
        Session session = null;
        ChannelExec openChannel = null;
        String result = "";
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, ip);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            jsch.addIdentity(keyPath);
            // session.setPassword(psw);
            session.connect();
            openChannel = (ChannelExec) session.openChannel("exec");
            openChannel.setCommand(command);
            int exitStatus = openChannel.getExitStatus();
            openChannel.connect();
            InputStream in = openChannel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String buf;
            while ((buf = reader.readLine()) != null) {
                result += new String(buf.getBytes("gbk"), "UTF-8") + "    <br>\r\n";
            }

        } catch (JSchException | IOException e) {
            result += e.getMessage();
            e.printStackTrace();
        } finally {
            if (openChannel != null && !openChannel.isClosed()) {
                openChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        System.out.println(result);
    }
}
