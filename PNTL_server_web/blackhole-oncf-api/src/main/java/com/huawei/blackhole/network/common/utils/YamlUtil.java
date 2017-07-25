package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.exception.InvalidFormatException;
import org.apache.commons.codec.Charsets;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.util.ArrayList;

public class YamlUtil {

    /**
     * 按照yml格式，获取confFile的配置，以Object返回
     *
     * @param confFile
     * @return
     * @throws ConfigLostException    ：没有文件
     * @throws InvalidFormatException ：文件内容错误，格式不对
     * @throws ApplicationException
     */
    public static Object getConf(String confFile) throws ConfigLostException, InvalidFormatException,
            ApplicationException {
        String path = getPath(confFile);
        InputStream istm = null;

        try {
            istm = new BufferedInputStream(new FileInputStream(path));
            Yaml yaml = new Yaml(new SafeConstructor());
            Object data = yaml.load(istm);
            if (data == null) {
                throw new ApplicationException(ExceptionType.SERVER_ERR, "fail to get configuration");
            }
            return data;
        } catch (FileNotFoundException e) {
            String errMsg = String.format("fail to load configuration file[%s] : %s", confFile, e.getLocalizedMessage());
            throw new ConfigLostException(ExceptionType.SERVER_ERR, errMsg);
        } catch (ScannerException e) {
            String errMsg = String.format("fail to load configuration file[%s] : %s", confFile, e.getLocalizedMessage());
            throw new InvalidFormatException(ExceptionType.SERVER_ERR, errMsg);
        } finally {
            try {
                if (istm != null) {
                    istm.close();
                }
                istm = null;
            } catch (IOException e) {
                istm = null;
            }
        }
    }

    public static void appendConf(Object data, String confFile) throws ApplicationException {
        if(null==data || 0==((ArrayList) data).size()){
            return;
        }
        Writer output = null;
        String path = getPath(confFile);
        try{
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), Charsets.UTF_8));
            Yaml yaml = new Yaml(new SafeConstructor());
            String yamlData = yaml.dumpAsMap(data);
            if (yamlData.startsWith("!!map")){
                yamlData = yamlData.substring(5);
            }
            output.write(yamlData);
            output.flush();
        } catch (IOException e){
            String errMsg = String.format("fail to append configuration file[%s] : %s", confFile, e.getLocalizedMessage());
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        }finally {
            try {
                if (output != null) {
                    output.close();
                }
                output = null;
            } catch (IOException e) {
                output = null;
            }
        }
    }

    public static void setConf(Object data, String confFile) throws ApplicationException {
        String path = getPath(confFile);
        Writer output = null;
        try {
//            output = new FileWriter(path);
            output = new OutputStreamWriter(new FileOutputStream(path), Charsets.UTF_8);
            Yaml yaml = new Yaml(new SafeConstructor());
            String yamlData = yaml.dumpAsMap(data);
            output.write(yamlData);
            output.flush();
        } catch (IOException e) {
            String errMsg = String.format("fail to set configuration file[%s] : %s", confFile, e.getLocalizedMessage());
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
                output = null;
            } catch (IOException e) {
                output = null;
            }
        }
    }

    private static String getPath(String confFile) {
        String directory = FileUtil.getResourcePath();
        final String separator = System.lineSeparator();
        if (separator.equals("\\") && directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        return directory + confFile;
    }

}
