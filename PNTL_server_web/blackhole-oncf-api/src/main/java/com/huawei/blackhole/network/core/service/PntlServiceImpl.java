package com.huawei.blackhole.network.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.AgentJson;
import com.huawei.blackhole.network.extention.bean.pntl.HostInfo;
import com.huawei.blackhole.network.extention.bean.pntl.PntlNetworkMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Created by y00214328 on 2017/5/19.
 */
@Service("pntlService")
public class PntlServiceImpl extends  BaseRouterService implements PntlService{
    private static final Logger LOG = LoggerFactory.getLogger(PntlServiceImpl.class);
    private List<PntlHostContext> hostList = null;

    /**
     * 首次启动
     * @return
     */
    public Result<String> startPntl() {
        Result<String> result = null;
        String errMsg = null;

        /* 获取主机列表 */
        try {
            hostList = genProbeHostList();
        } catch (Exception e){
            LOG.error(e.getMessage());
            result.addError("", e.getMessage());
            return result;
        }

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

    public Result<String> sendPingListToAgent(PntlConfig config){
        Result<String> result = new Result<>();

        /*testing begin*/
        hostList = new ArrayList<PntlHostContext>();
        PntlHostContext tmpHost = new PntlHostContext();
        tmpHost.setIp(config.getContent().getAgentIp());
        hostList.add(tmpHost);
        hostList.add(tmpHost);
        /*testing end*/

        if (hostList == null){
            result.addError("", "No host info");
            return result;
        }

        /* 发送网络探测表到agent */
        AgentJson agentJson = null;
        Iterator<PntlHostContext> iter = hostList.iterator();
        while (iter.hasNext()){
            try{
                agentJson = generateAgentJson(iter.next());
                RestResp response = pntlRequest.sendProbeList(config.getContent().getAgentIp(), agentJson);
                if (response.getStatusCode().isError()){
                    result.addError("", "send probe list response failed");
                }
            } catch (ClientException | MalformedURLException e) {
                LOG.error("send probe list failed" + e.getMessage());
                result.addError("", "send probe list failed," + e.getMessage());
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 生成agent对应的json格式
     * @param hostList
     * @return
     */
    private AgentJson generateAgentJson(PntlHostContext hostList){
        AgentJson agentJson = new AgentJson();

        List<AgentJson.FlowList> flowList = new ArrayList<>();
       // for (PntlHostContext host : hostList){
            AgentJson.FlowList flow = new AgentJson.FlowList();
            flow.setUrgent("true");
            flow.setSip(hostList.getIp());
            ///TODO 生成目标ip
            //flow.setDip("100.109.253.152");
            flow.setDip(hostList.getIp());
            flow.setIp_protocol("udp");
            flow.setSport_min(32769);
            flow.setSport_max(32868);
            flow.setSport_range(100);
            flow.setDscp(0);

            AgentJson.FlowList.TopologyTag topologyTag = new AgentJson.FlowList.TopologyTag();
            topologyTag.setSrcId(0);
            topologyTag.setDstId(0);
            topologyTag.setLevel(1);
            flow.setTopologyTag(topologyTag);
            flowList.add(flow);
        //}
        agentJson.setList(flowList);
        return agentJson;
    }

    /**
     * 初始化配置，下发脚本，学习网络拓扑
     * @param hostList
     * @return
     * @throws ClientException
     */
    private Result<String> initPntlConfig(List<PntlHostContext> hostList) throws ClientException {
        Result<String> result = new Result<String>();

        ///TODO:下发agent文件、脚本
        Runnable scriptSendTask = new Runnable() {
            @Override
            public void run() {
                try {
                    pntlRequest.sendFilesToAgents(hostList);
                } catch (ClientException e){
                    LOG.error("Send files to agents failed, " + e.getMessage());
                    String msg = e.toString();
                    result.addError("", msg);
                }
            }
        };

        /*发送主机ip列表到agent，进行traceroute学习*/
        try{
            pntlRequest.sendIpListToAgents(hostList);
        } catch (ClientException e){
            LOG.error("Send ip list to agents failed, " + e.getMessage());
            String msg = e.toString();
            result.addError("", msg);
        }

        /*通过traceroute获得网络拓扑*/
        Runnable getNetworkMapTask = new Runnable() {
            @Override
            public void run() {
                try{
                    genNetworkMap(hostList);
                } catch (ClientException e){
                    LOG.error("Generate network map failed" + e.getMessage());
                    String msg = e.toString();
                    result.addError("", msg);
                }
            }
        };

        return  result;
    }

    /**
     * 通过从CMDB获取的host信息生成网络拓扑，根据pod
     * @param hostList
     * @return
     */
    private Result<String> genNetworkMap(List<PntlHostContext> hostList)
            throws ClientException{
        Result<String> result = new Result<>();

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
        //PntlHostContext context = new PntlHostContext();
        HostInfo hostInfo = null;
        try{
            hostInfo = pntlRequest.getHostsList(accessToken);
        } catch (ClientException | ConfigLostException e){
            LOG.error("Get hosts info list failed from cmdb" + e.getMessage());
            throw new ApplicationException(e.getType(), e.getMessage());
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
            hostContext.setIp(host.getIp());
            hostContext.setAgentStatus(host.getAgentStatus());
            hostContext.setAgentSN(host.getAgentSN());
            hostContext.setOs(host.getOs());
            hostsList.add(hostContext);
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
}
