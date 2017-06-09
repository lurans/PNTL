/*
 * 文 件 名:  KeystoneWrapperServiceImpl.java
 * 版 本 号:  V1.0.0
 * 版    权:  Huawei Technologies Co., Ltd. Copyright 1988-2008,  All rights reserved
 * 创建日期:  2015-3-11
 */
package com.huawei.blackhole.network.extention.service.openstack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.constants.*;
import com.huawei.blackhole.network.common.exception.CommonException;
import com.huawei.blackhole.network.common.utils.MapUtils;
import com.huawei.blackhole.network.common.utils.WccCrypter;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.PntlTokenResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.huawei.blackhole.network.api.bean.OncfConfig;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.exception.InvalidFormatException;
import com.huawei.blackhole.network.common.exception.InvalidParamException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.RegexUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneAuthV3;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneAuthV3.AuthScope;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneAuthV3.AuthScope.AuthDomain;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneAuthV3.AuthScope.ScopeProject;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneTokenV3;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.TokenResponse;

/**
 * iam交互实现类
 */
@Service("keystoneService")
public class Keystone {
    private static final Logger LOG = LoggerFactory.getLogger(Keystone.class);

    private static final ConfUtil CONF = ConfUtil.getInstance();

    private static final String V3_VERSION = "/v3";

    private static final String FSP_ADMIN_DOMAIN_ID = "default";

    private static final String X_SUBJECT_TOKEN = "X-Subject-Token";

    private static final String SERVICE_NAME = FspServiceName.KEYSTONE;

    private static final Object LOCK = new Object();

    private static final String BASIC_TOKEN = "basic bXkwWVlFWTBPcHhTUENfNlk2MDJpRF" +
            "lGdFJBYTpyeWxlWkhQdDdNdHBLNTV4REoyUU04V1BLRVFh";

    private static final String GRANT_TYPE = "grant_type";

    /**
     * 判断fsp的账户信息是否是可用的。<br \>
     * 
     * PS : <br \>
     * 本操作设计更改hosts文件： <br \>
     * * 对于错误的fsp信息，hosts文件将会恢复到以前的hosts版本。<br \>
     * * 如果fsp信息正确，对hosts信息的操作将保留。
     * 
     * @param fspInfo
     * @return
     * @throws InvalidParamException
     * @throws ClientException
     * @throws ApplicationException
     */
    public boolean validFspConfig(OncfConfig fspInfo) throws InvalidParamException, ClientException,
            ApplicationException {
        checkFspInfo(fspInfo); // 参数检查
        boolean succ = false;
        // 写入host
        synchronized (LOCK) {
            File backupHost = backupHost();
            try {
                writeToHost(fspInfo);
                // 验证登录
                succ = validFsp(fspInfo);
            } catch (IOException e) {
                String errMsg = "fail to write " + Constants.HOST_FILE_PATH + " : " + e.getLocalizedMessage();
                throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
            } catch (Exception e) {
                throw new ApplicationException(ExceptionType.UNKOWN_ERR, e.getLocalizedMessage());
            } finally {
                revertHost(backupHost);
                deleteBackupHost(backupHost);
            }
        }
        return succ;
    }

    private boolean validFsp(OncfConfig fspInfo) throws ClientException {
        String url = getUrlToken(fspInfo.getOsAuthUrl());
        LOG.info("get fsp token url={}", url);
        KeystoneAuthV3 keystoneAuthV3 = getAuth(fspInfo);
        String token = getToken(url, keystoneAuthV3);
        return StringUtils.isNotEmpty(token);
    }

    @SuppressWarnings("unchecked")
    public boolean validFspConfig() throws ConfigLostException, InvalidFormatException, ApplicationException,
            ClientException, InvalidParamException ,CommonException{
        Map<String, Object> confInfo = (Map<String, Object>) YamlUtil.getConf(Resource.NAME_CONF);
        WccCrypter.decryptMapEntry(confInfo,Config.FSP_KEY_OS_PASSWORD);

        OncfConfig fspInfo = getFspByConfInfo(confInfo);
        checkFspInfo(fspInfo); // 参数检查
        return validFsp(fspInfo);
    }

