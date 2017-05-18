package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.NodeInfo;
import com.huawei.blackhole.network.api.bean.RouterInfoResponse;
import com.huawei.blackhole.network.api.bean.RouterTaskRequest;
import com.huawei.blackhole.network.api.resource.EwInfo;
import com.huawei.blackhole.network.api.resource.ResultPool;
import com.huawei.blackhole.network.common.constants.Constants;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.HostType;
import com.huawei.blackhole.network.common.constants.LogFileFormat;
import com.huawei.blackhole.network.common.constants.ResultTag;
import com.huawei.blackhole.network.common.constants.TaskTag;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.BaseException;
import com.huawei.blackhole.network.common.exception.InvalidParamException;
import com.huawei.blackhole.network.common.exception.ScriptException;
import com.huawei.blackhole.network.common.utils.ThreadUtil;
import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.CommonCallableParam;
import com.huawei.blackhole.network.core.bean.EwL2GWParam;
import com.huawei.blackhole.network.core.bean.EwTaskParam;
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.bean.RouterContext;
import com.huawei.blackhole.network.core.thread.ChkflowServiceStartup;
import com.huawei.blackhole.network.core.thread.EWTaskThread;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注意：src 和 dst 分别表示src端查到的信息，dst表示dst端查到的信息
 */
@Service("ewRouterService")
public class EwRouterServiceImpl extends BaseRouterService implements EwRouterService {
    private static final Logger LOG = LoggerFactory.getLogger(EwRouterServiceImpl.class);

