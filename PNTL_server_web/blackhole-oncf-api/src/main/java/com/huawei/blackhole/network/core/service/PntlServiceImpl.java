package com.huawei.blackhole.network.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.api.bean.*;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.common.exception.*;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.*;
import com.huawei.blackhole.network.extention.service.conf.PntlConfigService;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;
import com.huawei.blackhole.network.extention.service.pntl.PntlWarnService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;

@Service("pntlService")
public class PntlServiceImpl extends  BaseRouterService implements PntlService{
    private static final Logger LOG = LoggerFactory.getLogger(PntlServiceImpl.class);
    private List<PntlHostContext> hostList = null;
    private Map<String, String> agentIpMap = null;
    private static boolean notifyAgentToGetPingList = false;

    @javax.annotation.Resource(name = "pntlConfigService")
    private PntlConfigService pntlConfigService;

    @javax.annotation.Resource(name = "pntlWarnService")
    private PntlWarnService pntlWarnService;

    /**
     * 首次启动
     * @return
     */
    public Result<String> deployAgent() {
        Result<String> result = new Result<>();
        try {
            result = installStartAgent(hostList);
        } catch (ClientException e){
            result.setErrorMessage("Install and start agent failed:" + e.getMessage());
            LOG.error("Install and start agent failed: " + e.getMessage());
        }

        return result;
    }

    public Result<AgentFlowsJson> getPingList(PingListRequest config){
        Result<AgentFlowsJson> result = new Result<>();
        if (agentIpMap == null){
            result.addError("", "No host info");
            return result;
        }

        AgentFlowsJson agentFlowsJson = generateAgentFlowJson(config);
        if (agentFlowsJson == null){
            result.addError("", "pingList is null");
            return result;
        }
        result.setModel(agentFlowsJson);

        return result;
    }

    /**
     * 通知所有的agent获取pingMesh
     * @param hostList
     * @return
     */
    private Result<String> notfiyAllAgentsToGetPingList(List<PntlHostContext> hostList){
        Result<String> result = new Result<>();
        if (hostList == null){
            result.addError("", "host is null");
            return result;
        }

        result = sendMsgToAgents();
        if (!result.isSuccess()){
            LOG.error("notfiyAllAgentsToGetPingList failed");
        }
        return result;
    }

    private Result<String> checkAgentVbondIpValid(String agentIp, String vbondIp){
        Result<String> result = new Result<>();
        if (agentIpMap == null){
            agentIpMap = new HashMap<>();
        }

        if (agentIpMap.get(agentIp) != null && agentIpMap.get(agentIp).equals(vbondIp)){
            return result;
        }

        if ("0.0.0.0".equals(vbondIp)){
            result.addError("", "vbondIp is invalid:" + vbondIp);
            return result;
        }

        boolean findIp = false;
        for (PntlHostContext host : hostList){
            if (host.getAgentIp().equals(agentIp)){
                findIp = true;
                break;
            }
        }
        if (!findIp){
            result.addError("", agentIp + "is not exit");
            return result;
        }
        return result;
    }

    /**
     * 保存业务ip，生成pingMesh
     * @param agentIp
     * @param vbondIp
     * @return
     */
    public Result<String> saveVbondIp(String agentIp, String vbondIp){
        Result<String> result = new Result<>();

        result = checkAgentVbondIpValid(agentIp, vbondIp);
        if (!result.isSuccess()){
            return result;
        }

        agentIpMap.put(agentIp, vbondIp);
        addVbondIpToHostList(agentIp, vbondIp);

        /* 首次上报等10s后通知，若已等待中，则直接返回 */
        if (notifyAgentToGetPingList){
            return result;
        } else {
            try {
                notifyAgentToGetPingList = true;
                Thread.sleep(PntlInfo.NOTIFY_AGENT_TO_GET_PINGLIST_TIME * 1000);//second
            } catch(InterruptedException e){
                result.addError("", "Sleep error " + e.getMessage());
            }
        }

        result = notfiyAllAgentsToGetPingList(hostList);
        /* 全部通知完成之后，重新开始一个周期，等待10s，然后全量通知 */
        notifyAgentToGetPingList = false;

        return result;
    }

    private void addVbondIpToHostList(String agentIp, String vbondIp){
        if (hostList == null){
            return;
        }
        for (PntlHostContext host : hostList){
            if (host.getAgentIp().equals(agentIp)){
                host.setVbondIp(vbondIp);
                break;
            }
        }
        updatePingMesh();
    }

