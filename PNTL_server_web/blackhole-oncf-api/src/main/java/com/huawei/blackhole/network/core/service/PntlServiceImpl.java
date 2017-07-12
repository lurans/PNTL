package com.huawei.blackhole.network.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huawei.blackhole.network.api.bean.*;
import com.huawei.blackhole.network.api.resource.PntlShareInfo;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.InvalidFormatException;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.*;
import com.huawei.blackhole.network.extention.service.conf.PntlConfigService;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    /**
     * 首次启动
     * @return
     */
    public Result<String> startPntl() {
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
     * 保存业务ip，生成pingMesh
     * @param agentIp
     * @param vbondIp
     * @return
     */
    public Result<String> saveAgentIp(String agentIp, String vbondIp){
        Result<String> result = new Result<>();
        if (agentIpMap == null){
            agentIpMap = new HashMap<>();
        }

        if (agentIpMap.get(agentIp) != null && agentIpMap.get(agentIp).equals(vbondIp)){
            return result;
        }
        agentIpMap.put(agentIp, vbondIp);
        addVbondIpToHostList(agentIp, vbondIp);

        if (notifyAgentToGetPingList == false){
            try {
                Thread.sleep(PntlInfo.NOTIFY_AGENT_TO_GET_PINGLIST_TIME * 1000);//second
            } catch(InterruptedException e){
                result.addError("", "Sleep error " + e.getMessage());
            }
        }
        try {
            RestResp resp = pntlRequest.notifyAgentToGetPingList(agentIp);
            if (resp.getStatusCode().isError()){
                result.addError("", "Notify agent to get pingList failed");
            } else{
                notifyAgentToGetPingList = true;
            }
        } catch (ClientException e){
            String errMsg = "Notify agent to get pingList failed:" + e.getMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
        }
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
        for (PntlHostContext host : hostList){
            host.setPingMeshList(host.getAgentIp(), hostList);
        }
    }
    /**
     * 设置探测时间间隔，若为0，则停止探测
     * @return
     */
    public Result<String> setProbeInterval(String timeInterval){
        Result<String> result = new Result<>();
        ProbeInterval interval = new ProbeInterval();
        interval.setProbe_interval(timeInterval);

        if (hostList == null || hostList.size() == 0){
            try {
                hostList = readFileHostList(PntlInfo.PNTL_IPLIST_CONF);
            } catch (Exception e){
                LOG.error("get host list failed:" + e.getMessage());
            }
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
                LOG.error("cmd to exit agent failed, " + e.getMessage());
                result.addError("", e.toString());
            }
            return result;
        }
        for (int i = 0; i < hostList.size(); i++){
            String agentIp = hostList.get(i).getAgentIp();
            try {
                RestResp resp = pntlRequest.sendProbeInterval(agentIp, interval);
                if (resp.getStatusCode().isError()){
                    LOG.error("stop probe failed[" + agentIp + "]");
                    result.addError("", "stop probe failed[" + agentIp + "]");
                }
            } catch (ClientException | JsonProcessingException e){
                LOG.error("stop probe failed[" + agentIp + "] " + e.getMessage());
                result.addError("", "stop probe failed[" + agentIp + "] "+ e.getMessage());
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
            try {
                hostList = readFileHostList(PntlInfo.PNTL_IPLIST_CONF);
            } catch (Exception e){
                LOG.error("get host list failed:" + e.getMessage());
            }
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
        agentFlowsJson.setList(flowList);

        return agentFlowsJson;
    }

    /*
     *1. 上传到仓库
     *2. 分发到agent
     *3. 安装
     */
    private Result<String> installStartAgent(List<PntlHostContext> hosts) throws ClientException{
        if (hostList == null || hostList.size() == 0){
            try {
                hostList = readFileHostList(PntlInfo.PNTL_IPLIST_CONF);
            } catch (Exception e){
                LOG.error("get host list failed:" + e.getMessage());
            }
        }

        final PntlShareInfo pntlInfo = new PntlShareInfo();
        Result<String> result = new Result<String>();
        Runnable scriptSendTask = new Runnable() {
            @Override
            public void run() {
                Result<String> result = new Result<String>();
                try {
                    String token = identityWrapperService.getPntlAccessToken();
                    result = pntlRequest.sendFilesToAgents(hosts, token);
                    if (result.isSuccess()) {
                        pntlInfo.setSendSuccess(true);
                    } else {
                        pntlInfo.setErrMsg(result.getErrorMessage());
                    }
                } catch (ClientException e){
                    LOG.error("Send files to agents failed, " + e.getMessage());
                    String msg = e.toString();
                    result.addError("", msg);
                }
            }
        };
        resultService.execute(scriptSendTask);

        //wait 30s
        for (int i = 0; i < Constants.PNTL_WAIT_NUM; i++) {
            if (pntlInfo.isSendSuccess()) {
                break;
            }
            try {
                Thread.sleep(Constants.PNTL_WATI_TIME);
            } catch (InterruptedException e) {
                LOG.warn("ignore : interrupted sleep");
            }
        }

        LOG.info("agent upload ok, begin to install");
        try{
            if (!pntlInfo.isSendSuccess()){
                result.setErrorMessage(pntlInfo.getErrMsg());
                result.setSuccess(false);
                return result;
            }
            installAgent(hosts);
        } catch(ClientException e){
            result.addError("", e.getMessage());
            throw new ClientException(ExceptionType.SERVER_ERR, e.getMessage());
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
        Runnable monitorPntlNewestWarnTask = new Runnable() {
            @Override
            public void run() {
                monitorPntlNewestWarn();
            }
        };
        resultService.execute(monitorPntlNewestWarnTask);

        Runnable monitorPntlHistoryWarnTask = new Runnable() {
            @Override
            public void run() {
                monitorPntlHistoryWarn();
            }
        };
        resultService.execute(monitorPntlHistoryWarnTask);
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
        return result;
    }

    /**
     * 初始化配置，下发脚本，学习网络拓扑
     * @return
     * @throws ClientException
     */
    public Result<String> initPntl() throws ClientException {
        Result<String> result = new Result<String>();

        result = initHostList();
        if (!result.isSuccess()){
            String errMsg = result.getErrorMessage();
            LOG.error(errMsg);
            result.addError("", errMsg);
            return result;
        }

        result = initPntlConfig();
        if (!result.isSuccess()){
            LOG.error("get pntlConfig failed");
            result.addError("", "get pntlConfig failed");
            return result;
        }

        startMonitor();
        LOG.info("Init host list and pntlConfig success");

        return  result;
    }

    /**
     * 监控当前告警，对于长时间没有上报的告警，设置老化时间，超时则删除
     */
    private void monitorPntlNewestWarn(){
        while (true){
            LossRate.refleshLossRateWarning();
            DelayInfo.refleshDelayInfoWarning();
            try{
                Thread.sleep(PntlInfo.MONITOR_INTERVAL_TIME_NEWEST * 1000);//second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void monitorPntlHistoryWarn(){
        while (true){
            try{
                PntlWarning.refleshHistoryWarning();
                Thread.sleep(PntlInfo.MONITOR_INTERVAL_TIME_HISTORY * 1000);//second
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ParseException e) {
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
    public Result<String> installAgent(List<PntlHostContext> pntlHostList) throws ClientException{
        Result<String> result = new Result<>();
        RestResp resp = null;
        try{
            String token = identityWrapperService.getPntlAccessToken();
            resp = pntlRequest.installAgent(hostList, token);
            if (resp.getStatusCode().isError()){
                result.addError("", "install agent failed");
            }
        } catch (ClientException e){
            LOG.error("Send ip list to agents failed, " + e.getMessage());
            result.addError("", e.toString());
        }

        return result;
    }

    /**
     * 通过从CMDB获取的host信息生成网络拓扑，根据pod
     * @param hostList
     * @return
     */
    private Result<String> genNetworkMap(List<PntlHostContext> hostList)
            throws ClientException{
        Result<String> result = new Result<>();
        if (hostList == null){
            return null;
        }

        for (PntlHostContext host : hostList){
            try {
                getTracerouteLog(host);
            } catch (Exception e){

            }
        }
        PntlNetworkMap networkMap = new PntlNetworkMap();

        return result;
    }

    /**
     * 获取traceroute学习结果
     * @param host
     * @return
     */
    public Result<String> getTracerouteLog(PntlHostContext host){
        Result<String> result = new Result<>();

        try {
            pntlRequest.getTracerouteResult(host);
        } catch (ClientException e){
            result.addError("", e.getMessage());
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
            //hostContext.setPingMeshList(host.getIp(), hostInfo.getHostsInfoList());
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
            if (ip == null || os == null || az == null || pod == null || !checkIpValid(ip)){
                String errMsg = "ipList.yml format is invalid";
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
            } catch (Exception e){
                LOG.error("Get sn fail, " + e.getMessage());
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
            result.addError("", "no host list");
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
        if (hostList.isEmpty()){
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
        if (hostList.isEmpty() || hostList.size() == 0){
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
        hostList.removeAll(delHostsList);
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
        for (PntlHostContext host : updateHostsList){
            if (hostList.contains(host)){
                PntlHostContext h = new PntlHostContext();
                h.setAgentIp(host.getAgentIp());
                h.setZoneId(host.getZoneId());
                h.setPodId(host.getPodId());
                h.setOs(host.getOs());
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
        if (delHostsList.isEmpty()){
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
        if (!ip.isEmpty() && !pattern.matcher(ip).matches()){
            return false;
        }
        return true;
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
            if (host.getOs().isEmpty() || host.getZoneId().isEmpty() || host.getPodId().isEmpty()){
                return false;
            }
        }
        return true;
    }
    /**
     * 更新agent，读取上传的配置文件，先刷新ipList，再刷新agent
     * @return
     */
    public Result<String> updateAgents(String type){
        Result<String> result = new Result<>();

        List<PntlHostContext> updateHostsList = new ArrayList<PntlHostContext>();
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
            if (type.equals(PntlInfo.PNTL_UPDATE_TYPE_ADD)) {
                appendIpListConfig(updateHostsList);
                installStartAgent(updateHostsList);
            } else if (type.equals(PntlInfo.PNTL_UPDATE_TYPE_DEL)) {
                /*
                * 1. 更新hostList(pingMesh, pingList),ipList.yml
                * 2. stop agent
                * 3. 更新agentIpMap(ipList)
                * */
                List<PntlHostContext> delHostsList = delIpListConfig(updateHostsList);
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
}
