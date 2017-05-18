package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.codec.Charsets;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JschUtil {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JschUtil.class);

    public static List<String> submitCommand(String host, AuthUser authUser, String cmd) throws ApplicationException {
        LOGGER.info(String.format("submit cmd [host:%s, cmd:%s]", host, cmd));
        String keyPath = authUser.getKey();
        String ip = host;
        String user = authUser.getUser();
        String command = cmd;

        List<String> resultLine = new ArrayList<>();

        Session session = null;
        ChannelExec openChannel = null;
        InputStream in = null;
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, ip);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            jsch.addIdentity(keyPath);
            session.connect();

            openChannel = (ChannelExec) session.openChannel("exec");
            openChannel.setCommand(command);
            openChannel.connect();
            in = openChannel.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
            String buf;
            while ((buf = reader.readLine()) != null) {
                resultLine.add(buf);
            }
        } catch (Exception e) {
            LOGGER.error("fail to submit command to " + host + ":" + cmd, e);
            throw new ApplicationException(ExceptionType.SERVER_ERR, "fail to submit command: " + authUser + ":" + cmd);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    LOGGER.warn("mark unnecessary exception", e);
                }
            }
            if (openChannel != null && !openChannel.isClosed()) {
                openChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return resultLine;
    }
}
