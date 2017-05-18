package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.codec.Charsets;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ScpUtil {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ScpUtil.class);
    private static final String FAIL_GENERAL_TOPIC = "fail to transfer file";

    public static void scpTo(String host, AuthUser authUser, String srcFile, String dstFile) throws ApplicationException {
        FileInputStream fis = null;
        OutputStream out = null;
        try {
            String lfile = srcFile;
            String user = authUser.getUser();
            String rfile = dstFile;

            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            jsch.addIdentity(authUser.getKey());
            session.connect();

//            boolean ptimestamp = false;
//
//            // exec 'scp -t rfile' remotely
//            String command = "scp " + (ptimestamp ? "-p" : "") + " -t " + rfile;
            String command = "scp -t " + rfile;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                LOGGER.error(FAIL_GENERAL_TOPIC);
                throw new ApplicationException(ExceptionType.SERVER_ERR, FAIL_GENERAL_TOPIC);
            }

            File _lfile = new File(lfile);

            // send "C0644 filesize filename", where filename should not include '/'
            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (lfile.lastIndexOf('/') > 0) {
                command += lfile.substring(lfile.lastIndexOf('/') + 1);
            } else {
                command += lfile;
            }
            command += "\n";
            out.write(command.getBytes(Charsets.UTF_8));
            out.flush();
            if (checkAck(in) != 0) {
                LOGGER.error(FAIL_GENERAL_TOPIC);
                throw new ApplicationException(ExceptionType.SERVER_ERR, FAIL_GENERAL_TOPIC);
            }

            // send a content of lfile
            fis = new FileInputStream(lfile);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();
            if (checkAck(in) != 0) {
                System.exit(0);
            }
            out.close();

            channel.disconnect();
            session.disconnect();

            return;
        } catch (Exception e) {
            LOGGER.error(FAIL_GENERAL_TOPIC, e);
            throw new ApplicationException(ExceptionType.SERVER_ERR, FAIL_GENERAL_TOPIC);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                LOGGER.warn("close fail failed", e);
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                LOGGER.warn("close fail failed", e);
            }
        }
    }

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,1 for error,2 for fatal error,-1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
