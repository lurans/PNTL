package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.FIPRouterTaskRequest;
import com.huawei.blackhole.network.api.bean.NodeInfo;
import com.huawei.blackhole.network.api.bean.RouterInfoResponse;
import com.huawei.blackhole.network.api.resource.EipInfo;
import com.huawei.blackhole.network.api.resource.ResultPool;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.HostType;
import com.huawei.blackhole.network.common.constants.LogFileFormat;
import com.huawei.blackhole.network.common.constants.Resource;
import com.huawei.blackhole.network.common.constants.ResultTag;
import com.huawei.blackhole.network.common.constants.TaskTag;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.ClientException;
import com.huawei.blackhole.network.common.exception.ConfigLostException;
import com.huawei.blackhole.network.common.exception.InvalidParamException;
import com.huawei.blackhole.network.common.exception.ScriptException;
import com.huawei.blackhole.network.common.utils.ThreadUtil;
import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.CommonCallableParam;
import com.huawei.blackhole.network.core.bean.FIPTaskParam;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.bean.RouterContext;
import com.huawei.blackhole.network.core.thread.ChkflowServiceStartup;
import com.huawei.blackhole.network.core.thread.FIPTaskThread;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

@Service("eipRouterService")
public class EIPRouterServiceImpl extends BaseRouterService implements EIPRouterService {

    private static final Logger LOG = LoggerFactory.getLogger(EIPRouterServiceImpl.class);

    @Override
    public Result<String> submitEipTask(FIPRouterTaskRequest req, String taskId) {
        Result<String> result = new Result<String>();

        // 检查是否正在重启
        if (!ChkflowServiceStartup.isStarted()) {
            String msg = "OpsNetworkChkFlow is restaring, please wait for a moment";
            result.addError("", msg);
            LOG.error(msg);
            return result;
        }

        // 清理过期未取回任务
        ResultPool.clearOvertimeRecord();
        final EipInfo eipInfo = new EipInfo();

        Runnable submitWorker = new Runnable() { // 提交任务的task
            @Override
            public void run() {
                try {
                    submit(req, eipInfo);
                } catch (InvalidParamException | ApplicationException | ClientException | ConfigLostException e) {
                    eipInfo.errHappened();
                    String msg = e.toString();
                    result.addError("", msg);
                    eipInfo.setErrMsg(msg);
                    LOG.error(msg, e);
                }
            }
        };

        Runnable getResultWorker = new Runnable() { // 获取任务结果的task
            @Override
            public void run() {
                RouterInfoResponse response = null;
                try {
                    for (int i = 0; i < Constants.SUBMIT_NUMBER; i++) {
                        if (eipInfo.submited() || eipInfo.hasErr()) {
                            break;
                        }
                        try {
                            Thread.sleep(Constants.SEARCH_INTERVAL);
                        } catch (InterruptedException e) {
                            LOG.warn("ignore : interrupted sleep");
                        }
                        LOG.info("eip task submitting");
                    }
                    if (eipInfo.hasErr()) {
                        LOG.info("exception happened before submit eip task");
                        response = new RouterInfoResponse();
                        response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                        response.setErrorInfo(eipInfo.getErrMsg());
                        ResultPool.add(eipInfo.getTaskId(), response);
                        return;
                    }
                    boolean submited = eipInfo.submited();
                    for (int i = 0; i < Constants.SEARCH_NUMBER && submited; ++i) {
                        try {
                            Thread.sleep(Constants.SEARCH_INTERVAL);
                        } catch (InterruptedException e) { // nothing
                            LOG.warn("ignore : interrupted sleep");
                        }
                        try {
                            response = getRouterNodeInfo(eipInfo);
                        } catch (Exception e) {
                            LOG.info("eip get result error : ", e);
                            response = new RouterInfoResponse();
                            response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                            response.setErrorInfo(eipInfo.getErrMsg());
                            ResultPool.add(eipInfo.getTaskId(), response);
                            return;
                        }

                        if (!ResultTag.RESULT_STATUS_PROCESSING.equals(response.getStatus())) {
                            break;
                        }
                    }
                    if (ResultTag.RESULT_STATUS_PROCESSING.equals(response.getStatus())) {
                        response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                        response.setErrorInfo("Search Timeout");
                    }
                    ResultPool.add(eipInfo.getTaskId(), response);
                } finally {
                    if (eipInfo != null) {
                        clearResource(eipInfo);
                    }
                }
            }
        };

        // 设置任务id
        eipInfo.setTaskId(taskId);

        resultService.execute(submitWorker);
        resultService.execute(getResultWorker);

        result.setModel(taskId);
        return result;
    }

