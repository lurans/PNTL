package com.huawei.blackhole.network.extention.service.pntl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.extention.bean.pntl.AgentFlowsJson;
import com.huawei.blackhole.network.extention.bean.pntl.CmdSetJson;
import com.huawei.blackhole.network.extention.bean.pntl.HostInfo;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.extention.bean.pntl.ScriptSendJson;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("pntlRequestService")
public class Pntl {
    private static final Logger LOG = LoggerFactory.getLogger(Pntl.class);
    private static final String AGENTSTATUS = "agentStatus";
    private static final String HOSTCLASS = "hostClass";
    private static final String PAGESIZE = "pageSize";
    private static final String PORT = "8888";
    private static final String USERNAME = "user_name";
    private static final String SERVICENAME = "service_name";
    private static final String BEARER = "Bearer";
    private static final String APIGATWAYURL = "http://8.15.4.22/";
    private static final String OS_SUSE = "SUSE";
    private static final String CMD_SET = "sh -x install_pntl.sh";

    /**
     * 从CMDB查询主机列表
     * @param token
     * @return
     * @throws ClientException
     * @throws ConfigLostException
     */
    public HostInfo getHostsList(String token) throws ClientException, ConfigLostException {
        String url = PntlInfo.URL_IP + PntlInfo.CMDB_URL_SUFFIX;
        LOG.info("get cmdb Url={}", url);

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.AUTH, token);

        Parameter param = new Parameter();
        param.put(AGENTSTATUS, "running");
        param.put(HOSTCLASS, "PM");
        param.put(PAGESIZE, "0");

        /*test begin*/
        HostInfo hostInfo = new HostInfo();
        List<HostInfo.HostListInfo> list = new ArrayList<>();
        HostInfo.HostListInfo host = new HostInfo.HostListInfo();
        host.setAgentSN("4c3a8a5d-0bec-4715-aa0f-4d2f411819fd");
        host.setIp("8.15.1.103");
        list.add(host);
        hostInfo.setHostsInfoList(list);
        return hostInfo;
        /*test end*/

