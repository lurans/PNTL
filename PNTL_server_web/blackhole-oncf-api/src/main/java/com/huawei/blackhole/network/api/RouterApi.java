package com.huawei.blackhole.network.api;

import com.huawei.blackhole.network.api.bean.*;
import com.huawei.blackhole.network.api.resource.ResultPool;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.PntlInfo;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.BaseException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.InvalidParamException;
import com.huawei.blackhole.network.common.utils.AuthUtil;
import com.huawei.blackhole.network.common.utils.ExceptionUtil;
import com.huawei.blackhole.network.common.utils.ResponseUtil;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.service.*;
import com.huawei.blackhole.network.core.thread.ChkflowServiceStartup;
import com.huawei.blackhole.network.extention.bean.pntl.AgentFlowsJson;
import com.huawei.blackhole.network.extention.bean.pntl.IpListJson;
import com.huawei.blackhole.network.extention.service.conf.OncfConfigService;
import com.huawei.blackhole.network.extention.service.conf.PntlConfigService;
import com.huawei.blackhole.network.extention.service.openstack.Keystone;
import com.huawei.blackhole.network.extention.service.pntl.PntlWarnService;
import com.huawei.blackhole.network.extention.service.sso.SsoConfiger;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller("routerApi")
@Path("chkflow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RouterApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouterApi.class);

    @Context
    private HttpServletRequest request;

    @Resource
    private WebServiceContext context;

    @Resource(name = "ewRouterService")
    private EwRouterService ewRouterService;

    @Resource(name = "eipRouterService")
    private EIPRouterService eipRouterService;

    @Resource(name = "vpnRouterService")
    private VPNRouterService vpnRouterService;

    @Resource(name = "oncfConfigService")
    private OncfConfigService oncfConfigService;

    @Resource(name = "keystoneService")
    private Keystone keystoneService;

    @Resource(name = "ssoConfiger")
    private SsoConfiger ssoConfiger;

    @Resource(name = "chkflowServiceStartup")
    private ChkflowServiceStartup chkflowServiceStartup;

    @Resource(name = "pntlService")
    private PntlService pntlService;

    @Resource(name = "pntlWarnService")
    private PntlWarnService pntlWarnService;

    @Resource(name = "pntlConfigService")
    private PntlConfigService pntlConfigService;
    /**
     * 获取当前配置 config.ymal <br />
     * {<br />
     * "cascading_ip ":"xxx.xxx.xxx.xxx",<br />
     * "os_tenant_name ":"xx", <br />
     * "os_username ":"xx", <br />
     * "os_password":"****",<br />
     * "os_auth_url ":"http://www.xxx.com:123",<br />
     * "can_ssh_key":"xxx"<br />
     */
    @Path("config")
    @GET
    public Response getFspConfig() {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [get fsp config]");
        Result<OncfConfig> result = oncfConfigService.getOncfConfig();
        if (result.isSuccess()) {
            return ResponseUtil.succ(result.getModel());
        }
        return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
    }

    /**
     * 设置当前配置到config.yml
     */
    @Path("config")
    @PUT
    public Response setFspConfig(OncfConfig oncfConfig) {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [set fsp config]");
        try {
            if (keystoneService.validFspConfig(oncfConfig)) {
                Result<String> result = oncfConfigService.setOncfConfig(oncfConfig);
                if (result.isSuccess()) {
                    return ResponseUtil.succ();
                }
                return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
            }
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, "invalid configuration for fsp");
        } catch (BaseException e) {
            LOGGER.error(e.toString(), e);
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        }
    }

    @Path("token")
    @GET
    public Response validFsp() {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [check current config]");
        try {
            if (keystoneService.validFspConfig()) {
                return ResponseUtil.succ();
            }
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (BaseException e) {
            LOGGER.error(e.toString(), e);
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, e.toString());
        }
    }

    /**
     * 查看fspInfo是否是一组可以登录的账户信息。 <br />
     * 成功返回 {"success"}<br />
     * 失败返回 {"err_msg":"fail ..."} <br />
     */
    @Path("token")
    @POST
    public Response validFsp(OncfConfig fspInfo) {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [check new config]");
        try {
            if (keystoneService.validFspConfig(fspInfo)) {
                return ResponseUtil.succ();
            }
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR);
        } catch (InvalidParamException | ClientException | ApplicationException e) {
            String errMsg = e.toString();
            LOGGER.error(errMsg, e);
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, errMsg);
        }
    }

    /**
     * 上传key文件
     *
     * @param request
     * @return
     */
    @Path("key")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadKeyFile(@Context HttpServletRequest request, MultipartBody body) {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [upload or check key file]");
        if (!validUploadKeyParam(request, body)) {
            String errMsg = ExceptionUtil.prefix(ExceptionType.CLIENT_ERR) + "invalid request";
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, errMsg);
        }
        String type = body.getAttachmentObject("tinyFormDatas", String.class);
        if (type.startsWith("\"") && type.endsWith("\"")) {
            type = type.substring(1, type.length() - 1);
        }
        Attachment file = body.getAttachment(Constants.FORM_FILE);
        Result<String> result = null;
        if (Constants.KEY_STATUS_SUBMIT.equals(type)) {
            result = oncfConfigService.uploadKey(file);
        } else {
            result = oncfConfigService.validKey(file);
        }
        JSONObject requestType = new JSONObject();
        requestType.put("type", type);
        if (result.isSuccess()) {
            return ResponseUtil.succ(requestType);
        } else {
            requestType.put("err_msg", result.getErrorMessage());
            return ResponseUtil.errJson(Response.Status.INTERNAL_SERVER_ERROR, requestType);
        }
    }

    /*
     * 重启服务
     */
    @Path("tomcat")
    @POST
    public Response restartTomcat() {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [restart service]");
        Result<String> result = chkflowServiceStartup.registry();
        if (result.isSuccess()) {
            return ResponseUtil.succ();
        }
        return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
    }

    @Path("sso")
    @GET
    public Response getSsoConfig() {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [get sso config]");
        Result<Object> result = ssoConfiger.getSsoConfig();
        if (result.isSuccess()) {
            return ResponseUtil.succ(result.getModel());
        }
        return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
    }

    @Path("ssoLogin")
    @GET
    public Response isSsoLogin() {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [check use sso]");
        Result<Map<String, String>> result = ssoConfiger.isSsoLogin(request);
        if (result.isSuccess()) {
            return ResponseUtil.succ(result.getModel());
        }
        return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
    }

    @Path("/vm")
    @POST
    public Response submitEWRouterTask(RouterTaskRequest req) {
        String taskId = UUID.randomUUID().toString();
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [submit ew task]");
        LOGGER.info("TASK-START:" + taskId);
        RouterTaskResponse routerResponse = new RouterTaskResponse();
        Result<String> result = ewRouterService.submitEwTask(req, taskId);

        if (!result.isSuccess()) {
            JSONObject json = new JSONObject();
            json.put("err_msg", result.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }
        routerResponse.setTaskId(result.getModel());
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(routerResponse).build();
    }

    @Path("/fip")
    @POST
    public Response submitFIPRouterTask(FIPRouterTaskRequest req) {
        String taskId = UUID.randomUUID().toString();
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [submit eip task]");
        LOGGER.info("TASK-START:" + taskId);
        RouterTaskResponse routerResponse = new RouterTaskResponse();
        Result<String> result = eipRouterService.submitEipTask(req, taskId);

        if (!result.isSuccess()) {
            JSONObject json = new JSONObject();
            json.put("err_msg", result.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }
        routerResponse.setTaskId(result.getModel());
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(routerResponse).build();
    }

    @Path("/vpn")
    @POST
    public Response submitVPNRouterTask(VPNRouterTaskRequest req) {
        String taskId = UUID.randomUUID().toString();
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [submit vpn task]");
        LOGGER.info("TASK-START:" + taskId);
        RouterTaskResponse routerResponse = new RouterTaskResponse();
        Result<String> result;
        result = vpnRouterService.submitVpnTask(req, taskId);

        if (!result.isSuccess()) {
            JSONObject json = new JSONObject();
            json.put("err_msg", result.getErrorMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(json.toString()).build();
        }
        routerResponse.setTaskId(result.getModel());
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(routerResponse).build();
    }

    @Path("{id}")
    @GET
    public RouterInfoResponse getRouterInfo(@PathParam("id") String id) {
        LOGGER.info("User[" + AuthUtil.getUser(request) + "] [get task result]");
        return ResultPool.getResult(id);
    }

    private boolean validUploadKeyParam(HttpServletRequest request2, MultipartBody body) {
        String errMsg = ExceptionUtil.prefix(ExceptionType.CLIENT_ERR) + "invalid request";
        boolean valid = true;
        if (request == null || body == null) {
            LOGGER.error(errMsg);
            valid = false;
        }
        if (valid) {
            String type = body.getAttachmentObject("tinyFormDatas", String.class);
            if (type == null) {
                LOGGER.error(errMsg);
                valid = false;
            }
        }
        if (valid) {
            Attachment file = body.getAttachment(Constants.FORM_FILE);
            if (file == null) {
                LOGGER.error(errMsg);
                valid = false;
            }
        }
        return valid;
    }

    @Path("/pntlInit")
    @POST
    public Response deployAgent(){
        LOGGER.info("pntl init configuration");
        Result<String> result = new Result<String>();

        result = pntlService.deployAgent();
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ();
    }

    @Path("/pingList")
    @POST
    public Response getPingList(PingListRequest config){
        LOGGER.info("receive ping list from agent[" + config.getContent().getAgentIp() + "]");
        Result<AgentFlowsJson> result = new Result<AgentFlowsJson>();

        result = pntlService.getPingList(config);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ(result.getModel());
    }

    @Path("/lossRate")
    @POST
    public Response recvLossRate(LossRateAgent data){
        LOGGER.info("receive lossRate from agent");
        Result<String> result = new Result<String>();

        result = pntlWarnService.saveLossRateData(data);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        return ResponseUtil.succ();
    }

    @Path("/delayInfo")
    @POST
    public Response recvDelayInfo(DelayInfoAgent data){
        LOGGER.info("receive delayInfo from agent");
        Result<String> result = new Result<String>();

        result = pntlWarnService.saveDelayInfoData(data);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        return ResponseUtil.succ();
    }

    @Path("/lossRate")
    @GET
    public Response getLossRate(){
        LOGGER.info("send loss rate to UI");

        Result<Object> result = pntlWarnService.getLossRate();
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ(result.getModel());
    }

    @Path("/delayInfo")
    @GET
    public Response getDelayInfo(){
        LOGGER.info("send delay info to UI");

        Result<Object> result = pntlWarnService.getDelayInfo();
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ(result.getModel());
    }

    @Path("/pntlVariableConf")
    @POST
    public Response setPntlConfig(PntlConfig config){
        /*先保存在文件，后下发到agent*/
        Result<String> result = pntlConfigService.setPntlConfig(config);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        result = pntlService.setServerConf(config);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        return ResponseUtil.succ();
    }

    @Path("/pntlConf")
    @GET
    public Response getPntlConfig(){
        Result<PntlConfig> result = pntlConfigService.getPntlConfig();
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ(result.getModel());
    }

    @Path("/pntlAkSkConf")
    @POST
    public Response setPntlAkSkConf(PntlConfig config){
        Result<String> result = pntlConfigService.setPntlAkSkConfig(config);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        return ResponseUtil.succ();
    }

    @Path("/ipList")
    @POST
    public Response getIpList(IpListRequest req){
        String azId = req.getAzId();
        String podId = req.getPodId();
        LOGGER.info("Get ip list, azId(" + azId + "), podId(" + podId + ")");
        Result<IpListJson> result = pntlService.getIpListinfo(azId, podId);
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        return ResponseUtil.succ(result.getModel());
    }

    /*停止探测，agent依然活着*/
    @Path("/stopProbe")
    @POST
    public Response stopProbe(){
        Result<String> result = pntlService.setProbeInterval("0");
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ();
    }

    /*退出探测，agent死了*/
    @Path("/exitProbe")
    @POST
    public Response exitAgent(){
        Result<String> result = pntlService.setProbeInterval("-1");
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ();
    }

    /*重新启动探测，启动agent*/
    @Path("/startAgent")
    @POST
    public Response startAgent(){
        Result<String> result = pntlService.startAgent();
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ();
    }

    @Path("/agentIp")
    @POST
    public Response recvAgentIp(AgentIp ip){
        Result<String> result = pntlService.saveAgentIp(ip.getAgentIp(), ip.getVbondIp());
        if (!result.isSuccess()){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
        return ResponseUtil.succ();
    }

    @Path("/uploadFiles")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFiles(@Context HttpServletRequest request, MultipartBody body){
        LOGGER.info("start to upload agent package files");
        Result<String> result = null;
        Attachment file = body.getAttachment(Constants.FORM_FILE);
        if (file == null) {
            String errMsg = ExceptionUtil.prefix(ExceptionType.CLIENT_ERR) + "invalid request";
            LOGGER.error(errMsg);
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, errMsg);
        }

        List<Attachment> atts = body.getAllAttachments();
        for (Attachment a : atts) {
            ContentDisposition cd = a.getContentDisposition();
            if (cd != null && Constants.FORM_FILE.equals(cd.getParameter("name"))) {
                if (a.getDataHandler().getName().equalsIgnoreCase(PntlInfo.PNTL_IPLIST_CONF)) {
                    result = pntlConfigService.uploadIpListFile(a, "");
                    if (!result.isSuccess()) {
                        return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
                    }
                } else {
                    result = pntlConfigService.uploadAgentPkgFile(a);
                    if (!result.isSuccess()) {
                        return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
                    }
                }
            }
        }
        return ResponseUtil.succ();
    }

    @Path("/warningList")
    @POST
    public Response getWarningList(PntlWarning.PntlWarnInfo param){
        Result<Object> result = PntlWarning.getWarnList(param);
        if (result.isSuccess()) {
            return ResponseUtil.succ(result.getModel());
        } else {
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
    }

    @Path("/updateAgents")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateAgents(@Context HttpServletRequest request, MultipartBody body){
        LOGGER.info("start to update agents");
        Result<String> result = new Result<>();

        String type = body.getAttachmentObject("operation", String.class);
        if (!type.equals(PntlInfo.PNTL_UPDATE_TYPE_ADD) && !type.equals(PntlInfo.PNTL_UPDATE_TYPE_DEL)){
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, "operation is error:" + type);
        }

        Attachment file = body.getAttachment(Constants.FORM_FILE);
        result = pntlConfigService.uploadIpListFile(file, PntlInfo.PNTL_UPDATE_IPLIST_CONFIG);
        if (!result.isSuccess()) {
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }

        result = pntlService.updateAgents(type);
        if (result.isSuccess()) {
            return ResponseUtil.succ();
        } else {
            return ResponseUtil.err(Response.Status.INTERNAL_SERVER_ERROR, result.getErrorMessage());
        }
    }
}