    private void submit(FIPRouterTaskRequest req, EipInfo eipInfo) throws InvalidParamException, ClientException,
            ConfigLostException, ApplicationException {
        if (!validInput(req)) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid input for eip scenario");
        }
        boolean hasSnat = false;

        RouterContext routerContext = new RouterContext(null, req.getRemoteIp());
        String token = identityWrapperService.getToken();
        if (StringUtils.isEmpty(adminProjectId)) {
            setAdminProjectId(identityWrapperService.getTenantId());
        }
        if (!StringUtils.isEmpty(req.getVmId())) {// 使用vm id获取参数
            routerContext.setCascadeVmId(req.getVmId());
            this.getAzInfoByCascadeVmId(token, routerContext);
            String eipErr = null;
            int eipErrNum = 0;
            // getCascadePortAndFIPByVmId
            try {
                this.getCascadePortAndFIPByVmId(token, routerContext);
            } catch (ApplicationException e) {
                eipErr = e.getLocalizedMessage();
                ++eipErrNum;
                LOG.warn("adapt for eip : " + eipErr);
            }
            try {
                if (routerContext.getEip() == null || routerContext.getFip() == null) {
                    this.getCascadePortAndFIPByVmIdU30(token, routerContext);
                }
            } catch (ApplicationException e) {
                eipErr = e.getLocalizedMessage();
                ++eipErrNum;
                LOG.warn("adapt for eip : " + eipErr);
            }
            if (eipErrNum == 2) {
                throw new ApplicationException(ExceptionType.SERVER_ERR, eipErr);
            }
            // getPublicIpByFloatingIp
            try {
                this.getPublicIpByFloatingIp(token, routerContext);
            } catch (ApplicationException e) {
                LOG.warn("U30 does not need this parameter : public ip");
            }
            this.getCascadedPortbyCascadePort(token, routerContext);
        } else { // 使用public ip获取参数
            routerContext.setPublicIp(req.getPublicIp());// req.getPublicIp()
            // 就是EIP
            String eipErr = null;
            int eipErrNum = 0;
            try {
                this.getCascadePortAndFIPByEIP(token, routerContext);
            } catch (ApplicationException e) {
                eipErr = e.getLocalizedMessage();
                ++eipErrNum;
                LOG.info("adapt for eip : " + eipErr);
            }
            if (routerContext.getFip() == null || routerContext.getCascadePortId() == null) {
                try {
                    this.getCascadePortAndFIPByEIPU30(token, routerContext);
                } catch (ApplicationException e) {
                    eipErr = e.getLocalizedMessage();
                    ++eipErrNum;
                    LOG.info("adapt for eip : " + eipErr, e);
                }
            }
            if (eipErrNum == 2) {
                throw new ApplicationException(ExceptionType.SERVER_ERR, eipErr);
            }

            this.getCascadeVmIdByCascadePort(token, routerContext);
            this.getAzInfoByCascadeVmId(token, routerContext);
            this.getCascadedPortbyCascadePort(token, routerContext);
        }