    @Override
    public Result<String> submitEwTask(final RouterTaskRequest req, String taskId) {
        Result<String> result = new Result<String>();

        // 检查是否正在重启
        if (!ChkflowServiceStartup.isStarted()) {
            String msg = "OpsNetworkChkFlow is restarting, please wait for a moment";
            result.addError("", msg);
            LOG.error("EW task submit failed :" + msg);
            return result;
        }

        // 清理过期未取回任务
        ResultPool.clearOvertimeRecord();
        final EwInfo ewInfo = new EwInfo();

        Runnable submitWorker = new Runnable() { // 提交任务的task
            @Override
            public void run() {
                try {
                    submit(req, ewInfo);
                } catch (InvalidParamException | ApplicationException e) {
                    ewInfo.errHappened();
                    String msg = e.toString();
                    result.addError("", msg);
                    ewInfo.setErrMsg("EW task failed : " + msg);
                    LOG.error(msg, e);
                }
            }
        };

        Runnable getResultWorker = new Runnable() { // 获取任务结果的task
            @Override
            public void run() {
                RouterInfoResponse response = null;
                try {
                    // 等待提交
                    for (int i = 0; i < Constants.SUBMIT_NUMBER; i++) {
                        if (ewInfo.submited() || ewInfo.hasErr()) {
                            break;
                        }
                        try {
                            Thread.sleep(Constants.SEARCH_INTERVAL);
                        } catch (InterruptedException e) {
                            LOG.warn("ignore : interrupted sleep");
                        }
                        LOG.info("ew task submitting");
                    }
                    if (ewInfo.hasErr()) {
                        LOG.info("exception happened before submit ew task");
                        response = new RouterInfoResponse();
                        response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                        response.setErrorInfo(ewInfo.getErrMsg());
                        ResultPool.add(ewInfo.getTaskId(), response);
                        return;
                    }

                    boolean submited = ewInfo.submited();
                    // 等待脚本执行结束
                    for (int i = 0; i < Constants.SEARCH_NUMBER && submited; ++i) {
                        try {
                            Thread.sleep(Constants.SEARCH_INTERVAL);
                        } catch (InterruptedException e) {
                            LOG.info("ignore : interrupted sleep for EW task - " + taskId);
                        }
                        try {
                            response = getRouterNodeInfo(ewInfo);
                        } catch (Exception e) {
                            LOG.info("ew get result error : ", e);
                            response = new RouterInfoResponse();
                            response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                            response.setErrorInfo(ewInfo.getErrMsg());
                            ResultPool.add(ewInfo.getTaskId(), response);
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
                    ResultPool.add(ewInfo.getTaskId(), response);
                } finally {
                    if (ewInfo != null) {
                        clearResource(ewInfo);
                    }
                }
            }
        };

        // 设置任务id
        ewInfo.setTaskId(taskId);

        resultService.execute(submitWorker);
        resultService.execute(getResultWorker);

        result.setModel(taskId);
        return result;
    }

    /**
     * input parameter check
     *
     * @param req
     * @param srcRouterContext
     * @param destRouterContext
     * @return boolean
     */
    private boolean checkInit(RouterTaskRequest req, RouterContext srcRouterContext, RouterContext destRouterContext) {
        if (req == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)\\.([0-9]+)");
        Matcher srcMatcher = pattern.matcher(srcRouterContext.getVmIp());
        Matcher dstMatcher = pattern.matcher(destRouterContext.getVmIp());
        if (!srcMatcher.matches() || !dstMatcher.matches()) {
            return false;
        }
        if (!StringUtils.isEmpty(req.getSrcVmId()) && !StringUtils.isEmpty(req.getDstVmId())) {
            srcRouterContext.setCascadeVmId(req.getSrcVmId());
            destRouterContext.setCascadeVmId(req.getDstVmId());
            return true;
        } else if (!StringUtils.isEmpty(req.getSrcNetId()) && !StringUtils.isEmpty(req.getDstNetId())) {
            srcRouterContext.setCascadeNetworkId(req.getSrcNetId());
            destRouterContext.setCascadeNetworkId(req.getDstNetId());
            return true;
        }
        return false;
    }

    private void submit(RouterTaskRequest req, EwInfo ewInfo) throws InvalidParamException, ApplicationException {
        String srcVmIp = req.getSrcVmIp();
        String dstVmIp = req.getDstVmIp();

        RouterContext srcRouterContext = new RouterContext(srcVmIp);
        RouterContext dstRouterContext = new RouterContext(dstVmIp);

        // 1.input parameters check
        if (!checkInit(req, srcRouterContext, dstRouterContext)) {
            throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid input for vm-vm scenario");
        }

        boolean isInSameAz = false;
        try {
            // 2.acquire token
            String token = identityWrapperService.getToken();
            if (StringUtils.isEmpty(adminProjectId)) {
                setAdminProjectId(identityWrapperService.getTenantId());
            }
            // 3.get cascading port id by vmId+ip or networkid+ip
            if (!StringUtils.isEmpty(srcRouterContext.getCascadeVmId())) {
                this.getCascadePortIdByVmIdAndIP(token, srcVmIp, srcRouterContext);
                this.getCascadePortIdByVmIdAndIP(token, dstVmIp, dstRouterContext);
            }

            this.getCascadePortIdByNetwork(token, srcVmIp, srcRouterContext);
            this.getCascadePortIdByNetwork(token, dstVmIp, dstRouterContext);

            this.getMacInfobyCascadePort(token, srcRouterContext);
            this.getMacInfobyCascadePort(token, dstRouterContext);

            // 4.get cascaded layer of srcVm and dstVm
            this.getAzInfoByCascadeVmId(token, srcRouterContext);
            this.getAzInfoByCascadeVmId(token, dstRouterContext);

            // 5.get cascaded port by cascading port and cascaded layer
            this.getCascadedPortbyCascadePort(token, srcRouterContext);
            this.getCascadedPortbyCascadePort(token, dstRouterContext);

            // 6.在被级联层相应的vrm中查找物理主机
            this.getPhyHostIPbyCascadeVmId(token, srcRouterContext);
            this.getPhyHostIPbyCascadeVmId(token, dstRouterContext);

            // 总是获取dvr 信息
            this.findCascadedRouterofSrcAndDest(token, srcRouterContext, dstRouterContext);
            this.findSrcAndDestPortOfOneRouter(token, srcRouterContext);
            this.findSrcAndDestPortOfOneRouter(token, dstRouterContext);

            this.findDvrRouter(token, srcRouterContext);
            this.findDvrRouter(token, dstRouterContext);

            // 9.获取l2gw的ip
            isInSameAz = inSameAz(srcRouterContext, dstRouterContext);
            srcRouterContext.setIsInSameAz(isInSameAz);
            dstRouterContext.setIsInSameAz(isInSameAz);
            if (!isInSameAz) {
                this.getL2GWIp(token, srcRouterContext);
                this.getL2GWIp(token, dstRouterContext);
                this.getL2gwVtepIp(token, srcRouterContext);
                this.getL2gwVtepIp(token, dstRouterContext);
            }

            // 不同cna
            if (!StringUtils.equals(srcRouterContext.getHostIp(), dstRouterContext.getHostIp())) {
                this.getCNAVtepIp(token, srcRouterContext);
                this.getCNAVtepIp(token, dstRouterContext);
            }

        } catch (BaseException e) {
            if ("OS_AUTH_URL".equals(e.getLocalizedMessage())) {
                throw new ApplicationException(e.getType(), "authentication failed: check you configurations for fsp");
            } else {
                throw new ApplicationException(e.getType(), e.getLocalizedMessage());
            }
        }

        submitCnaGeneralTask(ewInfo, srcRouterContext, dstRouterContext);
        if (!isInSameAz) {
            submitL2gwTask(ewInfo, srcRouterContext, dstRouterContext);
        }
        ewInfo.setSubmited();
    }

    private void submitCnaGeneralTask(EwInfo ewInfo, RouterContext srcRouterContext, RouterContext destRouterContext) {
        String taskId = ewInfo.getTaskId();
        EwTaskParam srcVmParam = generateCnaTaskParam(srcRouterContext);
        EwTaskParam dstVmParam = generateCnaTaskParam(destRouterContext);

        String srcCnaTaskId = taskId + TaskTag.EW_CNA_SRC_TAG;
        String dstCnaTaskId = taskId + TaskTag.EW_CNA_DST_TAG;

        EWTaskThread srcCnaVmTask = new EWTaskThread(srcCnaTaskId, srcVmParam, dstVmParam);
        EWTaskThread dstCnaVmTask = new EWTaskThread(dstCnaTaskId, dstVmParam, srcVmParam);

        Future<BaseFutureCallableResult> srcCnaTaskFuture = jobThreadPool.addTaskThread(srcCnaVmTask);
        Future<BaseFutureCallableResult> dstCnaTaskFuture = jobThreadPool.addTaskThread(dstCnaVmTask);

        taskFutureMap.put(srcCnaTaskId, srcCnaTaskFuture);
        taskFutureMap.put(dstCnaTaskId, dstCnaTaskFuture);
    }

    private EwTaskParam generateCnaTaskParam(RouterContext routerContext) {
        EwTaskParam vmTaskParam = new EwTaskParam(new CommonCallableParam(routerContext.getVmIp(),
                routerContext.getHostIp(), HostType.CNA, routerContext.getAvailabiltyZone(), routerContext.getPod()));
        vmTaskParam.setDvrDevId(routerContext.getDvrDevId());
        vmTaskParam.setDvrDstPort(routerContext.getDvrDestPort());
        vmTaskParam.setDvrDstRouteIp(routerContext.getDvrDestRouteIp());
        vmTaskParam.setDvrSrcPort(routerContext.getDvrSrcPort());
        vmTaskParam.setDvrSrcRouteIp(routerContext.getDvrSrcRouteIp());
        vmTaskParam.setMacAddr(routerContext.getVmMac());
        vmTaskParam.getCommonParam().setHostIp(routerContext.getHostIp());
        vmTaskParam.setQvmPort(routerContext.getQvmPort());
        vmTaskParam.setSubnetId(routerContext.getSubnetId());
        vmTaskParam.setL2gwIp(routerContext.getL2gwIps());
        vmTaskParam.setL2gwVtepIp(routerContext.getL2gwVtepIp());
        vmTaskParam.setVtepIp(routerContext.getVtepIp());
        vmTaskParam.setIsInSameAz(routerContext.getIsInSameAz());
        vmTaskParam.setDvrSrcMac(routerContext.getDvrSrcMac());
        vmTaskParam.setDvrDstMac(routerContext.getDvrDstMac());
        return vmTaskParam;
    }

    private void submitL2gwTask(EwInfo ewInfo, RouterContext srcCtx, RouterContext dstCtx) {
        String taskId = ewInfo.getTaskId();
        List<String> srclgwIps = srcCtx.getL2gwIps();
        doSubmitL2gw(taskId, srcCtx, dstCtx, srclgwIps, TaskTag.SRC, Constants.NODE_FLAG_INPUT);
        ewInfo.setSrcTaskNum(srclgwIps.size());

        List<String> dstlgwIps = dstCtx.getL2gwIps();
        doSubmitL2gw(taskId, srcCtx, dstCtx, dstlgwIps, TaskTag.DST, Constants.NODE_FLAG_OUTPUT);
        ewInfo.setDstTaskNum(dstlgwIps.size());
    }

    private void doSubmitL2gw(String taskId, RouterContext srcCtx, RouterContext dstCtx, List<String> lgwIps,
                              String tag, String nodeFlag) {
        int lgwIpNum = lgwIps.size();
        for (int i = 0; i < lgwIpNum; i++) {
            EwL2GWParam vmL2GWParam = generateVmL2GWParam(srcCtx, dstCtx, lgwIps.get(i), nodeFlag);
            String l2gwTaskId = String.format(TaskTag.EW_L2GW_WILD_TAG, taskId, tag, i);
            EWTaskThread l2GWVmTask = new EWTaskThread(l2gwTaskId, vmL2GWParam);

            Future<BaseFutureCallableResult> l2GWTaskFuture = jobThreadPool.addTaskThread(l2GWVmTask);
            taskFutureMap.put(l2gwTaskId, l2GWTaskFuture);
        }
    }

    private EwL2GWParam generateVmL2GWParam(RouterContext srcCtx, RouterContext dstCtx, String lgwIp, String nodeFlag) {
        String vmIp = null; // l2gw does not need this information
        String hostIp = lgwIp;
        String hostType = HostType.L2GW;
        String az = null;
        String pod = null;
        String l2gwVtepIp = null;
        if (Constants.NODE_FLAG_INPUT.equals(nodeFlag)) {
            az = srcCtx.getAvailabiltyZone();
            pod = srcCtx.getPod();
            l2gwVtepIp = srcCtx.getL2gwVtepIp();
        } else {
            az = dstCtx.getAvailabiltyZone();
            pod = dstCtx.getPod();
            l2gwVtepIp = dstCtx.getL2gwVtepIp();
        }

        CommonCallableParam commonPara = new CommonCallableParam(vmIp, hostIp, hostType, az, pod);

        EwL2GWParam lgwParam = new EwL2GWParam(commonPara);

        lgwParam.setSrcVmIp(srcCtx.getVmIp());
        lgwParam.setSrcVmMac(srcCtx.getVmMac());
        lgwParam.setSrcVtepIp(srcCtx.getVtepIp());

        lgwParam.setDstVmIp(dstCtx.getVmIp());
        lgwParam.setDstVmMac(dstCtx.getVmMac());
        lgwParam.setDstVtepIp(dstCtx.getVtepIp());

        lgwParam.setL2gwVtepIp(l2gwVtepIp);
        lgwParam.setNodeFlag(nodeFlag);

        lgwParam.setSrcProvdSegmtId(srcCtx.getProvdSegmtId());
        lgwParam.setDstProvdSegmtId(dstCtx.getProvdSegmtId());

        return lgwParam;
    }

    private RouterInfoResponse getRouterNodeInfo(EwInfo ewInfo) {
        RouterInfoResponse result = new RouterInfoResponse();

        String taskId = ewInfo.getTaskId();
        boolean needL2gw = needL2GW(ewInfo);

        String cnaSrcTaskId = taskId + TaskTag.EW_CNA_SRC_TAG;
        String cnaDstTaskId = taskId + TaskTag.EW_CNA_DST_TAG;

        String cnaSrcLogFileName = String.format(LogFileFormat.EW_CNA_LOG, cnaSrcTaskId);
        String cnaDstLogFileName = String.format(LogFileFormat.EW_CNA_LOG, cnaDstTaskId);

        Map<String, List<NodeInfo>> cnaRouterInfo = null;

        List<NodeInfo> lgwSrcInputList = null;
        List<NodeInfo> lgwSrcOputList = null;
        List<NodeInfo> lgwDstInputList = null;
        List<NodeInfo> lgwDstOputList = null;

        try {
            cnaRouterInfo = getCnaRouterInfo(cnaSrcTaskId, cnaDstTaskId, cnaSrcLogFileName, cnaDstLogFileName);
            if (null == cnaRouterInfo) {
                LOG.info(String.format("ew cna task is processing : %s", taskId));
                result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                return result;
            }

            if (needL2gw) {
                lgwSrcInputList = new ArrayList<>();
                lgwSrcOputList = new ArrayList<>();
                lgwDstInputList = new ArrayList<>();
                lgwDstOputList = new ArrayList<>();
                getL2GWNodeInfo(ewInfo, lgwSrcInputList, lgwSrcOputList, ResultTag.FLOW_TAG_SRC);
                getL2GWNodeInfo(ewInfo, lgwDstInputList, lgwDstOputList, ResultTag.FLOW_TAG_DST);
                if (lgwSrcInputList.size() == 0 || lgwDstOputList.size() == 0) {
                    LOG.info(String.format("ew l2gw task is processing : %s", taskId));
                    result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                    return result;
                }
            }

        } catch (ApplicationException e) {
            String errorInfo = e.toString();
            LOG.error(errorInfo, e);
            result.setErrorInfo(errorInfo);
            result.setStatus(ResultTag.RESULT_STATUS_ERROR);
            return result;
        }

        result.setCnaInputRouter(cnaRouterInfo.get(ResultTag.FLOW_TAG_INPUT));
        result.setCnaOutputRouter(cnaRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT));
        result.setL2gwInput(lgwSrcInputList);
        result.setL2gwOutput(lgwDstOputList);
        patchVmIp(result);

        result.setStatus(ResultTag.RESULT_STATUS_END);
        return result;
    }

    private Map<String, List<NodeInfo>> getCnaRouterInfo(String srcTaskId, String dstTaskId, String srcLogFileName,
                                                         String dstLogFileName) throws ApplicationException {

        Map<String, List<NodeInfo>> srcRouterInfo = null;
        Map<String, List<NodeInfo>> dstRouterInfo = null;

        try {
            srcRouterInfo = ThreadUtil.getOneHostLogInfo(srcTaskId, srcLogFileName, taskFutureMap.get(srcTaskId),
                    HostType.CNA);
            dstRouterInfo = ThreadUtil.getOneHostLogInfo(dstTaskId, dstLogFileName, taskFutureMap.get(dstTaskId),
                    HostType.CNA);
        } catch (ScriptException e) {
            throw new ApplicationException(e.getType(), e.getLocalizedMessage());
        }

        if (null == srcRouterInfo || null == dstRouterInfo) {
            return null;
        }

        // output -------------------------------------------------------
        List<NodeInfo> outputResult = new ArrayList<NodeInfo>();
        List<NodeInfo> inputResult = new ArrayList<NodeInfo>();
        // List<NodeInfo>

        // src
        for (NodeInfo nodeInfo : srcRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT)) {
            outputResult.add(nodeInfo);
        }
        Collections.reverse(dstRouterInfo.get(ResultTag.FLOW_TAG_INPUT));
        for (NodeInfo nodeInfo : dstRouterInfo.get(ResultTag.FLOW_TAG_INPUT)) {
            outputResult.add(nodeInfo);
        }

        // input -------------------------------------------------------
        for (NodeInfo nodeInfo : srcRouterInfo.get(ResultTag.FLOW_TAG_INPUT)) {
            inputResult.add(nodeInfo);
        }

        Collections.reverse(dstRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT));
        for (NodeInfo nodeInfo : dstRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT)) {
            inputResult.add(nodeInfo);
        }

        Map<String, List<NodeInfo>> resultMap = new HashMap<String, List<NodeInfo>>();
        resultMap.put(ResultTag.FLOW_TAG_OUTPUT, outputResult);
        resultMap.put(ResultTag.FLOW_TAG_INPUT, inputResult);
        return resultMap;
    }

    /**
     * tag : src/dst
     *
     * @param inputList
     * @param outputList
     * @param tag
     * @return
     * @throws ApplicationException
     */
    private void getL2GWNodeInfo(EwInfo ewInfo, List<NodeInfo> inputList, List<NodeInfo> outputList, String tag)
            throws ApplicationException {
        List<String> srcL2gwTaskIds = new ArrayList<>();

        String taskId = ewInfo.getTaskId();
        int l2gwTaskNum = getL2gwNum(ewInfo, tag);
        for (int index = 0; index < l2gwTaskNum; index++) {
            String tmpL2gwTaskId = String.format(TaskTag.EW_L2GW_WILD_TAG, taskId, tag, index);
            if (taskFutureMap.get(tmpL2gwTaskId) != null) {
                srcL2gwTaskIds.add(tmpL2gwTaskId);
            }
        }
        // when there are l2gw task
        Map<String, List<NodeInfo>> lgwRouterInfo = null;
        try {
            lgwRouterInfo = getHostLogInfo(taskId, srcL2gwTaskIds, LogFileFormat.EW_L2GW_LOG, HostType.L2GW);
        } catch (ApplicationException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw e;
        }
        if (lgwRouterInfo != null) {
            if (lgwRouterInfo.get(ResultTag.FLOW_TAG_INPUT) != null) {
                inputList.addAll(lgwRouterInfo.get(ResultTag.FLOW_TAG_INPUT));
            }
            if (lgwRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT) != null) {
                outputList.addAll(lgwRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT));
            }
        }

        // l2gw 脚本不能按照要求的顺序返回，故需要反向
        Collections.reverse(inputList);
    }

    private void clearResource(EwInfo ewInfo) {
        String taskId = ewInfo.getTaskId();
        try {
            clearCnaTaskTempResource(ewInfo);
        } catch (Exception e) {
            String cnaErr = "ew task:" + taskId + "\nfail to clear CNA temp resources : " + e.getLocalizedMessage();
            LOG.error(cnaErr, e);
        }
        boolean needL2gw = needL2GW(ewInfo);
        if (needL2gw) {
            try {
                clearL2GWtaskTmpResource(ewInfo, ResultTag.FLOW_TAG_SRC);
            } catch (Exception e) {
                String l2GWErr = "ew task:" + taskId + "\nfail to clear L2GW temp resources : "
                        + e.getLocalizedMessage();
                LOG.error(l2GWErr, e);
            }
            try {
                clearL2GWtaskTmpResource(ewInfo, ResultTag.FLOW_TAG_DST);
            } catch (Exception e) {
                String l2GWErr = "ew task:" + taskId + "\nfail to clear L2GW temp resources : " + e.getLocalizedMessage();
                LOG.error(l2GWErr, e);
            }
        }
    }

    private void clearCnaTaskTempResource(EwInfo ewInfo) {
        String taskId = ewInfo.getTaskId();
        String srcCnaTaskId = taskId + TaskTag.EW_CNA_SRC_TAG;
        String dstCnaTaskId = taskId + TaskTag.EW_CNA_DST_TAG;

        if (taskFutureMap.get(srcCnaTaskId) != null) {
            String srcCnaLog = String.format(LogFileFormat.EW_CNA_LOG, srcCnaTaskId);
            try {
                ThreadUtil.cleanTempFile(srcCnaTaskId, srcCnaLog, taskFutureMap.get(srcCnaTaskId).get(), HostType.CNA);
            } catch (Exception e) {
                LOG.error("ew task:" + srcCnaTaskId + "\nfail to clear temp resources :" , e);
            }
            taskFutureMap.remove(srcCnaTaskId);
        }
        if (taskFutureMap.get(dstCnaTaskId) != null) {
            String dstCnaLog = String.format(LogFileFormat.EW_CNA_LOG, dstCnaTaskId);
            try {
                ThreadUtil.cleanTempFile(dstCnaTaskId, dstCnaLog, taskFutureMap.get(dstCnaTaskId).get(), HostType.CNA);
            } catch (Exception e) {
                LOG.error("ew  task:" + dstCnaTaskId + "\nfail to clear temp resources :", e);
            }
            taskFutureMap.remove(dstCnaTaskId);
        }

    }

    private void clearL2GWtaskTmpResource(EwInfo ewInfo, String tag) {
        String taskId = ewInfo.getTaskId();
        int l2gwTaskNum = getL2gwNum(ewInfo, tag);
        String logFileFormat = LogFileFormat.EW_L2GW_LOG;
        for (int i = 0; i < l2gwTaskNum; ++i) {
            String l2gwTaskId = String.format(TaskTag.EW_L2GW_WILD_TAG, taskId, tag, i);
            if (taskFutureMap.get(l2gwTaskId) != null) {
                String logFileName = String.format(logFileFormat, l2gwTaskId);
                try {
                    ThreadUtil.cleanTempFile(l2gwTaskId, logFileName, taskFutureMap.get(l2gwTaskId).get(),
                            HostType.L2GW);
                } catch (Exception e) {
                    LOG.error("ew task:" + l2gwTaskId + "\nfail to clear temp resources : " , e);
                }
                taskFutureMap.remove(l2gwTaskId);
            }
        }
    }

    private int getL2gwNum(EwInfo ewInfo, String tag) {
        if (ResultTag.FLOW_TAG_SRC.equals(tag)) {
            return ewInfo.getSrcTaskNum();
        } else {
            return ewInfo.getDstTaskNum();
        }
    }

    private boolean needL2GW(EwInfo ewInfo) {
        String taskId = ewInfo.getTaskId();
        return null != taskFutureMap.get(taskId + TaskTag.EW_L2GW_START_TAG);
    }

    private boolean isSameCNA(RouterInfoResponse rsp) {
        List<NodeInfo> nodeList = rsp.getCnaOutputRouter();
        return nodeList.get(0).getHostIp().equalsIgnoreCase(nodeList.get(nodeList.size() - 1).getHostIp());
    }

    private boolean inSameAz(RouterContext srcRouterContext, RouterContext destRouterContext) {
        return StringUtils.equals(srcRouterContext.getAvailabiltyZone(), destRouterContext.getAvailabiltyZone());
    }

    private void patchVmIp(RouterInfoResponse rsp) {
        if (!isSameCNA(rsp)) {
            return;
        }

        List<NodeInfo> nodeList = rsp.getCnaOutputRouter();
        String vmIp = nodeList.get(0).getVmIp() + "," + nodeList.get(nodeList.size() - 1).getVmIp();
        List<NodeInfo> l3OutputRouter = rsp.getCnaOutputRouter();
        List<NodeInfo> l3InputRouter = rsp.getCnaInputRouter();
        if (null != l3OutputRouter) {
            for (NodeInfo node : l3OutputRouter) {
                node.setVmIp(vmIp);
            }
        }
        if (null != l3InputRouter) {
            for (NodeInfo node : l3InputRouter) {
                node.setVmIp(vmIp);
            }
        }
    }
}
