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
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.service.PntlService;
import com.huawei.blackhole.network.extention.bean.pntl.AgentConfig;
import com.huawei.blackhole.network.extention.bean.pntl.CommonInfo;
import com.huawei.blackhole.network.extention.service.openstack.Keystone;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Service("pntlConfigService")
public class PntlConfigService {
    private static Logger LOGGER = LoggerFactory.getLogger(PntlConfigService.class);
    @javax.annotation.Resource(name = "keystoneService")
    private Keystone identityWrapperService;

    @javax.annotation.Resource(name = "pntlService")
    private PntlService pntlService;

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
            String errMsg = "invalid format: " + PntlInfo.PNTL_CONF;
            LOGGER.error(errMsg, e);
            result.addError("", errMsg);
        } catch (ApplicationException e) {
            String errMsg = "fail to get configuration: " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
        }
        return result;
    }

    /**
     * 将key:value保存到pntlConfig.yml文件汇总
     * @param key
     * @param value
     * @return
     */
    private Result<String> saveElemToPntlConfigFile(String key, String value){
        Result<String> result = new Result<String>();
        if (key == null){
            result.addError("", "key is null");
            return result;
        }

        try{
            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            dataObj.put(key, value);
            YamlUtil.setConf(dataObj, PntlInfo.PNTL_CONF);
        }catch (ApplicationException e) {
            String errMsg = "save " + key + ":" + value+" to " + PntlInfo.PNTL_CONF + " failed: " + e.getLocalizedMessage();
            LOGGER.error(errMsg);
            result.addError("", e.prefix() + errMsg);
        } catch (Exception e){
            result.addError("", "parameter is invalid");
        }
        return result;
    }

    private boolean checkIpValid(String kafkaUrl, String repoUrl){
        Pattern pattern = Pattern
            .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                    + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");

        if (!StringUtils.isEmpty(kafkaUrl) && !pattern.matcher(kafkaUrl).matches()) {
            return false;
        }

        if (!StringUtils.isEmpty(repoUrl) && !pattern.matcher(repoUrl).matches()) {
            return false;
        }

        return true;
    }

    private boolean checkAkSkTopicValid(String ak, String sk, String kafkaTopic){
        if (StringUtils.isEmpty(ak) || StringUtils.isEmpty(sk) || StringUtils.isEmpty(kafkaTopic)){
            return false;
        }

        if(ak.length() > 30 || sk.length() > 30|| kafkaTopic.length() > 20){
            return false;
        }

        Pattern pattern = Pattern
                .compile("[a-zA-Z0-9_]+");
        if (!pattern.matcher(ak).matches()
                || !pattern.matcher(sk).matches()
                || !pattern.matcher(kafkaTopic).matches()){
            return false;
        }

        return true;
    }

    public Result<String> setPntlDeployConfig(PntlConfig pntlConfig) {
        //Get PntlConfig
        Result<String> result = new Result<String>();
        if (!checkAkSkTopicValid(pntlConfig.getAk(), pntlConfig.getSk(), pntlConfig.getTopic())){
            result.addError("", "ak, sk or topic is invalid");
            return result;
        }
        if(!checkIpValid(pntlConfig.getKafkaIp(),pntlConfig.getRepoUrl())){
            result.addError("", "repoUrl or kafkaUrl is invalid");
            return result;
        }

        try{
            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            dataObj.put("ak", pntlConfig.getAk());
            dataObj.put("sk", pntlConfig.getSk());
            dataObj.put("repo_url", pntlConfig.getRepoUrl());
            dataObj.put("kafka_ip", pntlConfig.getKafkaIp());
            dataObj.put("topic", pntlConfig.getTopic());

            pntlConfig.setByMap(dataObj);
            pntlConfig.setBasicToken(genBasicToken(pntlConfig.getAk(), pntlConfig.getSk()));

            //保存在文件
            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, PntlInfo.PNTL_CONF);
        }catch (ApplicationException e) {
            String errMsg = "set config [" + PntlInfo.PNTL_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
            return result;
        } catch (Exception e){
            result.addError("", "parameter is invalid");
            return result;
        }
        //保存在内存公共信息
        CommonInfo.setRepoUrl(pntlConfig.getRepoUrl());
        CommonInfo.setKafkaIp(pntlConfig.getKafkaIp());
        CommonInfo.setTopic(pntlConfig.getTopic());

        LOGGER.info("Update pntlConfig success");

        return result;
    }

    public Result<String> setPntlAgentConfig(PntlConfig pntlConfig) {
        Result<String> result = new Result<String>();
        try {
            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            dataObj.put("probe_period", pntlConfig.getProbePeriod());
            dataObj.put("port_count", pntlConfig.getPortCount());
            dataObj.put("report_period", pntlConfig.getReportPeriod());
            dataObj.put("pkg_count", pntlConfig.getPkgCount());
            dataObj.put("delay_threshold", pntlConfig.getDelayThreshold());
            dataObj.put("lossRate_threshold", pntlConfig.getLossRateThreshold());
            dataObj.put("dscp", pntlConfig.getDscp());
            dataObj.put("lossPkg_timeout", pntlConfig.getLossPkgTimeout());
            dataObj.put("dropPkgThresh", pntlConfig.getDropPkgThresh());
            dataObj.put("package_size", pntlConfig.getPackageSize());

            validPntlConfig(pntlConfig);

            pntlConfig.setByMap(dataObj);
            pntlConfig.setBasicToken(genBasicToken(pntlConfig.getAk(), pntlConfig.getSk()));

            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, PntlInfo.PNTL_CONF);
        } catch (ApplicationException | InvalidParamException e) {
            String errMsg = "set config [" + PntlInfo.PNTL_CONF + "] failed : " + e.getLocalizedMessage();
            LOGGER.error(errMsg, e);
            result.addError("", e.prefix() + errMsg);
            return result;
        } catch (Exception e){
            result.addError("", "parameter is invalid");
            return result;
        }

        LossRate.setLossRateThreshold(Integer.valueOf(pntlConfig.getLossRateThreshold()));
        DelayInfo.setDelayThreshold(Long.valueOf(pntlConfig.getDelayThreshold()));
        LOGGER.info("Update pntlConfig success");

        return result;
    }

    private void validPntlConfig(PntlConfig pntlConfig) throws Exception {
        if (pntlConfig == null){
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "no data provided");
        }

        try {
            int probe_period = Integer.valueOf(pntlConfig.getProbePeriod());
            if (probe_period < 1 || probe_period > 120) {
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "probe period is invalid");
            }

            int port_count = Integer.valueOf(pntlConfig.getPortCount());
            if (port_count < 1 || port_count > 100){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "port count is invalid");
            }
            int report_period = Integer.valueOf(pntlConfig.getReportPeriod());
            if (report_period < 5 || report_period > 300){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "report period is invalid");
            }

            /*上报周期需要>=探测周期*/
            if (report_period < probe_period){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "report period shoud be bigger then probe period");
            }

            int large_pkg_count = Integer.valueOf(pntlConfig.getPkgCount());
            if (large_pkg_count != 0 && large_pkg_count != 100){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "package count is invalid");
            }

            int delay_threshold = Integer.valueOf(pntlConfig.getDelayThreshold());
            if (delay_threshold < 1 || delay_threshold > 2000){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "delay threshold is invalid");
            }

            int lossRate_threshold = Integer.valueOf(pntlConfig.getLossRateThreshold());
            if (lossRate_threshold < 1 || lossRate_threshold > 100){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "lossRate threshold is invalid");
            }

            int dscp = Integer.valueOf(pntlConfig.getDscp());
            if (dscp < 0 || dscp > 63){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "dscp is invalid");
            }

            int lossPkg_timeout = Integer.valueOf(pntlConfig.getLossPkgTimeout());
            if (lossPkg_timeout < 1 || lossPkg_timeout > 5){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "loss package timeout is invalid");
            }

            int dropPkgThresh = Integer.valueOf(pntlConfig.getDropPkgThresh());
            if (dropPkgThresh < 1 || dropPkgThresh > 10){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "drop package threshold is invalid");
            }

            int package_size = Integer.valueOf(pntlConfig.getPackageSize());
            if (package_size < 40 || package_size > 2000){
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "package size threshold is invalid");
            }
        } catch (Exception e){
            throw new Exception();
        }
    }

    private String genBasicToken(String ak, String sk){
        String str = ak + ":" + sk;
        byte[] encodeBasic64 = Base64.encodeBase64(str.getBytes());
        return new String(encodeBasic64);
    }

    private void validIpListAttachment(Attachment file, String filename) throws InvalidParamException, InvalidFormatException {
        if (file == null) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid request to upload ipList file");
        }
        String contentDisposition = file.getHeader("Content-Disposition");
        if ((contentDisposition == null) || (!contentDisposition.contains("filename"))) {
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
        String name = StringUtils.isEmpty(othername) ? file.getDataHandler().getName() : othername;
        try {
            validIpListAttachment(file, name);
        } catch (InvalidParamException | InvalidFormatException e) {
            String errMsg = "upload ipList failed:" + e.getMessage();
            result.addError("", errMsg);
            LOGGER.error(errMsg);
            return result;
        }

        File destinationFile = new File(getFileName(name));
        try {
            if (StringUtils.isEmpty(othername)) {
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

        /*更新ipList之后，重新加载文件*/
        if (StringUtils.isEmpty(othername)){
            result = pntlService.initHostList();
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
        if (!name.equalsIgnoreCase(PntlInfo.AGENT_EULER) && !name.equalsIgnoreCase(PntlInfo.AGENT_SUSE)){
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, "invalid filename:" + name);
        }

        if (new File(file.getDataHandler().getName()).length() > Constants.AGENT_FILE_MAX_SIZE){
            String errMsg = "too large file [ max size : " + Constants.AGENT_FILE_MAX_SIZE + "bytes ]";
            throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
        }
    }

    /**
     * 根据文件名，保存对应的repoUrl
     * @param filename
     * @param repoUrl
     */
    private void saveDownloadUrlToFile(String filename, String repoUrl){
        if (filename.equalsIgnoreCase(PntlInfo.AGENT_EULER)){
            saveElemToPntlConfigFile("eulerRepoUrl", repoUrl);
        } else if (filename.equalsIgnoreCase(PntlInfo.AGENT_SUSE)){
            saveElemToPntlConfigFile("suseRepoUrl", repoUrl);
        } else if (filename.equalsIgnoreCase(PntlInfo.AGENT_INSTALL_FILENAME)){
            saveElemToPntlConfigFile("installScriptRepoUrl", repoUrl);
        } else if (filename.equalsIgnoreCase(PntlInfo.AGENT_CONF)){
            saveElemToPntlConfigFile("agentConfUrl", repoUrl);
        }
    }

    private void deleteOldAgentFile(String name){
        new File(FileUtil.getResourcePath() + name).delete();
    }

    private Result<String> uploadAgentPkgToServer(Attachment file){
        Result<String> result = new Result<>();
        String name = file.getDataHandler().getName();
        File destinationFile = new File(getFileName(name));
        try {
            deleteOldAgentFile(name);
            file.transferTo(destinationFile);
        } catch (IOException e) {
            String errMsg = "file to load ipList file to server : " + e.getLocalizedMessage();
            result.addError("", ExceptionUtil.prefix(ExceptionType.SERVER_ERR) + errMsg);
            LOGGER.error(errMsg, e);
            return result;
        }
        return result;
    }

    private String getToken(){
        String token = null;
        try {
            token = identityWrapperService.getPntlAccessToken();
        } catch (ClientException e) {
            String errMsg = "get token failed:" + e.getMessage();
            LOGGER.error("", errMsg);
            return null;
        }
        return token;
    }

    /**
     * 上传文件到DFS仓库
     * @param filename
     * @return
     */
    public Result<String> uploadFilesToDFS(String filename){
        Result<String> result = new Result<>();
        if (StringUtils.isEmpty(filename)){
            result.addError("", "filename is null, cannot upload files to DFS");
            return result;
        }

        File f = new File(getFileName(filename));

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody("attachment", f);
        builder.addTextBody("type", "0");
        builder.addTextBody("uploader", PntlInfo.OPS_USERNAME);
        builder.addTextBody("space", PntlInfo.PTNL_UPLOADER_SPACE);
        builder.addTextBody("override", "true");
        builder.addTextBody("description", "");
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        HttpEntity entity = builder.build();

        RestResp resp = null;
        String url = Constants.HTTPS_PREFIX + CommonInfo.getRepoUrl() + PntlInfo.DFS_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();

        String token = getToken();
        if (StringUtils.isEmpty(token)){
            result.addError("", "Get token failed");
            return result;
        }

        Pntl.setCommonHeaderForAgent(header, token);
        try{
            resp = RestClientExt.post(url, entity, header);
            if (resp.getStatusCode().isError()){
                result.addError("", "upload file to dfs failed:" + resp.getStatusCode());
            } else {
                if (resp.getRespBody().getInt("code") == 0){
                    String downloadUrl = resp.getRespBody().getJSONObject("data").getString("downloadUrl");
                   /* 保存到内存 */
                    Pntl.setDownloadUrl(downloadUrl);
                   /* 保存到文件 */
                    saveDownloadUrlToFile(filename, downloadUrl);
                } else {
                    result.addError("", resp.getRespBody().getString("reason"));
                }
            }
        } catch (ClientException e){
            result.addError("", "upload file to dfs failed:" + e.getMessage());
        }
        return result;
    }

    public Result<String> uploadAgentPkgFile(Attachment attachment) {
        Result<String> result = new Result<String>();
        try {
            validAgentPkgAttachment(attachment);
        } catch (InvalidParamException | InvalidFormatException e) {
            String errMsg = "upload agentPkg failed" + e.toString();
            result.addError("", errMsg);
            LOGGER.error(errMsg);
            return result;
        }

        result = uploadAgentPkgToServer(attachment);
        if (!result.isSuccess()){
            String errMsg = "upload to server fail";
            result.addError("", errMsg);
            LOGGER.error(errMsg);
            return  result;
        }

        return uploadFilesToDFS(attachment.getDataHandler().getName());
    }

    public Result<String> writeInfoToAgentConf(String filename){
        Result<String> result = new Result<>();
        Result<PntlConfig> pntlConf = getPntlConfig();
        if (!pntlConf.isSuccess()){
            result.addError("", pntlConf.getErrorMessage());
            return result;
        }

        AgentConfig agentConfig = new AgentConfig();
        agentConfig.setDelayThreshold(pntlConf.getModel().getDelayThreshold());
        agentConfig.setDscp(pntlConf.getModel().getDscp());
        agentConfig.setKafkaIp(pntlConf.getModel().getKafkaIp());
        agentConfig.setLossPkgTimeout(pntlConf.getModel().getLossPkgTimeout());
        agentConfig.setPkgCount(pntlConf.getModel().getPkgCount());
        agentConfig.setPortCount(pntlConf.getModel().getPortCount());
        agentConfig.setProbePeriod(pntlConf.getModel().getProbePeriod());
        agentConfig.setReportPeriod(pntlConf.getModel().getReportPeriod());
        agentConfig.setTopic(pntlConf.getModel().getTopic());
        agentConfig.setDropPkgThresh(pntlConf.getModel().getDropPkgThresh());
        agentConfig.setPackageSize(pntlConf.getModel().getPackageSize());
        //server启动，通知agent，用于上报vbondIp
        agentConfig.setVbondIpFlag(CommonInfo.getServerStart());
        Result<Map<String, List<String>>> pingList = pntlService.getPingList();
        agentConfig.setPingList(pingList.getModel());
        System.out.println("vbondIp_flag:" + agentConfig.getVbondIpFlag());
        try {
            FileUtil.write(FileUtil.getPath(PntlInfo.AGENT_CONF) , agentConfig.toString());
        } catch (IOException e){
            String errMsg = "write " + PntlInfo.AGENT_CONF + "failed:" + e.getMessage();
            LOGGER.error(errMsg);
            result.addError("", errMsg);
        }

        return result;
    }
}
