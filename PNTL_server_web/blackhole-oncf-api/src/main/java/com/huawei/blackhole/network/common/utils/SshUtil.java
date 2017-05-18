//package com.huawei.blackhole.network.common.utils;
//
//import ch.ethz.ssh2.*;
//
//import com.huawei.blackhole.network.common.constants.ExceptionType;
//import com.huawei.blackhole.network.common.exception.ApplicationException;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SshUtil {
//    private static final Logger LOG = LoggerFactory.getLogger(SshUtil.class);
//
//    private static final Object lock = new Object();
//
//    public static final String AUTH_TYPE_PASSWORD = "password";
//    public static final String AUTH_TYPE_KEY = "publickey";
//    public static final String AUTH_TYPE_INTERACTIVE = "keyboard-interactive";
//
//    public static List<String> exec(Connection connection, String cmds) throws ApplicationException {
//        LOG.info("execute:\"{}\" on " + connection.getHostname(), cmds);
//        InputStream in = null;
//        List<String> result = null;
//        try {
//            Session session = connection.openSession(); // 打开一个会话
//            session.execCommand(cmds);
//            int conditions = session.waitForCondition(ChannelCondition.STDOUT_DATA | ChannelCondition.STDERR_DATA
//                    | ChannelCondition.EOF, 1000 * 600);
//            if ((conditions & ChannelCondition.TIMEOUT) != 0) {
//                String errMsg = String.format("execute %s timeout break", cmds);
//                throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, errMsg);
//            }
//            in = session.getStdout();
//            result = processStdout(in);
//            session.close();
//        } catch (IOException e) {
//            throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, e.getMessage());
//        }
//        return result;
//    }
//
////    public static void execute(Connection connection, String cmds) throws ApplicationException {
////        LOG.info("execute:\"{}\" on " + connection.getHostname(), cmds);
////        try {
////            Session session = connection.openSession(); // 打开一个会话
////            session.execCommand(cmds);
////            session.close();
////        } catch (IOException e) {
////            throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, e.getMessage());
////        }
////    }
//
//    public static void scp(Connection connection, String srcFile, String dstFileName, String dstDirectory, String mode)
//            throws ApplicationException {
//        LOG.info("scp [local file:{}] --> [host:{}, directory:{}, file:{} ]", srcFile, connection.getHostname(),
//                dstDirectory, dstFileName);
//        try {
//            SCPClient scpClient = connection.createSCPClient();
//            synchronized (lock) {
//                scpClient.put(srcFile, dstFileName, dstDirectory, mode);
////                scpClient.put(srcFile, new File(srcFile).length(), dstDirectory, mode);
//            }
//        } catch (IOException e) {
//            String filename = srcFile;
//            if (srcFile != null) {
//                int idx = srcFile.lastIndexOf("/");
//                if (idx != -1 && idx != srcFile.length() - 1) {
//                    filename = srcFile.substring(idx + 1);
//                }
//            }
//            String errorMsg = String.format("fail to scp file %s to %s : %s", filename, connection.getHostname(),
//                    e.getLocalizedMessage());
//            LOG.error(errorMsg, e);
//            throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, errorMsg);
//        }
//
//    }
//
//    @SuppressWarnings({ "resource" })
//    public static List<String> processStdout(InputStream in) throws IOException {
//        final InputStream is = new StreamGobbler(in);
//        final BufferedReader brs = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//        List<String> lines = new ArrayList<String>();
//        while (true) {
//            final String line = brs.readLine();
//            if (line == null) {
//                break;
//            }
//            lines.add(line);
//        }
//        return lines;
//    }
//
//    public static List<String> submitCommand(Connection connection, final String command) throws ApplicationException {
//        LOG.info("execute:\"{}\" on " + connection.getHostname(), command);
//        return exec(connection, command);
//    }
//
//    public static Connection getConnection(String ip, String user, String pass, File fileKey)
//            throws ApplicationException {
//        Connection conn = new Connection(ip);
//        String authType = null;
//        try {
//            ServerHostKeyVerifier verifier = null;//new ServerHostKeyVerifierImpl();
//            LOG.info("---- login ----: " + ip);
//            conn.connect(verifier, 30 * 1000, 0);
//            LOG.info("---- connected ----: " + ip);
//
//            if (pass != null) {
//                if (conn.isAuthMethodAvailable(user, SshUtil.AUTH_TYPE_PASSWORD)) {
//                    // auth with password
//                    authType = SshUtil.AUTH_TYPE_PASSWORD;
//                    if (conn.authenticateWithPassword(user, pass)) {
//                        return conn;
//                    }
//                } else if (conn.isAuthMethodAvailable(user, SshUtil.AUTH_TYPE_INTERACTIVE)) {
//                    // auth with keyboard-interactive
//                    authType = SshUtil.AUTH_TYPE_INTERACTIVE;
//                    final String finalSshPass = pass;
//                    if (conn.authenticateWithKeyboardInteractive(user,
//                            (name, instruction, numPrompts, prompt, echo) -> {
//                                String[] reply = new String[numPrompts];
//                                for (int i = 0; i < reply.length; i++) {
//                                    reply[i] = finalSshPass;
//                                }
//                                return reply;
//                            })) {
//                        return conn;
//                    }
//                }
//            }
//            // auth with key
//            if (fileKey != null) {
//                if (conn.isAuthMethodAvailable(user, SshUtil.AUTH_TYPE_KEY)) {
//                    authType = SshUtil.AUTH_TYPE_KEY;
//                    LOG.info("use ssh key to login " + ip);
//                    if (conn.authenticateWithPublicKey(user, fileKey, null)) {
//                        return conn;
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            StackTraceElement[] errlist = e.getStackTrace();
//            for(StackTraceElement elel : errlist) {
//                LOG.error(elel.getClassName() + ":" +elel.getFileName()+ ":" +elel.getMethodName()+ ":"+elel.getLineNumber());
//            }
//            conn.close();
//            throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, e.getLocalizedMessage());
//        } catch (Throwable e) {
//            e.printStackTrace();
//            StackTraceElement[] errlist = e.getStackTrace();
//            for(StackTraceElement elel : errlist) {
//                LOG.error(elel.getClassName() + ":" +elel.getFileName()+ ":" +elel.getMethodName()+ ":"+elel.getLineNumber(), elel);
//            }
//        } finally {
//            LOG.info("connection finally : " + ip);
//        }
//        
//
//        // if login successfully, will not reach here. must be auth fail.
//        try {
//            String[] methods = conn.getRemainingAuthMethods(user);
//            LOG.error("host[{}] support auth methods: {}", ip, methods);
//        } catch (IOException e) {
//            LOG.error("get auth methods fail: {}", e.getLocalizedMessage());
//        }
//        String errMsg = String.format("login host[%s] fail: %s", ip, authType);
//        conn.close();
//        throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, errMsg);
//    }
//    
//    private static class ServerHostKeyVerifierImpl implements ServerHostKeyVerifier {
//        @Override
//        public boolean verifyServerHostKey(String host, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
//            return true;
//        }
//    }
//
//}
