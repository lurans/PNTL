package com.huawei.blackhole.network.extention.service.pntl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.extention.bean.pntl.AgentJson;
import com.huawei.blackhole.network.extention.bean.pntl.HostInfo;
import com.huawei.blackhole.network.common.constants.PntlInfo;
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


/**
 * Created by y00214328 on 2017/5/19.
 */
@Service("pntlRequestService")
public class Pntl {
    private static final Logger LOG = LoggerFactory.getLogger(Pntl.class);
    private static final String AGENTSTATUS = "agentStatus";
    private static final String HOSTCLASS = "hostClass";
    private static final String PAGESIZE = "pageSize";
    private static final String PORT = "8888";

    private String getCmdbUrl(){
        return PntlInfo.URL_IP + PntlInfo.CMDB_URL_SUFFIX;
    }

    /**
     * 从CMDB查询主机列表
     * @param token
     * @return
     * @throws ClientException
     * @throws ConfigLostException
     */
    public HostInfo getHostsList(String token)
            throws ClientException, ConfigLostException {
        String url = getCmdbUrl();
        LOG.info("get cmdb Url={}", url);

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.AUTH, token);

        Parameter param = new Parameter();
        param.put(AGENTSTATUS, "running");
        param.put(HOSTCLASS, "PM");
        param.put(PAGESIZE, "0");
        return RestClientExt.get(url, param, HostInfo.class, header);
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
    public RestResp sendProbeList(String agentIp, AgentJson json)
            throws ClientException, JsonProcessingException, MalformedURLException, UnsupportedEncodingException {
        LOG.info("start to send Probe");

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.CONTENT_TYPE, "application/x-www-form-urlencoded");

        String url = "http://" + agentIp + PORT;
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
            ips.append(host.getIp()+" ");
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
    public RestResp sendFilesToAgents(List<PntlHostContext> pntlHostList)
            throws ClientException {
        LOG.info("send files to agents");

        RestResp resp = null;
        String url = getScriptSendUrl();
        for (PntlHostContext host : pntlHostList){
            ///todo
            resp = RestClientExt.post(url, null, null, null);
            if (resp.getStatusCode().isError()){
                LOG.info("send files to " + host.getIp() + "failed");
                continue;
            }
        }
        return resp;
    }

    public String getScriptSendUrl(){
        return PntlInfo.URL_IP + PntlInfo.SCRIPT_SEND_URL_SUFFIX;
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

        String url = getAgentLogUrl();
        ///todo
        resp = RestClientExt.post(url, null, null, null);
        if (resp.getStatusCode().isError()){
            LOG.info("get traceroute result from " + host.getIp() + "failed");
        }

        return resp;
    }

    private String getAgentLogUrl(){
        return PntlInfo.URL_IP + PntlInfo.AGENT_LOG_URL_SUFFIX;
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