    /**
     * keystone 获取原生openstack token
     *
     * @return string
     * @throws ClientException
     */
    public String getToken() throws ClientException, ConfigLostException {
        String url = getUrlToken(getEndpoint(null));
        LOG.info("get fsp token url={}", url);

        return getToken(url, getDefaultAuth());
    }

    /**
     * get auth token from AUTH SERVICE
     * @return
     */
    public String getPntlAccessToken() throws ClientException{
        Map<String, String> header = new HashMap<>();

        header.put(PntlInfo.AUTH, BASIC_TOKEN);
        header.put(PntlInfo.CONTENT_TYPE, PntlInfo.X_FORM_URLENCODED);

        List<NameValuePair> reqBody = new ArrayList<NameValuePair>();
        reqBody.add(new BasicNameValuePair(PntlInfo.GRANT_TYPE, "client_credentials"));

        String url = PntlInfo.URL_IP+PntlInfo.TOKEN_URL_SUFFIX;
        RestResp rsp = RestClientExt.post(url, null, reqBody, header);
        if (rsp == null){
        //    throw new ClientException(ExceptionType.ENV_ERR, "can not get access token");
        }
        //return tokenResponse.getAccessToken();
        return rsp.getRespBody().getString("access_token");
    }

    /**
     * Get tenant id of admin user.
     *
     * @return Tenant id.
     * @throws ClientException
     * @throws ConfigLostException
     */
    public String getTenantId() throws ClientException, ConfigLostException {
        String url = getUrlToken(getEndpoint(null));
        LOG.info("get fsp tenant id url={}", url);
        TokenResponse tokenResponse = RestClientExt.post(url, null, getDefaultAuth(), TokenResponse.class, null);
        if (tokenResponse == null) {
            throw new ClientException(ExceptionType.ENV_ERR, "can not find tenant");
        }
        return tokenResponse.getProjectId();
    }

    /**
     * keystone 获取原生openstack token
     *
     * @return string
     * @throws ClientException
     */
    public KeystoneTokenV3 getAdminToken() throws ClientException, ConfigLostException {
        String url = getUrlToken(getEndpoint(null));
        LOG.info("get fsp token url={}", url);

        return RestClientExt.post(url, null, getDefaultAuth(), KeystoneTokenV3.class, null);
    }

    private OncfConfig getFspByConfInfo(Map<String, Object> confInfo) {
        OncfConfig fspInfo = new OncfConfig();
        fspInfo.setCascadingIp(MapUtils.getAsStr(confInfo,Config.FSP_KEY_CASCADIND_IP));
        fspInfo.setOsAuthUrl(MapUtils.getAsStr(confInfo,Config.FSP_KEY_OS_AUTH_URL));
        fspInfo.setOsPassword(MapUtils.getAsStr(confInfo,(Config.FSP_KEY_OS_PASSWORD)));
        fspInfo.setOsTenantName(MapUtils.getAsStr(confInfo,Config.FSP_KEY_OS_TENANT_NAME));
        fspInfo.setOsUsername(MapUtils.getAsStr(confInfo,Config.FSP_KEY_OS_USERNAME));
        return fspInfo;
    }

    private void writeToHost(OncfConfig fspInfo) throws IOException {
        Map<String, String> keystoneInfo = new HashMap<String, String>();
        keystoneInfo.put(fspInfo.getOsAuthUrl(), fspInfo.getCascadingIp());
        writeHostFile(keystoneInfo);

    }

    /**
     * 还原hosts文件
     * 
     * @param backupHost
     * @throws ApplicationException
     * @throws InvalidParamException
     */
    private void revertHost(File backupHost) throws ApplicationException, InvalidParamException {
        File hosts = new File(Constants.HOST_FILE_PATH);
        try {
            FileUtil.copy(backupHost, hosts);
        } catch (IOException e) {
            String errMsg = "fail to revert hosts file";
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        }
    }

    /**
     * 删除备份的hosts文件
     */
    private void deleteBackupHost(File backupHost) {
        if (backupHost != null) {
            backupHost.delete();
        }
    }

