package com.huawei.blackhole.network.extention.service.pntl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.blackhole.network.api.bean.DelayInfoAgent;
import com.huawei.blackhole.network.api.bean.LossRateAgent;
import com.huawei.blackhole.network.api.bean.PntlConfig;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.utils.http.Parameter;
import com.huawei.blackhole.network.common.utils.http.RestClientExt;
import com.huawei.blackhole.network.common.utils.http.RestResp;
import com.huawei.blackhole.network.core.bean.PntlHostContext;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.extention.bean.pntl.*;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.extention.service.openstack.Keystone;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.Key;
import java.util.*;

@Service("pntlRequestService")
public class Pntl {
    private static final Logger LOG = LoggerFactory.getLogger(Pntl.class);
    private static final String AGENTSTATUS = "agentStatus";
    private static final String HOSTCLASS = "hostClass";
    private static final String PAGESIZE = "pageSize";
    private static final String PAGEINDEX = "pageIndex";
    private static final String PORT = "33000";
    private static final String FILETYPE_SCRIPT = "SCRIPT";
    private static final String FILETYPE_AGENT = "AGENT";
    private static final String PNTL_PATH = "/root";
    private static final String FILE_RIRHT = "644";

    private static final Map<String, String> AGENT_FILENAME = new HashMap<String, String>(){{
        put(PntlInfo.OS_SUSE, PntlInfo.AGENT_SUSE);
        put(PntlInfo.OS_EULER, PntlInfo.AGENT_EULER);
    }};
    private static final Map<String, String> SCRIPT_FILENAME= new HashMap<String, String>(){{
        put(PntlInfo.OS_SUSE, PntlInfo.AGENT_INSTALL_FILENAME);
        put(PntlInfo.OS_EULER, PntlInfo.AGENT_INSTALL_FILENAME);
    }};
    private static final Map<String, Map<String, String>> FILENAME = new HashMap<String, Map<String, String>>(){{
        put(FILETYPE_AGENT, AGENT_FILENAME);
        put(FILETYPE_SCRIPT, SCRIPT_FILENAME);
    }};

    private static Map<String, String> DownloadUrl = new HashMap<>();

    public static String getDownloadUrl(String key){
        return DownloadUrl.get(key);
    }

    public static void setDownloadUrl(String downloadUrl){
        if (downloadUrl.toUpperCase().contains(PntlInfo.OS_EULER)) {
            DownloadUrl.put(PntlInfo.OS_EULER, downloadUrl);
        } else if (downloadUrl.toUpperCase().contains("SLES"))  {
            DownloadUrl.put(PntlInfo.OS_SUSE, downloadUrl);
        } else if (downloadUrl.endsWith(".sh")){
            DownloadUrl.put(FILETYPE_SCRIPT, downloadUrl);
        }
    }

    /**
     * 从CMDB查询主机列表
     * @param token
     * @return
     * @throws ClientException
     * @throws ConfigLostException
     */
    public HostInfo getHostsList(String token) throws ClientException {
        String url = PntlInfo.URL_IP + PntlInfo.CMDB_URL_SUFFIX;
        final int PAGE_SIZE = 10;
        final String AGENT_STATUS = "running";
        final String HOST_CLASS = "PM";

        LOG.info("get cmdb Url={}", url);

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.AUTH, PntlInfo.BEARER + " " +token);

        Parameter param = new Parameter();
        param.put(AGENTSTATUS, AGENT_STATUS);
        param.put(HOSTCLASS, HOST_CLASS);
        param.put(PAGESIZE, String.valueOf(PAGE_SIZE));
        param.put(PAGEINDEX, "1");

        HostInfo hostInfo = new HostInfo();

