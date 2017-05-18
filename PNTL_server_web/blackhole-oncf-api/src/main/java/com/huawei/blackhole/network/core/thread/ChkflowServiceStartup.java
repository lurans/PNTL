package com.huawei.blackhole.network.core.thread;

import com.huawei.blackhole.network.common.constants.Config;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.FspServiceName;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigConflictException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.ConfUtil;
import com.huawei.blackhole.network.common.utils.ExceptionUtil;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.resource.AppStatus;
import com.huawei.blackhole.network.core.service.StartupService;
import com.huawei.blackhole.network.extention.bean.openstack.cps.FcNovaComputeParam;
import com.huawei.blackhole.network.extention.bean.openstack.keystone.KeystoneTokenV3;
import com.huawei.blackhole.network.extention.service.openstack.Keystone;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import javax.annotation.Resource;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("chkflowServiceStartup")
public class ChkflowServiceStartup implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ChkflowServiceStartup.class);

    private static final ConfUtil CONF = ConfUtil.getInstance();

    @Resource(name = "keystoneService")
    private Keystone keystoneService;

    @Resource(name = "startupService")
    private StartupService startupService;

    public static boolean isStarted() {
        return AppStatus.isStarted();
    }

    @SuppressWarnings("unused")
    private String adminTenantId;

    private String cascadingRegion = null;

    private Map<String, String> serviceRegionURIMap = null;

    private Map<String, String> regionIpMap = null;

    private Map<String, FcNovaComputeParam> vrmParamMap = null;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            doRegistry();
        } catch (ClientException | ConfigLostException | IOException | ConfigConflictException e) {
            LOG.error("ignore error : " + e.getLocalizedMessage() + "\nthis is normal if caused by fsp config", e);
        } catch (Exception e) {
            LOG.error("ignore error : " + e.getLocalizedMessage() + "\nthis is normal if caused by fsp config", e);
        } finally {
            LOG.info("service started");
            AppStatus.started();
//            ChkflowServiceStartup.started = true;
        }
    }

    public Result<String> registry() {
        Result<String> result = new Result<String>();
//        ChkflowServiceStartup.started = false;
        AppStatus.stop();
        try {
            CONF.reinit();
            doRegistry();
        } catch (ConfigLostException | ClientException | ConfigConflictException e) {
            result.addError("", e.toString());
        } catch (IOException e) {
            result.addError("", ExceptionUtil.prefix(ExceptionType.SERVER_ERR) + "fail to restart service");
        } catch (Exception e) {
            result.addError("", ExceptionUtil.prefix(ExceptionType.SERVER_ERR) + "fail to restart service");
        } finally {
            LOG.info("service started");
//            ChkflowServiceStartup.started = true;
            AppStatus.started();
        }
        return result;

    }

    private void doRegistry() throws ConfigLostException, IOException, ClientException, ConfigConflictException {
        // 1.写keystone信息到hosts文件中，并通过keystone查询服务URI信息
        writeKeystoneInfo();
        KeystoneTokenV3 adminToken = keystoneService.getAdminToken();
        if (adminToken == null) {
            throw new ClientException(ExceptionType.SERVER_ERR, "can not get KeyStone V3 from fsp");
        }
        adminTenantId = adminToken.getProjectId();
        serviceRegionURIMap = startupService.getServiceEndpoints(adminToken);
        cascadingRegion = getCascadingRegion();
        CONF.regConfAsString(Config.FSP_CASCADING_REGION, cascadingRegion);
        registMaptoCONF(serviceRegionURIMap);

        // 2.写级联层服务信息到hosts文件中，并通过CPS查询各被级联层pod和ip映射信息
        writeCascadingInfo();
        String tokenString = keystoneService.getToken();
        regionIpMap = startupService.getRegionIpMap(null, tokenString);
        registMaptoCONF(getRegionIpParamConfMap());

        // 3.写被级联层信息到hosts中，并通过各被级联CPS服务查询各个被级联的VRM参数
        writeCascadedInfo();
        List<String> cascadedCpsRegions = getCascadedCpsRegions();
        vrmParamMap = startupService.getFcNovaComputeParamofRegions(cascadedCpsRegions, tokenString);
        registMaptoCONF(getVrmParamConfMap());
        registMaptoCONF(getRolesParamMap());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getRolesParamMap() throws ClientException {
        InputStream reader = null;
        Map<String, String> confMap = null;
        String roleConf = com.huawei.blackhole.network.common.constants.Resource.ROLE_CONF;
        try {
            String directory = ConfUtil.class.getResource("/").getPath().replace("%20", " ");
            String path = null;
            final String separator = System.getProperty("file.separator");
            if (separator.equals("\\")) {
                if (directory.startsWith("/")) {
                    directory = directory.substring(1);
                }
                path = directory + roleConf;
            } else {
                path = directory + roleConf;
            }

            Yaml yaml = new Yaml(new SafeConstructor());
            reader = new BufferedInputStream(new FileInputStream(path));
            confMap = (Map<String, String>) yaml.load(reader);
        } catch (Exception e) {
            LOG.error("loading configuration file[{}] fail:", roleConf, e);
            try {
                if (null != reader) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException ex) {
                LOG.error("close file fail: {}", ex);
            }
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                LOG.error("close file fail:", e);
            }
        }
        if (confMap == null) {
            String errMsg = "configuration in role.yml error";
            LOG.error(errMsg);
            throw new ClientException(ExceptionType.CLIENT_ERR, errMsg);
        }
        return confMap;
    }

    private String getCascadingRegion() throws ConfigLostException {
        for (Map.Entry<String, String> serviceRegionURIEntry : serviceRegionURIMap.entrySet()) {
            String serviceRegion = serviceRegionURIEntry.getKey();
            if (CONF.getConfAsString(Config.FSP_ADMIN_AUTH_URL).contains(serviceRegionURIEntry.getValue())) {
                int index = serviceRegion.indexOf(Constants.SYMBOL_DOT);
                return serviceRegion.substring(index + 1);
            }
        }
        throw new ConfigLostException(ExceptionType.SERVER_ERR, "can not find cascading region");
    }

    private void registMaptoCONF(Map<String, String> conf) throws ConfigConflictException {
        for (Map.Entry<String, String> confEntry : conf.entrySet()) {
            CONF.regConfAsString(confEntry.getKey(), confEntry.getValue());
        }
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

    private void writeKeystoneInfo() throws ConfigLostException, IOException {
        Map<String, String> keystoneInfo = new HashMap<String, String>();
        keystoneInfo
                .put(CONF.getConfAsString(Config.FSP_ADMIN_AUTH_URL), CONF.getConfAsString(Config.FSP_CASCADIND_IP));
        writeHostFile(keystoneInfo);
    }

    private void writeCascadingInfo() throws ConfigLostException, IOException {
        Map<String, String> cascadingInfo = new HashMap<String, String>();
        for (Map.Entry<String, String> serviceRegionURIEntry : serviceRegionURIMap.entrySet()) {
            String serviceRegion = serviceRegionURIEntry.getKey();
            String regionURI = serviceRegionURIEntry.getValue();
            if (serviceRegion.contains(cascadingRegion)) {
                cascadingInfo.put(regionURI, CONF.getConfAsString(Config.FSP_CASCADIND_IP));
            }
        }
        writeHostFile(cascadingInfo);
    }

    private void writeCascadedInfo() throws IOException {
        Map<String, String> cascadedInfo = new HashMap<String, String>();
        Pattern pattern = Pattern.compile("([a-zA-Z]+)://([a-zA-Z]+)\\.(.+):([0-9]+)");
        List<String> needRemoveRegion = new ArrayList<String>();
        for (Map.Entry<String, String> serviceRegionURIEntry : serviceRegionURIMap.entrySet()) {
            String serviceRegion = serviceRegionURIEntry.getKey();
            if (!serviceRegion.contains(cascadingRegion)) {
                String regionURI = serviceRegionURIEntry.getValue();
                Matcher matcher = pattern.matcher(regionURI);
                if (matcher.find()) {
                    String ip = regionIpMap.get(matcher.group(Constants.NUM_THREE));
                    if (StringUtils.isEmpty(ip)) {
                        LOG.error("region {} is not exites", serviceRegion);
                        needRemoveRegion.add(serviceRegion);
                        continue;
                    }
                    cascadedInfo.put(regionURI, ip);
                }
            }
        }
        if (needRemoveRegion.size() != 0) {
            for (String serviceRegion : needRemoveRegion) {
                serviceRegionURIMap.remove(serviceRegion);
            }
        }
        writeHostFile(cascadedInfo);
    }

    private List<String> getCascadedCpsRegions() {
        List<String> cascadedCpsRegions = new ArrayList<String>();
        for (Map.Entry<String, String> serviceRegionURIEntry : serviceRegionURIMap.entrySet()) {
            String serviceRegion = serviceRegionURIEntry.getKey();
            if (serviceRegion.startsWith(FspServiceName.CPS) && !serviceRegion.contains(cascadingRegion)) {
                int index = serviceRegion.indexOf(Constants.SYMBOL_DOT);
                cascadedCpsRegions.add(serviceRegion.substring(index + 1));
            }
        }
        return cascadedCpsRegions;
    }

    private Map<String, String> getVrmParamConfMap() {
        Map<String, String> vrmParamConfMap = new HashMap<String, String>();
        for (Map.Entry<String, FcNovaComputeParam> vrmParamEntry : vrmParamMap.entrySet()) {
            String fcParamKey = vrmParamEntry.getKey();
            FcNovaComputeParam fcParamValue = vrmParamEntry.getValue();
            String ipParamName = getConfKey(FspServiceName.VRM, fcParamKey, null);
            String fcURL = Constants.HTTP_PREFIX + fcParamValue.getFcIp() + Constants.SYMBOL_COLON + Constants.VRM_PORT;
            vrmParamConfMap.put(ipParamName, fcURL);
            String userParamName = getConfKey(FspServiceName.VRM, fcParamKey, Constants.PARAM_USER);
            vrmParamConfMap.put(userParamName, fcParamValue.getFcUser());
            String passwdParamName = getConfKey(FspServiceName.VRM, fcParamKey, Constants.PARAM_PASSWORD);
            vrmParamConfMap.put(passwdParamName, fcParamValue.getFcPasswd());
        }
        return vrmParamConfMap;
    }

    private Map<String, String> getRegionIpParamConfMap() {
        Map<String, String> regionIpParamConfMap = new HashMap<String, String>();
        for (Map.Entry<String, String> regionIpEntry : regionIpMap.entrySet()) {
            if (!StringUtils.isEmpty(regionIpEntry.getValue())) {
                regionIpParamConfMap.put(getConfKey(regionIpEntry.getKey(), Constants.PARAM_IP, null),
                        regionIpEntry.getValue());
            }

        }
        return regionIpParamConfMap;
    }

    private String getConfKey(String serviceName, String region, String exetendParam) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(serviceName);
        stringBuilder.append(Constants.SYMBOL_DOT);
        stringBuilder.append(region);
        if (null != exetendParam) {
            stringBuilder.append(Constants.SYMBOL_DOT);
            stringBuilder.append(exetendParam);
        }
        return stringBuilder.toString();
    }

}
