package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.InvalidParamException;
import org.apache.cxf.helpers.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {

    private final static int BUFFER_SIZE = 1024;

    private static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static String read(String filePath, Map<String, String> content) throws IOException {
        BufferedReader br = null;
        String line = null;
        StringBuffer buf = new StringBuffer();
        Set<String> contentKeySet = content.keySet();
        Pattern pattern = Pattern.compile("\\s*([0-9\\.]+)\\s+(.+)");
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
            while ((line = br.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String lineKey = matcher.group(Constants.NUM_TWO);
                    if (contentKeySet.contains(lineKey)) {
                        continue;
                    }
                }
                buf.append(line);
                buf.append(System.getProperty("line.separator"));
            }
            for (Map.Entry<String, String> entry : content.entrySet()) {
                buf.append(entry.getValue()).append(" ").append(entry.getKey());
                buf.append(System.getProperty("line.separator"));
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
        return buf.toString();
    }

    /**
     * 将内容回写到文件中
     *
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void write(String filePath, String content) throws IOException {
        PrintWriter out = null;

        try {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "GB2312")));
            out.write(content);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 复制文件： src -> dst
     *
     * @param src
     * @param dst
     * @throws IOException
     * @throws InvalidParamException
     */
    public static void copy(File src, File dst) throws IOException, InvalidParamException {
        if (src == null || dst == null) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "write file failed : empty source or destination");
        }
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(src);
            output = new FileOutputStream(dst);
            IOUtils.copy(input, output);
            output.flush();
        } finally {
            if (input != null) {
                input.close();
            }
            input = null;
            if (output != null) {
                output.close();
            }
            output = null;
        }
    }

    public static void write(InputStream src, File dst) throws IOException, InvalidParamException {
        if (src == null || dst == null) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "write file failed : empty source or destination");
        }
        OutputStream out = null;
        try {
            out = new FileOutputStream(dst);
            IOUtils.copy(src, out);
            out.flush();
        } finally {
            if (src != null) {
                src.close();
            }
            src = null;
            if (out != null) {
                out.close();
            }
            out = null;
        }
    }

    /**
     * 获取resources的目录名称，绝对路径
     *
     * @return
     */
    public static String getResourcePath() {
        String directory = ConfUtil.class.getResource("/").getPath().replace("%20", " ");
        return directory;
    }

    public static String getResourceTmpKeyPath() {
        String classes = FileUtil.class.getResource("/").getPath().replace("%20", " ");
        String tmp = classes + "tmp_key/";
        return tmp;
    }

    /**
     * 获取WEB-INF的绝对路径
     *
     * @return
     */
    public static String getWebinfPath() {
        String classes = FileUtil.class.getResource("/").getPath().replace("%20", " ");
        String webinf = classes.substring(0, classes.length() - 8);
        return webinf;
    }

    /**
     * 获取
     *
     * @return
     */
    public static String getResourceBinPath() {
        String classes = FileUtil.class.getResource("/").getPath().replace("%20", " ");
        String bin = classes + "bin/";
        return bin;
    }

    /**
     * 判断一个文件是不是文本文件
     *
     * @param file
     * @return
     */
    public static boolean isTxt(File file) {
        InputStream in = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        try {
            in = new FileInputStream(file);
            while ((read = in.read(buffer)) != -1) {
                for (int i = 0; i < read; i++) {
                    byte b = buffer[i];
                    if (b != '\r' && b != '\n' && b != '\t' && b != '\f' && (b & 0xff) < 32) {
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("fail not found file");
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("fail to close stream when testify text file : ", e);
                }
            }
        }
        return true;
    }
}
