package com.huawei.blackhole.network.extention.service.conf;

import com.huawei.blackhole.network.api.bean.OncfConfig;
import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.CommonException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.exception.InvalidFormatException;
import com.huawei.blackhole.network.common.exception.InvalidParamException;
import com.huawei.blackhole.network.common.utils.ExceptionUtil;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.RegexUtil;
import com.huawei.blackhole.network.common.utils.WccCrypter;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.core.bean.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * 处理配置文件和秘钥文件
 */
@Service("oncfConfigService")
public class OncfConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(OncfConfigService.class);

    public Result<OncfConfig> getOncfConfig() {
        Result<OncfConfig> result = new Result<OncfConfig>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) YamlUtil.getConf(Resource.NAME_CONF);
            WccCrypter.decryptMapEntry(data, Config.FSP_KEY_OS_PASSWORD);
            OncfConfig oncfConfig = new OncfConfig();
            oncfConfig.setByMap(data);
            result.setModel(oncfConfig);
        } catch (ConfigLostException e) {
            String errMsg = Resource.NAME_CONF + " not found : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (ClassCastException e) {
            String errMsg = "invalid format: " + Resource.NAME_CONF;
            LOGGER.error(errMsg, e);
            result.addError("", ExceptionUtil.prefix(ExceptionType.CLIENT_ERR) + errMsg);
        } catch (InvalidFormatException e) {
            String errMsg = "invalid format: " + Resource.NAME_CONF;
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (ApplicationException e) {
            String errMsg = "fail to get configuration: " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (CommonException e) {
            String errMsg = "wcc decrypt failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        }
        return result;
    }

    public Result<String> setOncfConfig(OncfConfig oncfConfig) {
        Result<String> result = new Result<String>();
        try {
            validOncfConfig(oncfConfig);
            Map<String, Object> data = oncfConfig.convertToMap();
            setKeyFileName(data);
            WccCrypter.encryptMapEntry(data, Config.FSP_KEY_OS_PASSWORD);
            YamlUtil.setConf(data, Resource.NAME_CONF);
        } catch (ApplicationException | InvalidParamException | ConfigLostException | InvalidFormatException | CommonException e) {
            String errMsg = "set config [" + Resource.NAME_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        }
        return result;
    }

    public Result<String> validKey(Attachment file) {
        Result<String> result = new Result<String>();
        try {
            validAttachment(file);
        } catch (InvalidParamException | InvalidFormatException e) {
            String errMsg = e.toString();
            result.addError(" ", errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Result<String> uploadKey(Attachment file) {
        Result<String> result = new Result<String>();
        try {
            validAttachment(file);
        } catch (InvalidParamException | InvalidFormatException e) {
            String errMsg = e.toString();
            result.addError("", errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }

        String name = file.getDataHandler().getName();
        File destinationFile = new File(getFileName(name));
        try {
            deleteOldKey();
            file.transferTo(destinationFile);
        } catch (IOException e) {
            String errMsg = "file to load key file to server : " + e.getLocalizedMessage();
            result.addError("", ExceptionUtil.prefix(ExceptionType.SERVER_ERR) + errMsg);
            LOGGER.error(errMsg, e);
            return result;
        } catch (ApplicationException | ConfigLostException | InvalidFormatException | CommonException e) {
            String errMsg = "file to load key file to server : " + e.getLocalizedMessage();
            result.addError("", e.prefix() + errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }

        try {
            // 这里直接读取密文，所以不解密直接保存
            Map<String, Object> data = (Map<String, Object>) YamlUtil.getConf(Resource.NAME_CONF);
//            WccCrypter.decryptMapEntry(data, Config.FSP_KEY_OS_PASSWORD);
            data.put("cna_ssh_key", name);
            YamlUtil.setConf(data, Resource.NAME_CONF);
        } catch (ConfigLostException | InvalidFormatException | ApplicationException e) {
            String errMsg = "fail to write key file name to config : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private void deleteOldKey() throws ApplicationException, ConfigLostException, InvalidFormatException, CommonException {
        Map<String, Object> data = (Map<String, Object>) YamlUtil.getConf(Resource.NAME_CONF);
        WccCrypter.decryptMapEntry(data, Config.FSP_KEY_OS_PASSWORD);
        String oldKeyName = (String) data.get(Config.FSP_KEY_CNA_SSH_KEY);
        File oldKeyFile = new File(FileUtil.getResourcePath() + oldKeyName);
        oldKeyFile.delete();
    }

    private void validOncfConfig(OncfConfig oncfConfig) throws ApplicationException {
        if (oncfConfig == null) {
            throw new ApplicationException(ExceptionType.CLIENT_ERR, "no data provided");
        }
        if (!RegexUtil.validAuthUrl(oncfConfig.getOsAuthUrl())) {
            String errMsg = "invalid format of OS_AUTH_URL";
            if (oncfConfig.getOsAuthUrl() != null) {
                errMsg += oncfConfig.getOsAuthUrl();
            }
            throw new ApplicationException(ExceptionType.CLIENT_ERR, errMsg);
        }

    }

    private void setKeyFileName(Map<String, Object> data) throws InvalidParamException, ConfigLostException,
            InvalidFormatException, ApplicationException, CommonException {
        @SuppressWarnings("unchecked")
        Map<String, Object> yml = (Map<String, Object>) YamlUtil.getConf(Resource.NAME_CONF);
//    DO NOT NEED THIS    WccCrypter.decryptMapEntry(yml, Config.FSP_KEY_OS_PASSWORD);
        String cnaSshKey = (String) yml.get("cna_ssh_key");
        if (StringUtils.isEmpty(cnaSshKey)) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "key file name empty");
        }
        data.put("cna_ssh_key", cnaSshKey);
    }

    private void validAttachment(Attachment file) throws InvalidParamException, InvalidFormatException {
        if (file == null) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid request to upload key file");
        }
        String contentDisposition = file.getHeader("Content-Disposition");
        if ((contentDisposition == null) || (contentDisposition.indexOf("filename") == -1)) {
            // 附件是否上传
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "key file required");
        }
        String name = file.getDataHandler().getName();
        if (name.indexOf(".") != -1) {
            String format = name.substring(name.indexOf("."));
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, "invalid format of key file :　" + format);
        }

        File tmpFile = new File(FileUtil.getResourceTmpKeyPath() + name + UUID.randomUUID());
        try {
            file.transferTo(tmpFile);
            if (tmpFile.length() > Constants.KEY_FILE_MAX_SIZE) {
                String errMsg = "too large file [ max size : " + Constants.KEY_FILE_MAX_SIZE + "bytes ]";
                throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
            }
            if (!FileUtil.isTxt(tmpFile)) {
                String errMsg = "invalid format of key file : not a text file";
                throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
            }
        } catch (IOException e) {
            String errMsg = "fail to load key file to server" + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
        } finally {
            tmpFile.delete();
        }

    }

    private String getFileName(String fullname) {// 防止IE和火狐浏览器引起的上传名字问题不一致的问题
        String file = fullname.substring(fullname.lastIndexOf('\\') + 1, fullname.length());
        return FileUtil.getResourcePath() + file;
    }
}
