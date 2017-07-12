package com.huawei.blackhole.network.extention.service.conf;

import com.huawei.blackhole.network.api.bean.DelayInfo;
import com.huawei.blackhole.network.api.bean.LossRate;
import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.*;
import com.huawei.blackhole.network.common.utils.ExceptionUtil;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.ResponseUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.service.openstack.Keystone;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;
import org.apache.commons.codec.binary.Base64;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service("pntlConfigService")
public class PntlConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(PntlConfigService.class);
    @javax.annotation.Resource(name = "keystoneService")
    protected Keystone identityWrapperService;

    public Result<PntlConfig> getPntlConfig() {
        Result<PntlConfig> result = new Result<PntlConfig>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);

            PntlConfig pntlConfig = new PntlConfig();
            pntlConfig.setByMap(data);
            result.setModel(pntlConfig);
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
        }
        return result;
    }
    public Result<String> setPntlAkSkConfig(PntlConfig pntlConfig) {
        //Get PntlConfig
        Result<String> result = new Result<String>();
        try{

            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            dataObj.put("ak",pntlConfig.getAk());
            dataObj.put("sk",pntlConfig.getSk());
            pntlConfig.setByMap(dataObj);

            validPntlConfig(pntlConfig);
            pntlConfig.setBasicToken(genBasicToken(pntlConfig.getAk(), pntlConfig.getSk()));
            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, PntlInfo.PNTL_CONF);
        }catch (ApplicationException | InvalidParamException e) {
            String errMsg = "set config [" + PntlInfo.PNTL_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (Exception e){
            result.addError("", "parameter is invalid");
        }

        LOGGER.info("Update pntlConfig success");

        return result;
    }

    public Result<String> setPntlConfig(PntlConfig pntlConfig) {
        Result<String> result = new Result<String>();
        try {
            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            pntlConfig.setAk((String) dataObj.get("ak"));
            pntlConfig.setSk((String) dataObj.get("sk"));

            validPntlConfig(pntlConfig);
            pntlConfig.setBasicToken(genBasicToken(pntlConfig.getAk(), pntlConfig.getSk()));
            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, PntlInfo.PNTL_CONF);
        } catch (ApplicationException | InvalidParamException e) {
            String errMsg = "set config [" + Resource.NAME_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        } catch (Exception e){
            result.addError("", "parameter is invalid");
        }

        LossRate.setLossRateThreshold(Integer.valueOf(pntlConfig.getLossRateThreshold()));
        DelayInfo.setDelayThreshold(Long.valueOf(pntlConfig.getDelayThreshold()));
        LOGGER.info("Update pntlConfig success");

        return result;
    }

    private void validPntlConfig(PntlConfig pntlConfig)
            throws InvalidParamException, Exception {
        if (pntlConfig == null){
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "no data provided");
        }

        try {
            int probe_period = Integer.valueOf(pntlConfig.getProbePeriod());
            if (probe_period < 0 || probe_period > 60) {
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "probe period is invalid");
            }

            int port_count = Integer.valueOf(pntlConfig.getPortCount());
            if (port_count < 1 || port_count > 50){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "port count is invalid");
            }
            int report_period = Integer.valueOf(pntlConfig.getReportPeriod());
            if (report_period < 1 || report_period > 60){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "report period is invalid");
            }
            int pkg_count = Integer.valueOf(pntlConfig.getPkgCount());
            if (pkg_count <= 0){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "package count is invalid");
            }
            int delay_threshold = Integer.valueOf(pntlConfig.getDelayThreshold());
            if (delay_threshold < 0){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "delay threshold is invalid");
            }
            int lossRate_threshold = Integer.valueOf(pntlConfig.getLossRateThreshold());
            if (lossRate_threshold <= 0 || lossRate_threshold > 100){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "lossRate threshold is invalid");
            }
            int dscp = Integer.valueOf(pntlConfig.getDscp());
            if (dscp < 0 || dscp > 63){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "dscp is invalid");
            }
            int lossPkg_timeout = Integer.valueOf(pntlConfig.getLossPkgTimeout());
            if (lossPkg_timeout < 0){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "loss package timeout is invalid");
            }
        } catch (Exception e){
            throw new Exception();
        }
    }

    public String genBasicToken(String ak, String sk){
        String str = ak + ":" + sk;
        byte[] encodeBasic64 = Base64.encodeBase64(str.getBytes());
        return new String(encodeBasic64);
    }

    private void validIpListAttachment(Attachment file, String filename) throws InvalidParamException, InvalidFormatException {
        if (file == null) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid request to upload ipList file");
        }
        String contentDisposition = file.getHeader("Content-Disposition");
        if ((contentDisposition == null) || (contentDisposition.indexOf("filename") == -1)) {
            // 附件是否上传
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "ipList file required");
        }
        String name = file.getDataHandler().getName();
        if (!name.endsWith("yml")) {
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, "invalid format of ipList file, should be " + filename);
        }

        File tmpFile = new File(FileUtil.getResourceIpListPath() + name + UUID.randomUUID());
        try {
            file.transferTo(tmpFile);
            if (tmpFile.length() > Constants.IPLIST_FILE_MAX_SIZE) {
                String errMsg = "too large file [ max size : " + Constants.IPLIST_FILE_MAX_SIZE + "bytes ]";
                throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
            }
            if (!FileUtil.isTxt(tmpFile)) {
                String errMsg = "invalid format of ipList file : not a text file";
                throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
            }
        } catch (IOException e) {
            String errMsg = "fail to load ipList file to server" + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
        } finally {
            tmpFile.delete();
        }
    }

    private String getFileName(String fullname) {// 防止IE和火狐浏览器引起的上传名字问题不一致的问题
        String file = fullname.substring(fullname.lastIndexOf('\\') + 1, fullname.length());
        return FileUtil.getResourcePath() + file;
    }

    @SuppressWarnings("unchecked")
    private void deleteOldIpListFile() throws ApplicationException, ConfigLostException, InvalidFormatException, CommonException {
        new File(FileUtil.getResourcePath() + PntlInfo.PNTL_IPLIST_CONF).delete();
    }

    public Result<String> uploadIpListFile(Attachment file, String othername){
        Result<String> result = new Result<String>();
        String name = othername.isEmpty() ? file.getDataHandler().getName() : othername;
        try {
            validIpListAttachment(file, name);
        } catch (InvalidParamException | InvalidFormatException e) {
            String errMsg = e.toString();
            result.addError("", errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }

        File destinationFile = new File(getFileName(name));
        try {
            if (othername.isEmpty()) {
                deleteOldIpListFile();
            }
            file.transferTo(destinationFile);
        } catch (IOException e) {
            String errMsg = "file to load ipList file to server : " + e.getLocalizedMessage();
            result.addError("", ExceptionUtil.prefix(ExceptionType.SERVER_ERR) + errMsg);
            LOGGER.error(errMsg, e);
            return result;
        } catch (ApplicationException | ConfigLostException | InvalidFormatException | CommonException e) {
            String errMsg = "file to load key file to server : " + e.getLocalizedMessage();
            result.addError("", e.prefix() + errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }
        return result;
    }

    private void validAgentPkgAttachment(Attachment file) throws InvalidParamException, InvalidFormatException {
        if (file == null) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid request to upload agent file");
        }
        String contentDisposition = file.getHeader("Content-Disposition");
        if ((contentDisposition == null) || (contentDisposition.indexOf("filename") == -1)) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "agent file required");
        }

        String name = file.getDataHandler().getName();
        if (!name.equalsIgnoreCase(PntlInfo.AGENT_EULER) && !name.equalsIgnoreCase(PntlInfo.AGENT_SUSE)
                && !name.equalsIgnoreCase(PntlInfo.AGENT_INSTALL_FILENAME)){
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, "invalid format of agent file");
        }

        if (!name.equalsIgnoreCase(PntlInfo.AGENT_EULER) && !name.equalsIgnoreCase(PntlInfo.AGENT_SUSE)
                && !name.equalsIgnoreCase(PntlInfo.AGENT_INSTALL_FILENAME)){
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, "invalid filename:" + name);
        }

        if (new File(file.getDataHandler().getName()).length() > Constants.AGENT_FILE_MAX_SIZE){
            String errMsg = "too large file [ max size : " + Constants.AGENT_FILE_MAX_SIZE + "bytes ]";
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
        }
    }

    public Result<String> uploadAgentPkgFile(Attachment attachment) {
        final String BOUNDARY = "----WebKitFormBoundaryzOYdpFxbuIoovXYf";
        Result<String> result = new Result<String>();
        try {
            validAgentPkgAttachment(attachment);
        } catch (InvalidParamException | InvalidFormatException e) {
            String errMsg = e.toString();
            result.addError("", errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }

        String token = null;
        try {
            token = identityWrapperService.getPntlAccessToken();
        } catch (ClientException e) {
            String errMsg = "get token failed:" + e.getMessage();
            LOGGER.error("", errMsg);
            result.addError("", errMsg);
        }
        String url = PntlInfo.REPOURL + PntlInfo.DFS_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();

        Pntl.setCommonHeaderForAgent(header, token);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        File f = new File(getFileName(attachment.getDataHandler().getName()));

        builder.addBinaryBody("attachment", f);
        builder.addTextBody("type", "0");
        builder.addTextBody("uploader", PntlInfo.OPS_USERNAME);
        builder.addTextBody("space", PntlInfo.PNTL_ROOT_NAME);
        builder.addTextBody("override", "true");
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        HttpEntity entity = builder.build();

        RestResp resp = null;
        try{
            resp = RestClientExt.post(url, entity, header);
            if (resp.getStatusCode().isError()){
                result.addError("", "upload file to dfs failed:" + resp.getStatusCode());
            } else {
               if (resp.getRespBody().getInt("code") == 0){
                   String downloadUrl = resp.getRespBody().getJSONObject("data").getString("downloadUrl");
                   Pntl.setDownloadUrl(downloadUrl);
               } else {
                   result.addError("", resp.getRespBody().getString("reason"));
               }
            }
        } catch (ClientException e){
            result.addError("", "upload file to dfs failed:" + e.getMessage());
        }
        return result;
    }
}
