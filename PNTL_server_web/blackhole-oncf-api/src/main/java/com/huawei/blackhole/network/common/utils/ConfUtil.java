package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.ConfigConflictException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration manager.
 */
public class ConfUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfUtil.class);

    private static ConfUtil instance = new ConfUtil(Resource.NAME_CONF);

    private Map<String, Object> confMap = null;

    private ConfUtil(final String confFile) {
        initConf(confFile);
    }

    @SuppressWarnings("unchecked")
    private void initConf(String confFile) {
        InputStream reader = null;
        try {
            String directory = ConfUtil.class.getResource("/").getPath().replace("%20", " ");
//            LOGGER.info("configuration directory: {}", directory);
            String path = null;
            final String separator = System.getProperty("file.separator");
            if (separator.equals("\\")) {
                if (directory.startsWith("/")) {
                    directory = directory.substring(1);
                }
                path = directory + confFile;
            } else {
                path = directory + confFile;
            }
//            LOGGER.info("configuration path: {}", path);

            Yaml yaml = new Yaml(new SafeConstructor());
            reader = new BufferedInputStream(new FileInputStream(path));
            confMap = (Map<String, Object>) yaml.load(reader);
            if (confMap == null) {
                confMap = new HashMap<String, Object>(1);
            }
            WccCrypter.decryptMapEntry(confMap, Config.FSP_ADMIN_PASSWORD);
        } catch (Exception e) {
            LOGGER.error("loading configuration file[{}] fail:", confFile, e);
            try {
                if (null != reader) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException ex) {
                LOGGER.error("close file fail:", ex);
            }
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                LOGGER.error("close file fail:", e);
            }
        }
    }

    public void reinit() {
        confMap = null;
        initConf(Resource.NAME_CONF);
    }

    /**
     * Get an instance of configuration util. You can get configuration by this
     * instance.
     *
     * @return Instance of this class.
     */
    public static ConfUtil getInstance() {
        return instance;
    }

    public boolean isConfExist(String key) {
        if (!confMap.containsKey(key)) {
            return false;
        }
        if (confMap.get(key) == null) {
            return false;
        }
        return true;
    }

    /**
     * Register configuration.
     *
     * @param key   Configuration name.
     * @param value Configuration value.
     * @throws ConfigConflictException If 'key' has been exist.
     */
    public void regConfAsString(String key, String value) throws ConfigConflictException {
        if (confMap.containsKey(key)) {
            String errMsg = String.format("config for key %s already exits", key);
            throw new ConfigConflictException(ExceptionType.SERVER_ERR, errMsg);
        }
        confMap.put(key, value);
    }

    /**
     * Register configuration.
     *
     * @param key    Configuration name.
     * @param object Configuration value.
     * @throws ConfigConflictException If 'key' has been exist.
     */
    public void regConfAsMap(String key, Map<?, ?> object) throws ConfigConflictException {
        if (confMap.containsKey(key)) {
            String errMsg = String.format("config for key %s already exits", key);
            throw new ConfigConflictException(ExceptionType.SERVER_ERR, errMsg);
        }
        confMap.put(key, object);
    }

    /**
     * Get configuration value of a specify key.
     *
     * @param key Configuration name.
     * @return Configuration value.
     * @throws ConfigLostException
     */
    public String getConfAsString(String key) throws ConfigLostException {
        Object rst = confMap.get(key);
        if (rst != null) {
            return rst.toString();
        }
        String errMsg = "can not find value by key : " + key;
        throw new ConfigLostException(ExceptionType.SERVER_ERR, errMsg);
    }

    /**
     * Get configuration value of a specify key. If key does't exist, return
     * default value.
     *
     * @param key      Configuration name.
     * @param defValue Default value.
     * @return Configuration value.
     */
    @Deprecated
    public String getConfAsString(String key, String defValue) {
        Object rst = confMap.get(key);
        if (rst != null) {
            return rst.toString();
        }
        return defValue;
    }

    /**
     * Get configuration value of a specify key.
     *
     * @param key Configuration name.
     * @return Configuration value.
     * @throws ConfigLostException
     */
    @SuppressWarnings("unchecked")
    public List<String> getConfAsStringList(String key) throws ConfigLostException {
        List<String> rst = (List<String>) confMap.get(key);
        if (rst != null) {
            return rst;
        }
        String errMsg = "can not find value by key : " + key;
        throw new ConfigLostException(ExceptionType.SERVER_ERR, errMsg);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getConfAsMap(String key) throws ConfigLostException {
        Map<String, String> rst = (Map<String, String>) confMap.get(key);
        if (rst != null) {
            return rst;
        }
        String errMsg = "can not find value by key : " + key;
        throw new ConfigLostException(ExceptionType.SERVER_ERR, errMsg);
    }
}