        //return RestClientExt.get(url, param, HostInfo.class, header);
    }

    /**
     * 发送探测流表到agent
     * @param agentIp
     * @param json
     * @return
     * @throws ClientException
     * @throws JsonProcessingException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    public RestResp sendProbeList(String agentIp, AgentFlowsJson json)
            throws ClientException, JsonProcessingException, MalformedURLException, UnsupportedEncodingException {
        LOG.info("start to send Probe");

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.CONTENT_TYPE, PntlInfo.X_FORM_URLENCODED);

        String url = "http://" + agentIp + ":" + PORT;
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(json);

        List<NameValuePair> formBody = new ArrayList<NameValuePair>();
        formBody.add(new BasicNameValuePair(PntlInfo.SERVER_ANTS_ANGENT, jsonInString));

        return RestClientExt.post(url, null, formBody,  header);
    }

    /**
     * 发送ip列表到agent
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public RestResp sendIpListToAgents(List<PntlHostContext> pntlHostList)
        throws ClientException{
        LOG.info("send host ip list to agents");

        StringBuffer ips = new StringBuffer();
        for (PntlHostContext host : pntlHostList){
            ips.append(host.getIp()+"\n");
        }

        ///TODO 发送ips到agent
        return RestClientExt.post(null, null, null,  null);
    }

    /**
     *  发送ants agent和脚本
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public RestResp sendFilesToAgents(List<PntlHostContext> pntlHostList, String token)
            throws ClientException {
        LOG.info("send files to agents");

        RestResp resp = null;
        String url = PntlInfo.URL_IP + PntlInfo.SCRIPT_SEND_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();
        header.put(USERNAME, "y00214328");
        header.put(SERVICENAME, "ops_agent");
        header.put(PntlInfo.AUTH, BEARER + " " + token);

        List<String> suseAgentSnList = new ArrayList<>();
        List<String> eulerAgentSnList = new ArrayList<>();

        ScriptSendJson suseBody = new ScriptSendJson();
        ScriptSendJson eulerBody = new ScriptSendJson();
        /*两种os，agent不同*/
        for (PntlHostContext host : pntlHostList) {
            if (host.getOs().equals(OS_SUSE)){
                suseBody.setFilename(Resource.AGENT_SUSE);
                suseBody.setRepoUrl(APIGATWAYURL+Resource.AGENT_SUSE);
                suseBody.setPath(Resource.PNTL_PATH);
                suseAgentSnList.add(host.getAgentSN());
            } else{
                eulerBody.setFilename(Resource.AGENT_EULER);
                eulerBody.setRepoUrl(APIGATWAYURL+Resource.AGENT_EULER);
                eulerBody.setPath(Resource.PNTL_PATH);
                eulerAgentSnList.add(host.getAgentSN());
            }
        }
        suseBody.setAgentSNList(suseAgentSnList);
        eulerBody.setAgentSNList(eulerAgentSnList);

        try {
            resp = RestClientExt.post(url, null, suseBody, header);
        } catch (ClientException e){
            LOG.error("Send script to suse os agent failed");
        }

        try {
            resp = RestClientExt.post(url, null, eulerBody, header);
        } catch (ClientException e){
            LOG.error("Send script to euler os agent failed");
        }

        return resp;
    }

    /**
     *
     * 启动traceroute任务，将Dst ip列表发送给traceroute，并启动traceroute网络拓扑学习
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public RestResp tracerouteTask(List<PntlHostContext> pntlHostList)
        throws ClientException{
        LOG.info("start traceroute task");

        RestResp resp = null;
        StringBuffer ipList = new StringBuffer();
        for (PntlHostContext host : pntlHostList){
            ipList.append(host.getIp());
            if (pntlHostList.indexOf(host) != pntlHostList.size()-1){
                ipList.append(" ");
            }
        }

        for (PntlHostContext host : pntlHostList){
            String agentSn = host.getAgentSN();
            ///TODO 向每个agentSn发送ipList
        }

        return resp;
    }
    /**
     * 从server获取学习完成的traceroue结果
     * @param host
     * @return
     * @throws ClientException
     */
    public RestResp getTracerouteResult(PntlHostContext host)
        throws ClientException{
        RestResp resp = null;

        String url = PntlInfo.URL_IP + PntlInfo.AGENT_LOG_URL_SUFFIX;
        ///todo
        resp = RestClientExt.post(url, null, null, null);
        if (resp.getStatusCode().isError()){
            LOG.info("get traceroute result from " + host.getIp() + "failed");
        }

        return resp;
    }

    /**
     * 下发安装命令，执行安装脚本，进行agent安装
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public RestResp installAgent(List<PntlHostContext> pntlHostList) throws ClientException{
        RestResp resp = null;
        CmdSetJson reqBody = new CmdSetJson();
        String url = PntlInfo.URL_IP + PntlInfo.CMD_SET_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();

        reqBody.setCmdType("sync");
        reqBody.setUserName("gandalf");
        reqBody.setCmdSet(CMD_SET);
        reqBody.setTimeout("5000");

        List<String> snList = new ArrayList<>();
        for (PntlHostContext host : pntlHostList){
            snList.add(host.getAgentSN());
        }
        reqBody.setAgentSNList(snList);

        header.put(SERVICENAME, "ops_agent");
        header.put(USERNAME, "y00214328");
        resp = RestClientExt.post(url, null, reqBody, header);
        if (resp.getStatusCode().isError()){
            LOG.info("install agent failed");
        }

        return resp;
    }

    public static final class ProbeFlows{
        private String ServerAntsAgent;

        public String getServerAntsAgent() {
            return ServerAntsAgent;
        }

        public void setServerAntsAgent(String serverAntsAgent) {
            ServerAntsAgent = serverAntsAgent;
        }

        public ProbeFlows(String str){
            this.ServerAntsAgent = str;
        }
    }

}
