package com.huawei.blackhole.network.extention.service.pntl;

import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
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
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
    private static final String PNTL_INSTALL_PATH = "/opt/huawei/ServerAntAgent";
    private static final String FILE_RIGHT = "644";

    private static final Map<String, String> AGENT_FILENAME = new HashMap<String, String>(){
        {
        put(PntlInfo.OS_SUSE, PntlInfo.AGENT_SUSE);
        put(PntlInfo.OS_EULER, PntlInfo.AGENT_EULER);
    }};
    private static final Map<String, String> SCRIPT_FILENAME = new HashMap<String, String>(){
        {
        put(PntlInfo.OS_SUSE, PntlInfo.AGENT_INSTALL_FILENAME);
        put(PntlInfo.OS_EULER, PntlInfo.AGENT_INSTALL_FILENAME);
    }};
    private static final Map<String, Map<String, String>> FILENAME = new HashMap<String, Map<String, String>>(){
        {
        put(FILETYPE_AGENT, AGENT_FILENAME);
        put(FILETYPE_SCRIPT, SCRIPT_FILENAME);
    }};

    private static Map<String, String> DownloadUrl = new HashMap<>();

    private static String getDownloadUrl(String key){
        return DownloadUrl.get(key);
    }

    public static void setDownloadUrl(String downloadUrl){
        if (downloadUrl.toUpperCase().contains(PntlInfo.OS_EULER)) {
            DownloadUrl.put(PntlInfo.OS_EULER, downloadUrl);
        } else if (downloadUrl.toUpperCase().contains("SLES"))  {
            DownloadUrl.put(PntlInfo.OS_SUSE, downloadUrl);
        } else if (downloadUrl.endsWith(".sh")){
            DownloadUrl.put(FILETYPE_SCRIPT, downloadUrl);
        } else if (downloadUrl.contains(PntlInfo.AGENT_CONF)){
            DownloadUrl.put(PntlInfo.AGENT_CONF, downloadUrl);
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
     * 将参数配置文件发送到agent
     * @param pntlHostList
     * @param token
     * @return
     * @throws ClientException
     */
    public Result<String> sendPntlConfigToAgents(List<PntlHostContext> pntlHostList, String token)
            throws ClientException {
        Result<String> result = new Result<>();
        RestResp resp = null;

        String url = PntlInfo.URL_IP + PntlInfo.SCRIPT_SEND_URL_SUFFIX;
        Map<String, String> header = new HashMap<>();
        setCommonHeaderForAgent(header, token);
        ScriptSendJson body = new ScriptSendJson();
        body.setPath(PNTL_INSTALL_PATH);
        body.setMode(FILE_RIGHT);
        body.setFilename(PntlInfo.AGENT_CONF);
        body.setRepoUrl(getDownloadUrl(PntlInfo.AGENT_CONF));

        List<String> snList = new ArrayList<>();
        for(PntlHostContext host : pntlHostList) {
            if (!StringUtils.isEmpty(host.getAgentSN())) {
                snList.add(host.getAgentSN());
            }
        }
        body.setAgentSNList(snList);
        try {
            resp = RestClientExt.post(url, null, body, header);
            int code = (Integer)resp.getRespBody().get("code");
            if (code != 0){
                /* agent返回失败，1000部分成功，2000全部失败，其他非0值，调用接口失败 */
                String errMsg = "code is" + code;
                if (code != 1000 && code != 2000){
                    errMsg = resp.getRespBody().get("reason").toString();
                }
                result.addError("", "send file to agent failed " + errMsg);
            }
        } catch (ClientException | JSONException e){
            LOG.error("Send pntlConfig to agent failed:" + e.getMessage());
            result.addError("", e.getMessage());
        }
        return result;
    }

    /**
     *  发送ants agent和脚本
     * @param pntlHostList
     * @return
     * @throws ClientException
     */
    public Result<String> sendInstallFilesToAgents(List<PntlHostContext> pntlHostList, String token)
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
                body.get(key).setMode(FILE_RIGHT);
                if (host.getAgentSN() != null) {
                    agentSnList.get(key).add(host.getAgentSN());
                }
            }

            for (String key : body.keySet()){
                body.get(key.toUpperCase()).setAgentSNList(agentSnList.get(key.toUpperCase()));
                try {
                    resp = RestClientExt.post(url, null, body.get(key.toUpperCase()), header);
                    int code = (Integer)resp.getRespBody().get("code");
                    if (code != 0){
                        /* agent返回失败，1000部分成功，2000全部失败，其他非0值，调用接口失败 */
                        String errMsg = "code is" + code;
                        if (code != 1000 && code != 2000){
                            errMsg = resp.getRespBody().get("reason").toString();
                        }
                        setHostErrorMsg(pntlHostList, body.get(key.toUpperCase()).getAgentSNList(), PntlInfo.PNTL_AGENT_STATUS_FAIL, errMsg);
                        result.addError("", "send file to agent failed " + errMsg);
                    } else {
                        setHostErrorMsg(pntlHostList, body.get(key.toUpperCase()).getAgentSNList(), PntlInfo.PNTL_AGENT_STATUS_SUCC, "send files to agent success");
                    }
                } catch (ClientException | JSONException e){
                    LOG.error("Send file to agent failed:" + e.getMessage());
                    result.addError("", e.getMessage());
                }
            }
        }

        return result;
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
     * @param token
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
        final String command = "cd" + " " + PNTL_INSTALL_PATH + ";sh -x UninstallService.sh";
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

    /**
     * 从kafka获得消息
     * @return
     */
    public KafkaRsp getKafkaMsg(){
        String kafkaIp = CommonInfo.getKafkaIp();
        String topic = CommonInfo.getTopic();

        if (StringUtils.isEmpty(kafkaIp) || StringUtils.isEmpty(topic)){
            return null;
        }
        KafkaRsp resp = null;
        String url = Constants.HTTP_PREFIX + kafkaIp + "/mq/" + topic + "/pntl";
        try{
            resp = RestClientExt.get(url, null, KafkaRsp.class, null);
            if (resp.getCode() != 200){
                return null;
            }
        }catch (ClientException e){
            String errMsg = "get kafka message failed " + e.getMessage();
            LOG.error(errMsg);
            return null;
        }
        return resp;
    }

}