    /**
     * 创建临时文件，保存host原本的内容
     * 
     * @return
     * @throws ApplicationException
     *             : fail to create backup host file
     */
    private File backupHost() throws ApplicationException {
        File newHostFile = null;
        InputStream input = null;
        OutputStream output = null;
        try {
            newHostFile = File.createTempFile("tmp-", "-host");
            input = new FileInputStream(Constants.HOST_FILE_PATH);
            output = new FileOutputStream(newHostFile);
            IOUtils.copy(input, output);
            output.flush();
        } catch (IOException e) {
            String errMsg = "fail to create backup hosts file";
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
            }
            input = null;
            output = null;
        }

        return newHostFile;
    }

    private void writeHostFile(Map<String, String> content) throws IOException {
        Map<String, String> newContent = new HashMap<String, String>();
        String filePath = Constants.HOST_FILE_PATH;
        Pattern pattern = Pattern.compile("([a-zA-Z]+)://(.+):([0-9]+)");
        for (Map.Entry<String, String> contentEntry : content.entrySet()) {
            String contentKey = contentEntry.getKey();
            String contentValue = contentEntry.getValue();
            Matcher matcher = pattern.matcher(contentKey);
            if (matcher.find()) {
                newContent.put(matcher.group(Constants.NUM_TWO), contentValue);
            }
        }
        FileUtil.write(filePath, FileUtil.read(filePath, newContent));
    }

    private void checkFspInfo(OncfConfig fspInfo) throws InvalidParamException {
        boolean valid = true;
        if (fspInfo == null) {
            valid = false;
        }
        if (valid) {
            if (StringUtils.isEmpty(fspInfo.getCascadingIp()) || StringUtils.isEmpty(fspInfo.getOsAuthUrl())
                    || StringUtils.isEmpty(fspInfo.getOsPassword()) || StringUtils.isEmpty(fspInfo.getOsTenantName())
                    || StringUtils.isEmpty(fspInfo.getOsUsername())) {
                valid = false;
            }
        }
        if (valid) {
            if (!RegexUtil.validAuthUrl(fspInfo.getOsAuthUrl())) {
                valid = false;
            }
        }
        if (!valid) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid fsp configuration");
        }
    }

    private KeystoneAuthV3 getAuth(OncfConfig fspInfo) {
        String username = fspInfo.getOsUsername();
        String password = fspInfo.getOsPassword();
        String tenantName = fspInfo.getOsTenantName();
        AuthScope authScope = new AuthScope(new ScopeProject(tenantName, new AuthDomain(FSP_ADMIN_DOMAIN_ID, null)));
        return new KeystoneAuthV3(username, password, FSP_ADMIN_DOMAIN_ID, null, authScope);
    }

    private KeystoneAuthV3 getDefaultAuth() throws ConfigLostException {
        String user_name = CONF.getConfAsString(Config.FSP_ADMIN_USER_NAME);
        String password = CONF.getConfAsString(Config.FSP_ADMIN_PASSWORD);
        String tenant_name = CONF.getConfAsString(Config.FSP_ADMIN_TENANT_NAME);
        AuthScope authScope = new AuthScope(new ScopeProject(tenant_name, new AuthDomain(FSP_ADMIN_DOMAIN_ID, null)));
        return new KeystoneAuthV3(user_name, password, FSP_ADMIN_DOMAIN_ID, null, authScope);
    }

    private String getToken(String url, KeystoneAuthV3 auth) throws ClientException {
        Map<String, String> header = new HashMap<String, String>(4);
        header.put("Connection", "close");
        RestResp response = RestClientExt.post(url, null, auth, header);
        return getSubjectToken(response.getHeader());
    }

    private String getUrlToken(String endpoint) {
        return endpoint + "/identity" + V3_VERSION + "/auth/tokens";
    }

    private String getSubjectToken(Header[] header) {
        if (header != null) {
            for (Header h : header) {
                if (h.getName().equals(X_SUBJECT_TOKEN)) {
                    return h.getValue();
                }
            }
        }

        return null;
    }

    private String getEndpoint(String region) throws ConfigLostException {
        if (null == region) {
            return CONF.getConfAsString(Config.FSP_ADMIN_AUTH_URL);
        }
        return CONF.getConfAsString(getServiceRegion(SERVICE_NAME, region));
    }

    private String getServiceRegion(String serviceName, String region) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(serviceName);
        stringBuilder.append(Constants.SYMBOL_DOT);
        stringBuilder.append(region);
        return stringBuilder.toString();
    }

}