    private void updatePingMesh(){
        if (hostList == null){
            return;
        }
        for (PntlHostContext host : hostList){
            host.setPingMeshList(host.getAgentIp(), hostList);
        }
    }

    public Result<String> startAgents(){
        //todo:
        Result<String> result = new Result<>();
        PntlConfig pntlConfig = new PntlConfig();
        try {
            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            dataObj.put("probe_period", pntlConfig.getProbePeriod());
            pntlConfig.setByMap(dataObj);
            Map<String, Object> data = pntlConfig.convertToMap();
            YamlUtil.setConf(data, PntlInfo.PNTL_CONF);

            String token = identityWrapperService.getPntlAccessToken();
            RestResp resp = pntlRequest.startAgent(hostList, token);
            if (resp.getStatusCode().isError()){
                result.addError("", "cmd to start agent failed");
            }
        } catch (ApplicationException e) {
            String errMsg = "set config [" + PntlInfo.PNTL_CONF + "] failed : " + e.getLocalizedMessage();
            result.addError("", e.prefix() + errMsg);
            return result;
        } catch (ConfigLostException e) {
            String errMsg = "set config [" + PntlInfo.PNTL_CONF + "] failed : " + e.getLocalizedMessage();
            result.addError("", e.prefix() + errMsg);
            return result;
        } catch (InvalidFormatException e) {
            String errMsg = "invalid file format: " + PntlInfo.PNTL_CONF  + e.getLocalizedMessage();
            result.addError("", e.prefix() + errMsg);
            return result;
        } catch (ClientException e){
            String errMsg = "cmd to start agent failed, " + e.getMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
        }
        return result;
    }

    public Result<String> notifyAgentConf(PntlConfig config){
        Result<String> result = new Result<>();
        if (hostList == null){
            result.addError("", "host is null");
            return result;
        }

        result = sendMsgToAgents();
        if (!result.isSuccess()){
            LOG.error("notifyAgentConf failed");
        }
        return result;
    }


    private Result<String> stopAgentProbe(String probe_period){
        Result<String> result = new Result<>();
        try {
            Map<String, Object> dataObj = (Map<String, Object>) YamlUtil.getConf(PntlInfo.PNTL_CONF);
            dataObj.put("probe_period", probe_period);
            YamlUtil.setConf(dataObj, PntlInfo.PNTL_CONF);
        } catch (ApplicationException e) {
            String errMsg = "set config [" + PntlInfo.PNTL_CONF + "] failed : " + e.getLocalizedMessage();
            result.addError("", e.prefix() + errMsg);
            return result;
        } catch (Exception e){
            result.addError("", "parameter is invalid");
            return result;
        }

        result = sendMsgToAgents();
        if (!result.isSuccess()){
            LOG.error("setProbeInterval failed");
        }
        return result;
    }

    /**
     * 设置探测时间间隔，若为0，则停止探测
     * @return
     */
    public Result<String> setProbeInterval(String timeInterval){
        Result<String> result = new Result<>();

        if (hostList == null){
            result.addError("", "host is null");
            return result;
        }
        LOG.info("Set probe interval:" + timeInterval);
        if (timeInterval.equals("-1")){
            try{
                String token = identityWrapperService.getPntlAccessToken();
                RestResp resp = pntlRequest.exitAgent(hostList, token);
                if (resp.getStatusCode().isError()){
                    result.addError("", "cmd to exit agent failed");
                }
            } catch (ClientException e){
                String errMsg = "cmd to exit agent failed, " + e.getMessage();
                LOG.error(errMsg);
                result.addError("", errMsg);
            }
            return result;
        } else {
            result = stopAgentProbe(timeInterval);
            if (!result.isSuccess()){
                LOG.error("setProbeInterval failed");
            }
        }

        return result;
    }

    private void setFlowCommon(AgentFlowsJson.FlowList flow, String agentIp){
        flow.setUrgent("false");
        flow.setSip(agentIp);
        flow.setIp_protocol("udp");
        flow.setSport_min(32769);
        flow.setSport_max(32868);
        flow.setSport_range(5);
        flow.setDscp(0);

        AgentFlowsJson.FlowList.TopologyTag topologyTag = new AgentFlowsJson.FlowList.TopologyTag();
        topologyTag.setSrcId(0);
        topologyTag.setDstId(0);
        topologyTag.setLevel(1);
        flow.setTopologyTag(topologyTag);
    }