        hasSnat = this.getHasSnat(token, routerContext);
        routerContext.setHasSnat(hasSnat);
        this.getPhyHostIPbyCascadeVmId(token, routerContext);
        this.findDvrRouter(token, routerContext);
        this.getGatewayPortInfo(token, routerContext);
        this.getCascadePortIdByVmIdAndIP(token, routerContext.getVmIp(), routerContext);
        this.getCascadePortIdByNetwork(token, routerContext.getVmIp(), routerContext);
        try {
            this.getSnatIp(token, routerContext);
        } catch (ApplicationException e) { // 适配无snat场景
            LOG.warn("no snat scene");
        }

        submitCnaEIPTask(eipInfo, routerContext);
        if (hasSnat) {
            submitSnatEIPTask(eipInfo, routerContext);
        }
        eipInfo.setSubmited();
    }

    /**
     * input parameter check
     *
     * @param req
     * @return boolean
     */
    private boolean validInput(FIPRouterTaskRequest req) {
        if (req == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.([0-9]+)");
        if (!pattern.matcher(req.getRemoteIp()).matches()) {
            return false;
        }
        if (!StringUtils.isEmpty(req.getPublicIp()) && !pattern.matcher(req.getPublicIp()).matches()) {
            return false;
        }
        return true;
    }

    private void submitSnatEIPTask(EipInfo eipInfo, RouterContext routerContext) {
        List<String> snatIps = routerContext.getSnatIps();
        if (snatIps == null) {
            return;
        }
        int snatNum = snatIps.size();
        String taskId = eipInfo.getTaskId();
        for (int i = 0; i < snatNum; ++i) {
            String snatTaskId = String.format(TaskTag.EIP_SNAT_WILD_TAG, taskId, i);
            doSubmitSnatEipTask(snatTaskId, routerContext, snatIps.get(i));
        }
        eipInfo.setSnatNum(snatNum);
    }

    private void doSubmitSnatEipTask(String snatTaskId, RouterContext routerContext, String snatIp) {
        FIPTaskParam snatParam = generateSnatEIPTaskParam(routerContext, snatIp);

        FIPTaskThread snatThread = new FIPTaskThread(snatTaskId, snatParam);
        Future<BaseFutureCallableResult> taskFuture = jobThreadPool.addTaskThread(snatThread);
        taskFutureMap.put(snatTaskId, taskFuture);
    }

    private FIPTaskParam generateSnatEIPTaskParam(RouterContext ctx, String snatIp) {
        CommonCallableParam commParam = generateSnatCommonParam(ctx, HostType.SNAT, snatIp);

        FIPTaskParam snatParam = new FIPTaskParam(commParam);
        // snatParam.setEip(ctx.getPublicIp());
        snatParam.setEipFloatingIp(ctx.getEip());
        snatParam.setfIp(ctx.getFip());
        snatParam.setRemoteIp(ctx.getRemoteIp());
        snatParam.setLocalScriptFile(Resource.FLOW_EIP_SNAT);
        snatParam.setRemoteScriptName(Resource.NAME_FLOW_EIP_SNAT);
        snatParam.setProvdSegmtId(ctx.getProvdSegmtId());

        return snatParam;
    }

    private CommonCallableParam generateSnatCommonParam(RouterContext ctx, String hostType, String snatIp) {
        String vmIp = null;
        String hostIp = snatIp;
        String az = ctx.getAvailabiltyZone();
        String pod = ctx.getPod();
        String publicIp = ctx.getPublicIp();
        return new CommonCallableParam(vmIp, hostIp, hostType, az, pod, publicIp);
    }

    private void submitCnaEIPTask(EipInfo eipInfo, RouterContext routerContext) {
        String taskId = eipInfo.getTaskId();
        FIPTaskParam cnaParam = generalCnaEipTaskParam(routerContext);
        String cnaTaskId = taskId + TaskTag.EIP_CNA_TAG;
        FIPTaskThread cnaEipThread = new FIPTaskThread(cnaTaskId, cnaParam);
        Future<BaseFutureCallableResult> taskFuture = jobThreadPool.addTaskThread(cnaEipThread);
        taskFutureMap.put(cnaTaskId, taskFuture);
    }

    private FIPTaskParam generalCnaEipTaskParam(RouterContext routerContext) {
        CommonCallableParam cnaCommParam = generateCnaCommonParam(routerContext, HostType.CNA);
        FIPTaskParam cnaParam = new FIPTaskParam(cnaCommParam);
        cnaParam.setDvrDevId(routerContext.getDvrDevId());
        cnaParam.setDvrPort(routerContext.getDvrSrcPort());
        cnaParam.setfIp(routerContext.getFip());
        cnaParam.setEipFloatingIp(routerContext.getEip());
        cnaParam.getCommonParam().setHostIp(routerContext.getHostIp());
        cnaParam.setQvmPort(routerContext.getQvmPort());
        cnaParam.setVmIp(routerContext.getVmIp());
        cnaParam.setRemoteIp(routerContext.getRemoteIp());
        cnaParam.setHasSnat(routerContext.getHasSnat());
        cnaParam.setDvrMac(routerContext.getDvrMac());
        cnaParam.setFipNsId(routerContext.getFipNsId());
        cnaParam.setFgMac(routerContext.getFgMac());
        cnaParam.setFipPortId(routerContext.getFipPortId());
        cnaParam.setLocalScriptFile(Resource.FLOW_EIP_CNA);
        cnaParam.setRemoteScriptName(Resource.NAME_FLOW_EIP_CNA);
        return cnaParam;
    }

    private CommonCallableParam generateCnaCommonParam(RouterContext ctx, String hostType) {
        String vmIp = ctx.getVmIp();
        String hostIp = ctx.getHostIp();
        String az = ctx.getAvailabiltyZone();
        String pod = ctx.getPod();
        String publicIp = ctx.getPublicIp();
        return new CommonCallableParam(vmIp, hostIp, hostType, az, pod, publicIp);
    }

    private RouterInfoResponse getRouterNodeInfo(EipInfo eipInfo) {
        RouterInfoResponse result = new RouterInfoResponse();
        String taskId = eipInfo.getTaskId();

        Map<String, List<NodeInfo>> cnaInfo = null;
        Map<String, List<NodeInfo>> snatInfo = null;

        boolean submitedSnatTask = false;
        try {
            cnaInfo = getCnaRouteInfo(eipInfo);
            if (null == cnaInfo) {
                LOG.info(String.format("eip cna task is processing : %s", taskId));
                result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                return result;
            }
            submitedSnatTask = hasSnatTask(eipInfo);
            if (submitedSnatTask) {
                snatInfo = getSnatInfo(eipInfo);
                if (null == snatInfo) {
                    LOG.info(String.format("eip snat task is processing : %s", taskId));
                    result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                    return result;
                }
            }
        } catch (ApplicationException e) {
            String errorInfo = e.toString();
            LOG.error(errorInfo);
            result.setStatus(ResultTag.RESULT_STATUS_ERROR);
            result.setErrorInfo(errorInfo);
            return result;
        }

        Collections.reverse(cnaInfo.get(ResultTag.FLOW_TAG_INPUT));
        result.setCnaInputRouter(cnaInfo.get(ResultTag.FLOW_TAG_INPUT));
        // 脚本特殊处理，需要eip场景对近流量反序
        result.setCnaOutputRouter(cnaInfo.get(ResultTag.FLOW_TAG_OUTPUT));
        if (submitedSnatTask) {
            result.setSnatInput(snatInfo.get(ResultTag.FLOW_TAG_INPUT));
            result.setSnatOutput(snatInfo.get(ResultTag.FLOW_TAG_OUTPUT));
        }

        result.setStatus(ResultTag.RESULT_STATUS_END);
        return result;
    }

    private boolean hasSnatTask(EipInfo eipInfo) {
        if (null != taskFutureMap.get(eipInfo.getTaskId() + TaskTag.EIP_SNAT_START_TAG)) {
            return true;
        }
        return false;
    }

    private Map<String, List<NodeInfo>> getCnaRouteInfo(EipInfo eipInfo) throws ApplicationException {
        String taskId = eipInfo.getTaskId();
        String cnaTaskId = taskId + TaskTag.EIP_CNA_TAG;
        String cnaFileName = String.format(LogFileFormat.EIP_CNA_LOG, cnaTaskId);
        Map<String, List<NodeInfo>> l3Info;
        try {
            l3Info = ThreadUtil.getOneHostLogInfo(cnaTaskId, cnaFileName, taskFutureMap.get(cnaTaskId), HostType.CNA);
        } catch (ScriptException e) {
            throw new ApplicationException(e.getType(), e.getLocalizedMessage());
        }
        return l3Info;
    }

    private Map<String, List<NodeInfo>> getSnatInfo(EipInfo eipInfo) throws ApplicationException {
        String taskId = eipInfo.getTaskId();
        int snatTaskNum = eipInfo.getSnatNum();
        List<String> snatTaskIds = new ArrayList<String>(snatTaskNum);
        for (int i = 0; i < snatTaskNum; ++i) {
            String snatTaskId = String.format(TaskTag.EIP_SNAT_WILD_TAG, taskId, i);
            if (taskFutureMap.get(snatTaskId) != null) {
                snatTaskIds.add(snatTaskId);
            }
        }
        Map<String, List<NodeInfo>> snatInfo = getHostLogInfo(taskId, snatTaskIds, LogFileFormat.EIP_SNAT_LOG,
                HostType.SNAT);
        return snatInfo;
    }

    private void clearResource(EipInfo eipInfo) {
        String taskId = eipInfo.getTaskId();
        try {
            clearCnaTmpResource(eipInfo);
        } catch (Exception e) {
            LOG.error("eip task:" + taskId + "\nfail to clear cna temp resources : ", e);
        }
        try {
            clearSnatTmpResource(eipInfo);
        } catch (Exception e) {
            LOG.error("eip task:" + taskId + "\nfail to clear snat temp resources : ", e);
        }

    }

    private void clearCnaTmpResource(EipInfo eipInfo) {
        String taskId = eipInfo.getTaskId();
        String cnaTaskId = taskId + TaskTag.EIP_CNA_TAG;
        if (taskFutureMap.get(cnaTaskId) != null) {
            String cnaFileName = String.format(LogFileFormat.EIP_CNA_LOG, cnaTaskId);
            try {
                ThreadUtil.cleanTempFile(cnaTaskId, cnaFileName, taskFutureMap.get(cnaTaskId).get(), HostType.CNA);
            } catch (Exception e) {
                LOG.error("eip task:" + cnaTaskId + "\nfail to clear temp resources : ", e);
            }
            taskFutureMap.remove(cnaTaskId);
        }
    }

    private void clearSnatTmpResource(EipInfo eipInfo) {
        String taskId = eipInfo.getTaskId();
        int snatNum = eipInfo.getSnatNum();
        for (int i = 0; i < snatNum; ++i) {
            String snatTaskId = String.format(TaskTag.EIP_SNAT_WILD_TAG, taskId, i);
            if (taskFutureMap.get(snatTaskId) != null) {
                String snatFileName = String.format(LogFileFormat.EIP_SNAT_LOG, snatTaskId);
                try {
                    ThreadUtil.cleanTempFile(snatTaskId, snatFileName, taskFutureMap.get(snatTaskId).get(),
                            HostType.SNAT);
                } catch (Exception e) {
                    LOG.error("eip task:" + snatTaskId + "\nfail to clear temp resources : ", e);
                }
                taskFutureMap.remove(snatTaskId);
            }
        }
    }

}