        int total = 0;
        try {
            hostInfo = RestClientExt.get(url, param, HostInfo.class, header);
            total = hostInfo.getTotal();
            if (total > PAGE_SIZE){
                for (int pageIndex = 2; pageIndex <= total/PAGE_SIZE+1; pageIndex++){
                    param.put(PAGEINDEX, String.valueOf(pageIndex));
                    try {
                        HostInfo rsp_hostInfo = new HostInfo();
                        rsp_hostInfo = RestClientExt.get(url, param, HostInfo.class, header);
                        hostInfo.getHostsInfoList().addAll(rsp_hostInfo.getHostsInfoList());
                    } catch (ClientException e){
                        throw new ClientException(ExceptionType.SERVER_ERR, e.getMessage());
                    }
                }
            }
        } catch (ClientException e){
            throw new ClientException(ExceptionType.SERVER_ERR, e.getMessage());
        }

        return hostInfo;
    }

    public RestResp sendServerConf(String agentIp, PntlConfig config)
            throws ClientException, JsonProcessingException {
        LOG.info("start to send server config");

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.CONTENT_TYPE, PntlInfo.X_FORM_URLENCODED);

        String url = Constants.HTTPS_PREFIX + agentIp + ":" + PORT;
        ObjectMapper mapper = new ObjectMapper();
        ServerConf json = new ServerConf();
        json.setProbePeriod(config.getProbePeriod());
        json.setPortCount(config.getPortCount());
        json.setReportPeriod(config.getReportPeriod());
        json.setDelayThreshold(config.getDelayThreshold());
        json.setDscp(config.getDscp());
        json.setLossPkgTimeout(config.getLossPkgTimeout());
        json.setBigPkgRate(config.getPkgCount());
        String jsonInString = mapper.writeValueAsString(json);

        List<NameValuePair> formBody = new ArrayList<NameValuePair>();
        formBody.add(new BasicNameValuePair(PntlInfo.SERVER_ANTS_AGENT_CONF, jsonInString));

        return RestClientExt.post(url, null, formBody,  header);
    }

    /**
     * 发送探测时间间隔到agent
     * @param agentIp
     * @param json
     * @return
     * @throws ClientException
     */
    public RestResp sendProbeInterval(String agentIp, ProbeInterval json)
            throws ClientException, JsonProcessingException {
        LOG.info("start to send Probe time interval");

        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.CONTENT_TYPE, PntlInfo.X_FORM_URLENCODED);

        String url = Constants.HTTPS_PREFIX + agentIp + ":" + PORT;
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = mapper.writeValueAsString(json);

        List<NameValuePair> formBody = new ArrayList<NameValuePair>();
        formBody.add(new BasicNameValuePair(PntlInfo.SERVER_ANTS_AGENT_ACTION, jsonInString));

        return RestClientExt.post(url, null, formBody,  header);
    }


    /**
     * 通知agent查询pingList
     * @param agentIp
     * @return
     * @throws ClientException
     */
    public RestResp notifyAgentToGetPingList(String agentIp) throws ClientException{
        LOG.info("Notify agent[" + agentIp + "] to get pingList");
        Map<String, String> header = new HashMap<>();
        header.put(PntlInfo.CONTENT_TYPE, PntlInfo.X_FORM_URLENCODED);

        String url = Constants.HTTPS_PREFIX + agentIp + ":" + PORT;
        List<NameValuePair> formBody = new ArrayList<NameValuePair>();
        formBody.add(new BasicNameValuePair(PntlInfo.SERVER_ANTS_AGENT_IP, ""));

        return RestClientExt.post(url, null, formBody,  header);
    }

    private void setHostErrorMsg(List<PntlHostContext> hostList, List<String> snList, String agentStatus,
                                 String errMsg){
        for (PntlHostContext host : hostList){
            if (snList.contains(host.getAgentSN())){
                host.setAgentStatus(agentStatus);
                host.setReason(errMsg);
            }
        }
    }

    /**
     *  发送ants agent和脚本
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public Result<String> sendFilesToAgents(List<PntlHostContext> pntlHostList, String token)
            throws ClientException {
        Result<String> result = new Result<>();
        LOG.info("send files to agents");

        RestResp resp = null;
        String url = PntlInfo.URL_IP + PntlInfo.SCRIPT_SEND_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();
        setCommonHeaderForAgent(header, token);

        Map<String, List<String>> agentSnList= new HashMap<String, List<String>>(){{
            put(PntlInfo.OS_SUSE, new ArrayList<>());
            put(PntlInfo.OS_EULER, new ArrayList<>());
        }};

        Map<String, ScriptSendJson> body = new HashMap<String, ScriptSendJson>(){{
            put(PntlInfo.OS_SUSE, new ScriptSendJson());
            put(PntlInfo.OS_EULER, new ScriptSendJson());
        }};

        /*两类文件：agent、安装脚本*/
        for (String fileType : FILENAME.keySet()){
            Map<String, String> file = FILENAME.get(fileType);
             /*两种os，agent不同*/
            for (PntlHostContext host : pntlHostList) {
                if (host.getOs() == null){
                    host.setReason("os is null");
                    host.setAgentStatus(PntlInfo.PNTL_AGENT_STATUS_FAIL);
                    continue;
                }
                String key = host.getOs().toUpperCase();
                if (!key.equalsIgnoreCase(PntlInfo.OS_SUSE) && !key.equalsIgnoreCase(PntlInfo.OS_EULER)){
                    host.setReason("os is not Suse or Euler");
                    host.setAgentStatus(PntlInfo.PNTL_AGENT_STATUS_FAIL);
                    continue;
                }
                /*根据不同的文件，获取仓库地址*/
                body.get(key).setFilename(file.get((key)));
                if (FILETYPE_SCRIPT.equals(fileType)){
                     body.get(key).setRepoUrl(getDownloadUrl(FILETYPE_SCRIPT));
                } else {
                    body.get(key).setRepoUrl(getDownloadUrl(key));
                }
                if (host.getAgentSN() == null){
                    host.setReason("sn is null");
                    host.setAgentStatus(PntlInfo.PNTL_AGENT_STATUS_FAIL);
                    continue;
                } else if (body.get(key).getRepoUrl() == null){
                    host.setReason("repo url is null");
                    host.setAgentStatus(PntlInfo.PNTL_AGENT_STATUS_FAIL);
                    continue;
                }

                body.get(key).setPath(PNTL_PATH);
                body.get(key).setMode(FILE_RIRHT);
                if (host.getAgentSN() != null) {
                    agentSnList.get(key).add(host.getAgentSN());
                }
            }

            for (String key : body.keySet()){
                body.get(key.toUpperCase()).setAgentSNList(agentSnList.get(key.toUpperCase()));
                try {
                    resp = RestClientExt.post(url, null, body.get(key.toUpperCase()), header);
                    if ((Integer)resp.getRespBody().get("code") != 0){
                        /* agent返回失败，1000部分成功，2000全部失败，其他非0值，调用接口失败 */
                        setHostErrorMsg(pntlHostList, body.get(key.toUpperCase()).getAgentSNList(), PntlInfo.PNTL_AGENT_STATUS_FAIL, resp.getRespBody().get("reason").toString());
                        result.addError("", "send file to agent failed " + resp.getRespBody().get("reason").toString());
                    } else {
                        setHostErrorMsg(pntlHostList, body.get(key.toUpperCase()).getAgentSNList(), PntlInfo.PNTL_AGENT_STATUS_SUCC, "send files to agent success");
                    }
                } catch (ClientException | JSONException e){
                    LOG.error("Send script to agent failed");
                    result.addError("", e.getMessage());
                }
            }
        }

        return result;
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
            ipList.append(host.getVbondIp());
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
            LOG.info("get traceroute result from " + host.getVbondIp() + "failed");
        }

        return resp;
    }

    public static void setCommonHeaderForAgent(Map<String, String> header, String token){
        if (header == null){
            return;
        }
        header.put(PntlInfo.SERVICENAME, PntlInfo.PNTL_SERVICENAME);
        header.put(PntlInfo.USERNAME, PntlInfo.OPS_USERNAME);
        if (token != null){
            header.put(PntlInfo.AUTH, PntlInfo.BEARER + " " + token);
        }
    }

    private RestResp sendCommandToAgents(List<String> snList, String token, String command, String cmdType)
        throws ClientException{
        CmdSetJson reqBody = new CmdSetJson();
        String url = PntlInfo.URL_IP + PntlInfo.CMD_SET_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();

        reqBody.setCmdType(cmdType);
        reqBody.setUserName(PntlInfo.PNTL_ROOT_NAME);
        reqBody.setCmdSet(command);
        reqBody.setTimeout("5000");
        reqBody.setAgentSNList(snList);

        setCommonHeaderForAgent(header, token);
        return RestClientExt.post(url, null, reqBody, header);
    }
    /**
     * 下发安装命令，执行安装脚本，进行agent安装
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public RestResp installAgent(List<PntlHostContext> pntlHostList, String token) throws ClientException{
        List<String> snList = new ArrayList<>();
        for (PntlHostContext host : pntlHostList){
            if (host.getAgentSN() != null && !PntlInfo.PNTL_AGENT_STATUS_FAIL.equals(host.getAgentStatus())) {
                snList.add(host.getAgentSN());
            }
        }
        final String command = "cd" + " " + PNTL_PATH + ";sh -x install_pntl.sh";
        return sendCommandToAgents(snList, token, command, "sync");
    }

    public RestResp exitAgent(List<PntlHostContext> pntlHostList, String token) throws ClientException{
        List<String> snList = new ArrayList<>();
        for (PntlHostContext host : pntlHostList){
            if (host.getAgentSN() != null) {
                snList.add(host.getAgentSN());
            }
        }
        final String command = "service ServerAntAgentService stop";
        return sendCommandToAgents(snList, token, command, "sync");
    }

    public RestResp startAgent(List<PntlHostContext> pntlHostList, String token) throws ClientException{
        List<String> snList = new ArrayList<>();
        for (PntlHostContext host : pntlHostList){
            if (host.getAgentSN() != null) {
                snList.add(host.getAgentSN());
            }
        }
        final String command = "service ServerAntAgentService start";
        return sendCommandToAgents(snList, token, command, "sync");
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

    public static String getAgentSnByIp(String ip) throws ClientException{
        String agentSn = null;
        if (ip == null){
            return null;
        }

        Map<String, String> header = new HashMap<>();
        String token = null;
        try {
            token = new Keystone().getPntlAccessToken();
        }catch (ClientException e){
            throw new ClientException("", "Get token fail " + e.getMessage());
        }
        setCommonHeaderForAgent(header, token);

        AgentInfoByIp resp = null;
        String url = PntlInfo.URL_IP + PntlInfo.AGENT_INFO_BY_IP;
        Parameter param = new Parameter();
        param.put("value", ip);
        try {
            resp = RestClientExt.get(url, param, AgentInfoByIp.class, header);
        }catch (ClientException e){
            throw new ClientException("", "get agent info by ip(" + ip + ") failed " + e.getMessage());
        }
        agentSn = resp.getData();

        return agentSn;
    }

    public void startTraceroute(String srcIp, String dstIp){
        RestResp resp = null;
        String srcAgentSn = null;
        String dstAgentSn = null;
        if (srcIp == null || dstIp == null){
            return;
        }
        try {
            srcAgentSn = getAgentSnByIp(srcIp);
            dstAgentSn = getAgentSnByIp(srcIp);
        } catch (ClientException e){
            LOG.error(e.getMessage());
            return;
        }
        if (srcAgentSn == null || dstAgentSn == null){
            return;
        }
        List<String> snList = new ArrayList<>();
        snList.add(srcAgentSn);
        String token = null;
        try {
            token = new Keystone().getPntlAccessToken();
        }catch (ClientException e){
            LOG.error("Get token fail " + e.getMessage());
            return;
        }
        final String command = "cd /opt/huawei/ServerAntAgent & python tracetool.py";
        try {
            resp = sendCommandToAgents(snList, token, command, "async");
            if (resp.getStatusCode().isError()){
                LOG.error("Execute:" + command + "fail");
            }
        } catch(ClientException e){
            LOG.error("Execute:" + command + "fail " + e.getMessage());
        }
    }

}