    /**
     * 生成agent对应的json格式
     * @param config
     * @return
     */
    private AgentFlowsJson generateAgentFlowJson(PingListRequest config){
        AgentFlowsJson agentFlowsJson = new AgentFlowsJson();
        Map<String, List<String>> pingMeshMap = null;

        if (agentIpMap == null){
            return null;
        }
        if (hostList == null){
            LOG.error("no host");
            return null;
        }
        String agentIp = config.getContent().getAgentIp();
         for (PntlHostContext host : hostList){
            if (host.getAgentIp().equals(agentIp)){
                pingMeshMap = host.getPingMeshList();
            }
        }
        if (pingMeshMap == null || pingMeshMap.get(agentIp) == null){
            return null;
        }

        List<AgentFlowsJson.FlowList> flowList = new ArrayList<>();
        for(String dstIp : pingMeshMap.get(agentIp)){
            AgentFlowsJson.FlowList flow = new AgentFlowsJson.FlowList();
            flow.setDip(dstIp);
            setFlowCommon(flow, agentIp);
            flowList.add(flow);
        }

        if (!flowList.isEmpty()){
            agentFlowsJson.setList(flowList);
        }

        return agentFlowsJson;
    }

    private void resetAgentStatus(List<PntlHostContext> hosts){
        if (hosts == null){
            return;
        }
        for (PntlHostContext h : hosts){
            h.setReason(null);
            h.setAgentStatus(null);
        }
    }

    private void sendFilesToAgentsTask(List<PntlHostContext> hosts, int funFlags){
        Runnable scriptSendTask = new Runnable() {
            @Override
            public void run() {
                Result<String> result = new Result<String>();
                try {
                    String token = identityWrapperService.getPntlAccessToken();
                    switch (funFlags) {
                        case 0:
                            result = pntlRequest.sendInstallFilesToAgents(hosts, token);
                        case 1:
                            result = pntlRequest.sendPntlConfigToAgents(hosts, token);
                        default:
                            break;
                    }
                } catch (ClientException e){
                    String errMsg = "Send files to agents failed: " + e.getMessage();
                    LOG.error(errMsg);
                    result.addError("", errMsg);
                }
            }
        };
        resultService.execute(scriptSendTask);
    }

    private Result<String> waitMoment(){
        Result<String> result = new Result<>();
        //wait 30s
        for (int i = 0; i < Constants.PNTL_WAIT_NUM; i++) {
            try {
                Thread.sleep(Constants.PNTL_WATI_TIME);
            } catch (InterruptedException e) {
                LOG.warn("ignore : interrupted sleep");
                result.addError("", "ignore : interrupted sleep");
                return result;
            }
        }
        return result;
    }

    private int getFailAgentCount(List<PntlHostContext> hosts){
        int failAgentCount = 0;
        /*若所有的host均失败，则不需要install，直接返回*/
        for (PntlHostContext host : hosts){
            if (host.getAgentStatus() == null){
                continue;
            }

            if (host.getAgentStatus().equals(PntlInfo.PNTL_AGENT_STATUS_FAIL)){
                failAgentCount++;
            }
        }
        return failAgentCount;
    }

    /*
     *1. 上传到仓库
     *2. 分发到agent
     *3. 安装
     */
    private Result<String> installStartAgent(List<PntlHostContext> hosts) throws ClientException{
        Result<String> result = new Result<String>();

        if (hosts == null || hosts.isEmpty()){
            result.addError("", "no host info");
            return result;
        }
        /*初始话agent状态，去除上次安装结果*/
        resetAgentStatus(hosts);
        sendFilesToAgentsTask(hosts, 0);

        result = waitMoment();
        if (!result.isSuccess()){
            return result;
        }

        int failAgentCount = getFailAgentCount(hosts);
        if (failAgentCount == hosts.size()){
            result.addError("", "send files to agent fail");
            return result;
        }

        LOG.info("send files to agents ok, begin to install");
        try{
            result = installAgent(hosts);
        } catch(ClientException e){
            result.addError("", e.getMessage());
            throw new ClientException(ExceptionType.SERVER_ERR, e.getMessage());
        }

        if (result.isSuccess() && failAgentCount != 0){
            result.addError("", "some agents[" + failAgentCount+ "] install failed");
        }

        return result;
    }

