package com.huawei.blackhole.network.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huawei.blackhole.network.api.bean.*;
import com.huawei.blackhole.network.api.resource.PntlShareInfo;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.utils.FileUtil;
import com.huawei.blackhole.network.common.utils.YamlUtil;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.*;
import com.huawei.blackhole.network.extention.service.pntl.Pntl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.ws.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

@Service("pntlService")
public class PntlServiceImpl extends  BaseRouterService implements PntlService{
    private static final Logger LOG = LoggerFactory.getLogger(PntlServiceImpl.class);
    private List<PntlHostContext> hostList = null;
    private Map<String, String> agentIpMap = null;
    private static boolean notifyAgentToGetPingList = false;

    /**
     * 首次启动
     * @return
     */
    public Result<String> startPntl() {
        Result<String> result = new Result<>();
        String errMsg = null;

        /*临时方案，从配置文件获取iplist*/
        try {
            hostList = readFileHostList();
        } catch (Exception e){
            LOG.error("get host list failed:" + e.getMessage());
            result.addError("", "get host list failed:" + e.getMessage());
            return result;
        }
        LOG.info("Get host list finish");
        /* 获取主机列表 */
        /*
        try {
            hostList = genProbeHostList();
        } catch (Exception e){
            LOG.error("get probe host list failed:" + e.getMessage());
            result.addError("", "get probe host list failed:" + e.getMessage());
            return result;
        }
*/
        /* 初始化配置 */
        try {
           result = initPntlConfig(hostList);
        } catch (Exception e){
            LOG.error(e.getMessage());
            result.addError("", e.getMessage());
            return result;
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
                hostList = readFileHostList();
            } catch (Exception e){
                LOG.error("get host list failed:" + e.getMessage());
            }
        }
        LOG.info("Set probe interval:" + timeInterval);
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
                hostList = readFileHostList();
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
    private Result<String> installStartAgent() throws ClientException{
        final PntlShareInfo pntlInfo = new PntlShareInfo();
        Result<String> result = new Result<String>();
        Runnable scriptSendTask = new Runnable() {
            @Override
            public void run() {
                Result<String> result = new Result<String>();
                try {
                    String token = identityWrapperService.getPntlAccessToken();
                    result = pntlRequest.sendFilesToAgents(hostList, token);
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
            installAgent(hostList);
        } catch(ClientException e){
            result.addError("", e.getMessage());
            throw new ClientException(ExceptionType.SERVER_ERR, e.getMessage());
        }

        return result;
    }
    /**
     * 初始化配置，下发脚本，学习网络拓扑
     * @param hostList
     * @return
     * @throws ClientException
     */
    private Result<String> initPntlConfig(List<PntlHostContext> hostList) throws ClientException {
        Result<String> result = new Result<String>();

        try {
            result = installStartAgent();
        } catch (ClientException e){
            result.setErrorMessage("Install and start agent failed:" + e.getMessage());
            LOG.error("Install and start agent failed: " + e.getMessage());
        }

        /*发送主机ip列表到agent，进行traceroute学习*/
        /*
        try{
            pntlRequest.sendIpListToAgents(hostList);
        } catch (ClientException e){
            LOG.error("Send ip list to agents failed, " + e.getMessage());
            result.addError("", e.toString());
        }*/

        /*通过traceroute获得网络拓扑*/
        Runnable getNetworkMapTask = new Runnable() {
            @Override
            public void run() {
                Result<String> result = new Result<String>();
                try{
                    genNetworkMap(hostList);
                } catch (ClientException e){
                    LOG.error("Generate network map failed" + e.getMessage());
                    String msg = e.toString();
                    result.addError("", msg);
                }
            }
        };
       // resultService.execute(getNetworkMapTask);

         /* 启动轮询监控*/
        Runnable monitorTask = new Runnable() {
            @Override
            public void run() {
                    monitorPntl();
            }
        };
        resultService.execute(monitorTask);

        return  result;
    }

    /**
     * 监控当前告警，对于长时间没有上报的告警，设置老化时间，超时则删除
     */
    private void monitorPntl(){
        while (true){
            LossRate.refleshLossRateWarning();
            DelayInfo.refleshDelayInfoWarning();
            try{
                Thread.sleep(PntlInfo.MONITOR_INTERVAL_TIME * 1000);//second
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

    private List<PntlHostContext> readFileHostList() throws Exception{
        List<PntlHostContext> hostsList = new ArrayList<PntlHostContext>();

        Map<String, PntlHostInfo> data = (Map<String, PntlHostInfo>) YamlUtil.getConf(Resource.PNTL_IPLIST_CONF);
        List<Map<String, String>> ipList = (List<Map<String, String>>) data.get("host");
        for (int i = 0; i < ipList.size(); i++) {
            PntlHostContext host = new PntlHostContext();
            String ip = (String)ipList.get(i).get("ip");
            host.setAgentIp(ip);
            host.setOs((String)ipList.get(i).get("os"));
            host.setZoneId((String)ipList.get(i).get("az"));
            host.setPodId((String)ipList.get(i).get("pod"));
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
        /*
        hostList = new ArrayList<PntlHostContext>();
        List<HostInfo.HostListInfo> hosts = new ArrayList<>();
        int ip = 152;
        for (int i = 0; i < 40; i++){
            HostInfo.HostListInfo host = new HostInfo.HostListInfo();
            host.setIp("100.109.253." + String.valueOf(ip));
            hosts.add(host);

            PntlHostContext tmpHost = new PntlHostContext();
            tmpHost.setIp("100.109.253." + String.valueOf(ip));
            hostList.add(tmpHost);
            ip++;
        }
        hostList.get(0).setPingMeshList("100.109.253.152", hosts);
        return hostList;
*/
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
}