    public Result<String> initHostList(){
        Result<String> result = new Result<String>();
        try {
            hostList = readFileHostList(PntlInfo.PNTL_IPLIST_CONF);
        } catch (Exception e){
            LOG.error("get host list failed:" + e.getMessage());
            result.addError("", "get host list failed:" + e.getMessage());
            return result;
        }

        return result;
    }

    private void startMonitor(){
         /* 启动轮询监控*/
        Runnable monitorPntlWarnTask = new Runnable() {
            @Override
            public void run() {
                monitorPntlWarn();
            }
        };
        resultService.execute(monitorPntlWarnTask);

        Runnable monitorKafkaTask = new Runnable() {
            @Override
            public void run() {
                monitorKafka();
            }
        };
        resultService.execute(monitorKafkaTask);
    }

    private void handleLossRateMsg(Object obj)  throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = objectMapper.writeValueAsString(obj);
        LossRateAgent lossRate = objectMapper.readValue(jsonStr, LossRateAgent.class);
        Result<String> result = pntlWarnService.saveLossRateData(lossRate);
        if (!result.isSuccess()){
            LOG.error("save lossRate failed");
        }
    }

    private void handleDelayInfoMsg(Object obj)  throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = objectMapper.writeValueAsString(obj);
        DelayInfoAgent delay = objectMapper.readValue(jsonStr, DelayInfoAgent.class);
        Result<String> result = pntlWarnService.saveDelayInfoData(delay);
        if (!result.isSuccess()){
            LOG.error("save delay failed");
        }
    }

    private void handleAgentIpMsg(Object obj) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = objectMapper.writeValueAsString(obj);
        AgentIp ip = objectMapper.readValue(jsonStr, AgentIp.class);
        Result<String> result = saveVbondIp(ip.getAgentIp(), ip.getVbondIp());
        if (!result.isSuccess()){
            LOG.error("save vbond ip failed");
        }
    }

    private void handleKafkaMsg(KafkaRsp.Data data) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonstr = objectMapper.writeValueAsString(data);
        KafkaRsp.Data value = objectMapper.readValue(jsonstr, KafkaRsp.Data.class);
        Object obj = value.getValue();
        System.out.println(objectMapper.writeValueAsString(obj));
        JsonNode objNode = objectMapper.readTree(jsonstr).get("value");
        if (objNode.findValue("orgnizationSignature") != null){
            String orgSig = objNode.findValue("orgnizationSignature").toString();
            if (orgSig.contains("HuaweiDC3ServerAntsDropNotice")){
                handleLossRateMsg(obj);
            } else if (orgSig.contains("HuaweiDC3ServerAntsFull")){
                handleDelayInfoMsg(obj);
            }
        } else if (objNode.findValue("vbond_ip") != null){
            handleAgentIpMsg(obj);
        }
    }

    /**
     * 实时监控kafka消息
     */
    private void monitorKafka(){
        //noinspection InfiniteLoopStatement
        while (true){
            KafkaRsp msg = pntlRequest.getKafkaMsg();
            if (null == msg){
                continue;
            }

            for (KafkaRsp.Data data : msg.getData()) {
                try {
                    handleKafkaMsg(data);
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private Result<String> sendMsgToAgents(){
        Result<String> result = new Result<>();

        //1. 写信息到文件
        result = pntlConfigService.writeInfoToAgentConf(PntlInfo.AGENT_CONF);
        if (!result.isSuccess()){
            LOG.error(result.getErrorMessage());
            return result;
        }

        //2.上传文件到仓库
        result = pntlConfigService.uploadFilesToDFS(PntlInfo.AGENT_CONF);
        if (!result.isSuccess()){
            LOG.error("upload files to dfs failed," + result.getErrorMessage());
            return result;
        }

        //3.将文件从仓库分发到agents
        sendFilesToAgentsTask(hostList, 1);
        return result;
    }
    /**
     * 通知agent，表示server已启动
     */
    private void notifyAgentServerStart(){
        CommonInfo.setServerStart("1");
        Result<String> result = sendMsgToAgents();
        if (!result.isSuccess()){
            LOG.error("notifyAgentServerStart failed");
        }
        //通知后，恢复标记,避免反复通知使agent不断上报VbondIp
        CommonInfo.setServerStart("0");
        System.out.println("notify agent start, server start=" + CommonInfo.getServerStart());
    }

    private Result<String> initPntlConfig(){
        Result<String> result = new Result<String>();
        //read config.yml
        Result<PntlConfig> pntlConfig = pntlConfigService.getPntlConfig();
        if (!pntlConfig.isSuccess()){
            LOG.error("get pntlConfig failed");
            result.addError("", "get pntlConfig failed");
            return result;
        }
        LossRate.setLossRateThreshold(Integer.valueOf(pntlConfig.getModel().getLossRateThreshold()));
        DelayInfo.setDelayThreshold(Long.valueOf(pntlConfig.getModel().getDelayThreshold()));
        CommonInfo.setRepoUrl(pntlConfig.getModel().getRepoUrl());
        CommonInfo.setReportPeriod(Integer.valueOf(pntlConfig.getModel().getReportPeriod()));
        CommonInfo.setKafkaIp(pntlConfig.getModel().getKafkaIp());
        CommonInfo.setTopic(pntlConfig.getModel().getTopic());
        /*恢复仓库地址到内存*/
        Pntl.setDownloadUrl(pntlConfig.getModel().getEulerRepoUrl());
        Pntl.setDownloadUrl(pntlConfig.getModel().getSuseRepoUrl());
        Pntl.setDownloadUrl(pntlConfig.getModel().getInstallScriptRepoUrl());
        return result;
    }

    /**
     * 初始化配置，下发脚本，学习网络拓扑
     * @return
     * @throws ClientException
     */
    public Result<String> initPntl() throws ClientException {
        Result<String> result = new Result<String>();

        result = initPntlConfig();
        if (!result.isSuccess()){
            LOG.error("get pntlConfig failed");
            result.addError("", "get pntlConfig failed");
            return result;
        }

        result = initHostList();
        if (!result.isSuccess()){
            String errMsg = result.getErrorMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
            return result;
        }

        //自动将安装脚本上传到仓库
        result = pntlConfigService.uploadFilesToDFS(PntlInfo.AGENT_INSTALL_FILENAME);
        if (!result.isSuccess()){
            LOG.error("upload install_pntl.sh to dfs failed," + result.getErrorMessage());
            return result;
        }

        startMonitor();

        notifyAgentServerStart();
        LOG.info("Init host list and pntlConfig success");

        return  result;
    }

    /**
     * 监控当前告警，对于长时间没有上报的告警，设置老化时间，超时则删除
     * 每5min一个周期
     */
    private void monitorPntlWarn(){
        Long count = 0L;
        int reportPeriod = CommonInfo.getReportPeriod(); /* second */
        int refleshPeriod = reportPeriod + PntlInfo.MONITOR_INTERVAL_TIME_NEWEST; /* add 5min */
        while (true) {
            LossRate.refleshLossRateWarning();
            DelayInfo.refleshDelayInfoWarning();

            /* 7 days */
            if (count % (PntlInfo.MONITOR_INTERVAL_TIME_HISTORY) == 0) {
                try {
                    PntlWarning.refleshHistoryWarning();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(refleshPeriod * 1000);
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解压、安装agent包
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    private Result<String> installAgent(List<PntlHostContext> pntlHostList) throws ClientException{
        Result<String> result = new Result<>();
        RestResp resp = null;
        try{
            String token = identityWrapperService.getPntlAccessToken();
            resp = pntlRequest.installAgent(pntlHostList, token);
            if (resp.getStatusCode().isError()){
                result.addError("", "install agents failed:" + resp.getStatusCode());
            } else if ((Integer) resp.getRespBody().get("code") != 0){
                int code = (Integer) resp.getRespBody().get("code");
                String errMsg = "code is " + code;
                if (code != 1000 && code != 2000){
                    errMsg += resp.getRespBody().get("reason").toString();
                } else {
                    errMsg += resp.getRespBody().get("result").toString();
                }
                result.addError(String.valueOf(resp.getRespBody().get("code")), errMsg);
            }
        } catch (ClientException e){
            String errMsg = "Send ip list to agents failed, " + e.getMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
        }

        return result;
    }

    /**
     * get hosts'ip from CMDB
     * @return
     */
    private List<PntlHostContext> getHostsInfo() throws ApplicationException, ClientException {
        List<PntlHostContext> hostsList = new ArrayList<PntlHostContext>();
        String accessToken = identityWrapperService.getPntlAccessToken();

        HostInfo hostInfo = null;
        try{
            hostInfo = pntlRequest.getHostsList(accessToken);
        } catch (ClientException e){
            LOG.error("Get hosts info list failed from cmdb" + e.getMessage());
            throw new ClientException(e.getType(), e.getMessage());
        }

        if (null == hostInfo || null == hostInfo.getHostsInfoList()){
            String errMsg = "hosts info is null";
            LOG.error(errMsg);
            throw new ApplicationException(ExceptionType.REMOTE_EXE_ERR, errMsg);
        }

        for (HostInfo.HostListInfo host : hostInfo.getHostsInfoList()){
            if (null == host){
                continue;
            }
            PntlHostContext hostContext = new PntlHostContext();
            hostContext.setId(host.getId());
            hostContext.setAgentIp(host.getIp());
            hostContext.setAgentStatus(host.getAgentStatus());
            hostContext.setAgentSN(host.getAgentSN());
            hostContext.setOs(host.getOs());
            hostContext.setPodId(host.getPodId());
            hostContext.setZoneId(host.getZoneId());
            hostsList.add(hostContext);
        }

        return hostsList;
    }

    private String getPath(String confFile) {
        String directory = FileUtil.getResourcePath();
        final String separator = System.lineSeparator();
        if (separator.equals("\\") && directory.startsWith("/")) {
            directory = directory.substring(1);
        }
        return directory + confFile;
    }

    private List<PntlHostContext> readFileHostList(String filename) throws Exception, InvalidFormatException{
        List<PntlHostContext> hostsList = new ArrayList<PntlHostContext>();

        Map<String, PntlHostInfo> data = (Map<String, PntlHostInfo>) YamlUtil.getConf(filename);
        List<Map<String, String>> ipList = (List<Map<String, String>>) data.get("host");
        for (int i = 0; i < ipList.size(); i++) {
            PntlHostContext host = new PntlHostContext();
            String ip = ipList.get(i).get("ip");
            String os = ipList.get(i).get("os");
            String az = ipList.get(i).get("az");
            String pod = ipList.get(i).get("pod");
            if (az == null || pod == null || !checkIpValid(ip)){
                String errMsg = "ipList.yml format is invalid";
                LOG.error(errMsg);
                throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
            }

            if (StringUtils.isEmpty(os) ||
                    (!PntlInfo.OS_EULER.equalsIgnoreCase(os) && !PntlInfo.OS_SUSE.equalsIgnoreCase(os))){
                String errMsg = "os is error in ipList.yml";
                LOG.error(errMsg);
                throw new InvalidFormatException(ExceptionType.CLIENT_ERR, errMsg);
            }

            host.setAgentIp(ip);
            host.setOs(os);
            host.setZoneId(az);
            host.setPodId(pod);
            try{
                String sn = Pntl.getAgentSnByIp(ip);
                host.setAgentSN(sn);
            } catch (ClientException e){
                /* 获取sn异常，不退出 */
                LOG.error("Get sn fail: " + e.getMessage());
            }

            hostsList.add(host);
        }
        return hostsList;
    }

    /**
     *  生成主机列表
     * @return
     */
    private List<PntlHostContext> genProbeHostList() throws ApplicationException, ClientException {

        String errMsg = null;

        List<PntlHostContext> pntlHostList = getHostsInfo();
        if (null == pntlHostList || 0 == pntlHostList.size()){
            errMsg = "get host info failed";
            LOG.error(errMsg);
            throw new ApplicationException(ExceptionType.SERVER_ERR, errMsg);
        }
        return pntlHostList;
    }

    /**
     * 获取ip列表
     * @param azId
     * @param podId
     * @return
     */
    public Result<IpListJson> getIpListinfo(String azId, String podId){
        Result<IpListJson> result = new Result<>();

        IpListJson ipListInfo = new IpListJson();
        ipListInfo.setAzId(azId);
        ipListInfo.setPodId(podId);
        if (agentIpMap == null || agentIpMap.isEmpty()){
            result.setModel(ipListInfo);
            //result.addError("", "no host list");
            return result;
        }

        List<IpListJson.IpList> ipList = new ArrayList<>();
        for (String agentIp : agentIpMap.keySet()){
            IpListJson.IpList ip = new IpListJson.IpList();
            ip.setIp(agentIpMap.get(agentIp));
            ipList.add(ip);
        }

        ipListInfo.setList(ipList);
        result.setModel(ipListInfo);

        return result;
    }

    private void appendToHostListMem(List<PntlHostContext> updateHostsList){
        if (hostList == null){
            return;
        }
        hostList.addAll(updateHostsList);
    }

    /**
     *将新增加的ip追加到ipList中
     * @param updateHostsList
     * @throws ApplicationException
     */
    private void appendIpListConfig(List<PntlHostContext> updateHostsList) throws ApplicationException {
        try {
            filterDuplicateHosts(hostList, updateHostsList);
            List<Map<String, String>> data = new PntlHostContext().convertToMap(updateHostsList);
            YamlUtil.appendConf(data, PntlInfo.PNTL_IPLIST_CONF);
            appendToHostListMem(updateHostsList);
        } catch (ApplicationException e) {
            throw new ApplicationException(ExceptionType.CLIENT_ERR, e.getMessage());
        }
    }

    private void updateIpListConfig() throws ApplicationException{
        Map<String, List<Map<String, String>>> data = new HashMap<>();
        List<Map<String, String>> list = new ArrayList<>();
        if (hostList == null || hostList.isEmpty()){
            return;
        }
        for (PntlHostContext h : hostList){
            Map<String, String> d = new HashMap<>();
            d.put("ip", h.getAgentIp());
            d.put("pod", h.getPodId());
            d.put("az", h.getZoneId());
            d.put("os", h.getOs());
            list.add(d);
        }
        data.put("host", list);
        try {
            YamlUtil.setConf(data, PntlInfo.PNTL_IPLIST_CONF);
        } catch (ApplicationException e) {
            throw new ApplicationException(ExceptionType.CLIENT_ERR, e.getMessage());
        }
    }

    /**
     * 刷新hostList数据，包括agentIp,pingMesh，以及在其他host中的vbond信息
     * @param delHostsList
     */
    private void removeHostsList(List<PntlHostContext> delHostsList){
        if (hostList == null || hostList.isEmpty()){
            return;
        }
        hostList.removeAll(delHostsList);

        if (agentIpMap == null || agentIpMap.isEmpty()){
            return;
        }
        for (PntlHostContext host : delHostsList){
            String vbondIp = agentIpMap.get(host.getAgentIp());
            if (vbondIp == null){
                continue;
            }
            for (PntlHostContext h : hostList){
                Map<String, List<String>> mesh = h.getPingMeshList();
                mesh.get(h.getAgentIp()).remove(vbondIp);
            }
        }
    }

    /**
     * 将ip从ipList中去除
     * @param updateHostsList
     * @return
     * @throws ApplicationException
     */
    private List<PntlHostContext> delIpListConfig(List<PntlHostContext> updateHostsList)
        throws ApplicationException{
        List<PntlHostContext> delHostsList = new ArrayList<>();
        if (hostList == null){
            return null;
        }
        for (PntlHostContext host : updateHostsList){
            if (hostList.contains(host)){
                PntlHostContext h = new PntlHostContext();
                h.setAgentIp(host.getAgentIp());
                h.setZoneId(host.getZoneId());
                h.setPodId(host.getPodId());
                h.setOs(host.getOs());
                h.setAgentSN(host.getAgentSN());
                delHostsList.add(h);
            }
        }

        if (delHostsList.size() == 0){
            return delHostsList;
        }

        removeHostsList(delHostsList);
        try {
            updateIpListConfig();
        } catch (ApplicationException e){
            throw new ApplicationException("", e.getMessage());
        }

        return delHostsList;
    }

    /**
     * 停止相关agent
     * @param delHostsList
     */
    private Result<String> stopAgent(List<PntlHostContext> delHostsList){
        Result<String> result = new Result<>();
        if (delHostsList == null || delHostsList.isEmpty()){
            return result;
        }
        try{
            String token = identityWrapperService.getPntlAccessToken();
            RestResp resp = pntlRequest.exitAgent(delHostsList, token);
            if (resp.getStatusCode().isError()){
                result.addError("", "cmd to exit agent failed");
            }
        } catch (ClientException e){
            String errMsg = "cmd to exit agent failed, " + e.getMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
        }
        return result;
    }

    private void updateAgentIpMap(List<PntlHostContext> hosts){
        if (null == hosts || hosts.isEmpty() || null == agentIpMap || agentIpMap.isEmpty()){
            return;
        }
        for (PntlHostContext h : hosts){
            if (agentIpMap.get(h.getAgentIp()) != null){
                agentIpMap.remove(h.getAgentIp());
            }
        }
    }

    /**
     * 校验ip合理性
     * @param ip
     * @return true:ok, false: failed
     */
    private boolean checkIpValid(String ip){
        Pattern pattern = Pattern
                .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]"
                        + "|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
        return !(ip == null || (!ip.isEmpty() && !pattern.matcher(ip).matches()));
    }

    /**
     * 校验文件内容，ip符合规范，os，az，pod不能为空
     * @param updateHostsList
     * @return
     */
    private boolean validUpdateIpListConf(List<PntlHostContext> updateHostsList){
        if (updateHostsList.size() == 0){
            return false;
        }
        for (PntlHostContext host : updateHostsList){
            if (!checkIpValid(host.getAgentIp())){
                return false;
            }
            if (StringUtils.isEmpty(host.getOs()) || StringUtils.isEmpty(host.getZoneId())
                    || StringUtils.isEmpty(host.getPodId())){
                return false;
            }
        }
        return true;
    }

    /**
     * 判断updateHostsList中host是否和hostList重复，若有，则去重
     * @param hostList
     * @param updateHostsList
     */
    private void filterDuplicateHosts(List<PntlHostContext> hostList, List<PntlHostContext> updateHostsList){
        if (hostList == null || hostList.isEmpty() || updateHostsList == null || updateHostsList.isEmpty()){
            return;
        }

        Iterator<PntlHostContext> it = updateHostsList.iterator();
        while (it.hasNext()){
            PntlHostContext h = it.next();
            for (PntlHostContext host : hostList){
                if (host.equals(h)){
                    it.remove();
                    break;
                }
            }
        }
    }

    /**
     * 更新agent，读取上传的配置文件，先刷新ipList，再刷新agent
     * @return
     */
    public Result<String> updateAgents(String type){
        Result<String> result = new Result<>();

        List<PntlHostContext> updateHostsList;
        try {
            updateHostsList = readFileHostList(PntlInfo.PNTL_UPDATE_IPLIST_CONFIG);
        } catch (Exception e){
            String errMsg = "read update host failed:" + e.getMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
            return result;
        }

        if (!validUpdateIpListConf(updateHostsList)){
            result.addError("", "content in update ipList is invalid");
        }

        try {
            if (PntlInfo.PNTL_UPDATE_TYPE_ADD.equals(type)) {
                result = installStartAgent(updateHostsList);
                if (!result.isSuccess()){
                    LOG.error("install and start agent failed:" + result.getErrorMessage());
                    return result;
                }
                appendIpListConfig(updateHostsList);
            } else if (PntlInfo.PNTL_UPDATE_TYPE_DEL.equals(type)) {
                /*
                * 1. 更新hostList(pingMesh, pingList),ipList.yml
                * 2. 通知agent获取pingMesh
                * 3. 更新agentIpMap(ipList)
                * 4. stop agent
                * */
                List<PntlHostContext> delHostsList = delIpListConfig(updateHostsList);
                result = notfiyAllAgentsToGetPingList(hostList);
                if (!result.isSuccess()){
                    LOG.error(result.getErrorMessage());
                }

                updateAgentIpMap(delHostsList);
                result = stopAgent(delHostsList);
                if (!result.isSuccess()){
                    LOG.error("stop agent failed:" + result.getErrorMessage());
                }
            }
        } catch (ApplicationException | ClientException e){
            String errMsg = "Update ipList failed:" + e.getMessage();
            result.addError("", errMsg);
            return result;
        }
        return result;
    }

    public Result<Map<String, List<String>>> getPingList(){
        Result<Map<String, List<String>>> result = new Result<>();
        Map<String, List<String>> list = new HashMap<>();
        if (hostList == null || hostList.isEmpty()){
            result.addError("", "hostlist is null");
            return result;
        }
        for(PntlHostContext host : hostList){
            if (host.getPingMeshList() != null) {
                list.putAll(host.getPingMeshList());
            }
        }

        result.setModel(list);
        return result;
    }

    /**
     * 判断ip是否已配置
     * @param ip
     * @return
     */
    public boolean checkIpIsExist(String ip){
        if (StringUtils.isEmpty(ip)){
            return false;
        }

        boolean hasIp = false;
        for (PntlHostContext host : hostList){
            if (ip.equals(host.getVbondIp())){
                hasIp = true;
                break;
            }
        }
        return hasIp;
    }
}
