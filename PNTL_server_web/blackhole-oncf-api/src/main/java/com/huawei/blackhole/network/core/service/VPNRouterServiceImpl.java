package com.huawei.blackhole.network.core.service;

import com.huawei.blackhole.network.api.bean.NodeInfo;
import com.huawei.blackhole.network.api.bean.RouterInfoResponse;
import com.huawei.blackhole.network.api.bean.VPNRouterTaskRequest;
import com.huawei.blackhole.network.api.resource.ResultPool;
import com.huawei.blackhole.network.api.resource.VpnInfo;
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
import com.huawei.blackhole.network.core.bean.Result;
import com.huawei.blackhole.network.core.bean.RouterContext;
import com.huawei.blackhole.network.core.bean.VPNTaskParam;
import com.huawei.blackhole.network.core.thread.ChkflowServiceStartup;
import com.huawei.blackhole.network.core.thread.VPNTaskThread;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Service("vpnRouterService")
public class VPNRouterServiceImpl extends BaseRouterService implements VPNRouterService {
    private static final Logger LOG = LoggerFactory.getLogger(VPNRouterServiceImpl.class);

    @Override
    public Result<String> submitVpnTask(final VPNRouterTaskRequest req, String taskId) {
        Result<String> result = new Result<String>();

        // 检查是否正在重启
        if (!ChkflowServiceStartup.isStarted()) {
            String msg = "OpsNetworkChkFlow is restarting, please wait for a moment";
            result.addError("", msg);
            LOG.error("VPN task waiting : " + msg);
            return result;
        }

        // 清理过期未取回任务
        ResultPool.clearOvertimeRecord();
        final VpnInfo vpnInfo = new VpnInfo();

        Runnable submitWorker = new Runnable() { // 提交任务的task
            @Override
            public void run() {
                try {
                    submit(req, vpnInfo);
                    LOG.info("task submit success : " + taskId);
                } catch (InvalidParamException | ApplicationException | ClientException | ConfigLostException e) {
                    vpnInfo.errHappened();
                    String msg = "VPN task failed : " + e.toString();
                    result.addError("", msg);
                    vpnInfo.setErrMsg(msg);
                    LOG.error(msg, e);
                }
            }
        };

        Runnable backWorker = new Runnable() {
            @Override
            public void run() {
                RouterInfoResponse response = null;
                try {
                    for (int i = 0; i < Constants.SUBMIT_NUMBER; i++) {
                        if (vpnInfo.submited() || vpnInfo.hasErr()) {
                            break;
                        }
                        try {
                            Thread.sleep(Constants.SEARCH_INTERVAL);
                        } catch (InterruptedException e) { // nothing
                            LOG.warn("ignore : interrupted sleep");
                        }
                        LOG.info("vpn task submitting");
                    }

                    if (vpnInfo.hasErr()) {
                        LOG.info("exception happened before submit vpn task");
                        response = new RouterInfoResponse();
                        response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                        response.setErrorInfo(vpnInfo.getErrMsg());
                        ResultPool.add(vpnInfo.getTaskId(), response);
                        return;
                    }

                    boolean submited = vpnInfo.submited();
                    // 等待脚本执行结束
                    for (int i = 0; i < Constants.SEARCH_NUMBER && submited; ++i) {
                        try {
                            Thread.sleep(Constants.SEARCH_INTERVAL);
                        } catch (Exception e) { // nothing
                            LOG.warn("ignore : interrupted sleep for VPN task" + taskId);
                        }
                        try {
                            response = getRouterNodeInfo(vpnInfo);
                        } catch (Exception e) {
                            LOG.info("vpn get result error : ", e);
                            response = new RouterInfoResponse();
                            response.setStatus(ResultTag.RESULT_STATUS_ERROR);
                            response.setErrorInfo(vpnInfo.getErrMsg());
                            ResultPool.add(vpnInfo.getTaskId(), response);
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
                    ResultPool.add(vpnInfo.getTaskId(), response);
                } finally {
                    if (vpnInfo != null) {
                        clearResource(vpnInfo);
                    }
                }
            }
        };

        // 设置任务id
        vpnInfo.setTaskId(taskId);

        resultService.execute(backWorker);
        resultService.execute(submitWorker);

        result.setModel(taskId);
        return result;
    }

    private void submit(VPNRouterTaskRequest req, VpnInfo vpnInfo)
            throws InvalidParamException, ClientException, ConfigLostException, ApplicationException {
        try {
            String vmIp = req.getVmIp();
            String remoteIp = req.getRemoteIp();
            RouterContext routerContext = new RouterContext(vmIp, remoteIp);

            if (!checkInit(req, routerContext)) {
                throw new InvalidParamException(ExceptionType.CLIENT_ERR, "invalid input for vpn scenario");
            }

            String token = identityWrapperService.getToken();
            if (StringUtils.isEmpty(adminProjectId)) {
                setAdminProjectId(identityWrapperService.getTenantId());
            }
            this.getRouterForwardIp(token, routerContext);
            if (!StringUtils.isEmpty(routerContext.getCascadeVmId())) {
                this.getCascadePortIdByVmIdAndIP(token, vmIp, routerContext);
            }
            this.getCascadePortIdByNetwork(token, vmIp, routerContext);

            this.getMacInfobyCascadePort(token, routerContext);
            this.getAzInfoByCascadeVmId(token, routerContext);
            this.getCascadedPortbyCascadePort(token, routerContext);
            this.getPhyHostIPbyCascadeVmId(token, routerContext);
            this.findDvrRouter(token, routerContext);
            this.setSgMac(token, routerContext);
            this.getL2GWIp(token, routerContext);
            this.getVrouterVtepIp(token, routerContext);
            this.getCNAVtepIp(token, routerContext);
//        this.getVrouterVtepIp(routerContext);
            this.getL2gwVtepIp(token, routerContext);

            submitCNAVPNTask(vpnInfo, routerContext);
            submitL2GWVPNTask(vpnInfo, routerContext);
            submitRfVPNTask(vpnInfo, routerContext);
            vpnInfo.setSubmited();
        } catch (Exception e) {
            LOG.error("failed vpn task", e);
            throw new ApplicationException(ExceptionType.SERVER_ERR, e.getLocalizedMessage());
        }
    }

    private boolean checkInit(VPNRouterTaskRequest req, RouterContext routerContext) {
        if (req == null) {
            return false;
        }
        if (!StringUtils.isEmpty(req.getVmId())) {
            routerContext.setCascadeVmId(req.getVmId());
            return true;
        } else if (!StringUtils.isEmpty(req.getNetId())) {
            routerContext.setCascadeNetworkId(req.getNetId());
            return true;
        }
        return false;
    }

    private void submitCNAVPNTask(VpnInfo vpnInfo, RouterContext routerContext) {
        VPNTaskParam cnaTaskParam = generateVPNTaskParam(routerContext, routerContext.getHostIp(), HostType.CNA,
                Resource.FLOW_VPN_CNA, Resource.NAME_FLOW_VPN_CNA);
        String cnaTaskId = vpnInfo.getTaskId() + TaskTag.VPN_CNA_TAG;
        VPNTaskThread cnaTaskThread = new VPNTaskThread(cnaTaskId, cnaTaskParam);
        Future<BaseFutureCallableResult> cnaTaskFuture = jobThreadPool.addTaskThread(cnaTaskThread);
        taskFutureMap.put(cnaTaskId, cnaTaskFuture);
    }

    private void submitL2GWVPNTask(VpnInfo vpnInfo, RouterContext routerContext) {
        int l2gwSize = routerContext.getL2gwIps().size();
        String taskId = vpnInfo.getTaskId();
        for (int index = 0; index < l2gwSize; index++) {
            VPNTaskParam l2gwTaskParam = generateVPNTaskParam(routerContext, routerContext.getL2gwIps().get(index),
                    HostType.L2GW, Resource.FLOW_VPN_L2GW, Resource.NAME_FLOW_VPN_L2GW);
            String l2gwTaskId = String.format(TaskTag.VPN_L2GW_WILD_TAG, taskId, index);
            VPNTaskThread l2gwTaskThread = new VPNTaskThread(l2gwTaskId, l2gwTaskParam);
            Future<BaseFutureCallableResult> l2gwTaskFuture = jobThreadPool.addTaskThread(l2gwTaskThread);
            taskFutureMap.put(l2gwTaskId, l2gwTaskFuture);
        }
        vpnInfo.setL2gwNum(l2gwSize);
    }

    private void submitRfVPNTask(VpnInfo vpnInfo, RouterContext routerContext) {
        int rtSize = routerContext.getRouterForwarderIps().size();
        String taskId = vpnInfo.getTaskId();
        for (int index = 0; index < rtSize; index++) {
            VPNTaskParam routerTaskParam = generateVPNTaskParam(routerContext,
                    routerContext.getRouterForwarderIps().get(index), HostType.ROUTERFORWARDER,
                    Resource.FLOW_VPN_ROUTERFORWARDER, Resource.NAME_FLOW_VPN_ROUTERFORWARDER);
            String routerTaskId = String.format(TaskTag.VPN_VROUTER_WILD_TAG, taskId, index);
            VPNTaskThread cnaTaskThread = new VPNTaskThread(routerTaskId, routerTaskParam);
            Future<BaseFutureCallableResult> cnaTaskFuture = jobThreadPool.addTaskThread(cnaTaskThread);
            taskFutureMap.put(routerTaskId, cnaTaskFuture);
        }
        vpnInfo.setRfNum(rtSize);
    }

    private VPNTaskParam generateVPNTaskParam(RouterContext routerContext, String hostIp, String hostType,
                                              String localFile, String dstFileName) {
        VPNTaskParam vpnTaskParam = new VPNTaskParam(new CommonCallableParam(routerContext.getVmIp(), hostIp, hostType,
                routerContext.getAvailabiltyZone(), routerContext.getPod()));
        vpnTaskParam.setDvrDevId(routerContext.getDvrDevId());
        vpnTaskParam.setDvrMac(routerContext.getDvrMac());
        vpnTaskParam.setDvrPortId(routerContext.getDvrSrcPort());
        vpnTaskParam.setQvmMac(routerContext.getVmMac());
        vpnTaskParam.setQvmPort(routerContext.getQvmPort());
        vpnTaskParam.setRemoteIp(routerContext.getRemoteIp());
        vpnTaskParam.setLocalFile(localFile);
        vpnTaskParam.setDstFileName(dstFileName);
        vpnTaskParam.setSgMac(routerContext.getSgMac());
        vpnTaskParam.setCnaVtepIp(routerContext.getVtepIp());
        vpnTaskParam.setVrouteVtepIP(routerContext.getVrouterVtepIp());
        vpnTaskParam.setL2gwVtepIp(routerContext.getL2gwVtepIp());
        vpnTaskParam.setProvdSegmtId(routerContext.getProvdSegmtId());
        return vpnTaskParam;
    }

    private RouterInfoResponse getRouterNodeInfo(VpnInfo vpnInfo) {
        RouterInfoResponse result = new RouterInfoResponse();
        String taskId = vpnInfo.getTaskId();
        Map<String, List<NodeInfo>> l3RouterInfo = null;
        Map<String, List<NodeInfo>> l2gwRouteInfo = null;
        Map<String, List<NodeInfo>> rfRouterInfo = null;

        try {
            l3RouterInfo = getCnaRouteInfo(vpnInfo);
            if (null == l3RouterInfo) {
                LOG.info(String.format("vpn cna task is processing : %s", taskId));
                result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                return result;
            }
            l2gwRouteInfo = getL2GWRouteInfo(vpnInfo);
            if (null == l2gwRouteInfo) {
                LOG.info(String.format("vpn l2gw task is processing : %s", taskId));
                result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                return result;
            }
            rfRouterInfo = getRfRouteInfo(vpnInfo);
            if (null == rfRouterInfo) {
                LOG.info(String.format("vpn rf task is processing : %s", taskId));
                result.setStatus(ResultTag.RESULT_STATUS_PROCESSING);
                return result;
            }
        } catch (ApplicationException e) {
            String errorInfo = e.toString();
            LOG.error(errorInfo, e);
            result.setErrorInfo("VPN failed : " + errorInfo);
            result.setStatus(ResultTag.RESULT_STATUS_ERROR);
            return result;
        }

        List<NodeInfo> outputResult = new ArrayList<NodeInfo>();
        List<NodeInfo> inputResult = new ArrayList<NodeInfo>();

        for (NodeInfo nodeInfo : l3RouterInfo.get(ResultTag.FLOW_TAG_OUTPUT)) {
            outputResult.add(nodeInfo);
        }

        for (NodeInfo nodeInfo : l3RouterInfo.get(ResultTag.FLOW_TAG_INPUT)) {
            inputResult.add(nodeInfo);
        }

        result.setCnaInputRouter(inputResult);
        result.setCnaOutputRouter(outputResult);
        result.setL2gwInput(l2gwRouteInfo.get(ResultTag.FLOW_TAG_INPUT));
        result.setRfInput(rfRouterInfo.get(ResultTag.FLOW_TAG_INPUT));
        result.setRfOutput(rfRouterInfo.get(ResultTag.FLOW_TAG_OUTPUT));

        result.setStatus(ResultTag.RESULT_STATUS_END);
        return result;
    }

    private Map<String, List<NodeInfo>> getCnaRouteInfo(VpnInfo vpnInfo) throws ApplicationException {
        String taskId = vpnInfo.getTaskId();
        String vpnCnaTaskId = taskId + TaskTag.VPN_CNA_TAG;
        String vpnCnaLogFileName = String.format(LogFileFormat.VPN_CNA_LOG, vpnCnaTaskId);
        Map<String, List<NodeInfo>> vpnCnaRouterInfo;
        try {
            vpnCnaRouterInfo = ThreadUtil.getOneHostLogInfo(vpnCnaTaskId, vpnCnaLogFileName,
                    taskFutureMap.get(vpnCnaTaskId), HostType.CNA);
        } catch (ScriptException e) {
            throw new ApplicationException(e.getType(), e.getLocalizedMessage());
        }
        return vpnCnaRouterInfo;
    }

    private Map<String, List<NodeInfo>> getL2GWRouteInfo(VpnInfo vpnInfo) throws ApplicationException {
        String taskId = vpnInfo.getTaskId();
        List<String> l2gwTaskIds = getL2GWTaskIds(vpnInfo);
        // input方向需要反向，是否反向主要由于脚本打印顺序确定，由于脚本前后语句有逻辑，故此处处理
        Map<String, List<NodeInfo>> l2gwHostInfo = getHostLogInfo(taskId, l2gwTaskIds, LogFileFormat.VPN_L2GW_LOG, HostType.L2GW);
        if (l2gwHostInfo != null) {
            List<NodeInfo> inputList = l2gwHostInfo.get(ResultTag.FLOW_TAG_INPUT);
            if (inputList != null) {
                Collections.reverse(inputList);
            }
        }
        return l2gwHostInfo;
    }

    private Map<String, List<NodeInfo>> getRfRouteInfo(VpnInfo vpnInfo) throws ApplicationException {
        String taskId = vpnInfo.getTaskId();
        List<String> rfTaskIds = getRfTaskIds(vpnInfo);
        return getHostLogInfo(taskId, rfTaskIds, LogFileFormat.VPN_RF_LOG, HostType.ROUTERFORWARDER);
    }

    private List<String> getRfTaskIds(VpnInfo vpnInfo) {
        String taskId = vpnInfo.getTaskId();
        List<String> rfTaskIds = new ArrayList<>();
        int rtTaskSize = vpnInfo.getRfNum();
        for (int index = 0; index < rtTaskSize; index++) {
            String tmpRfTaskId = String.format(TaskTag.VPN_VROUTER_WILD_TAG, taskId, index);
            if (taskFutureMap.get(tmpRfTaskId) != null) {
                rfTaskIds.add(tmpRfTaskId);
            }
        }
        return rfTaskIds;
    }

    private List<String> getL2GWTaskIds(VpnInfo vpnInfo) {
        String taskId = vpnInfo.getTaskId();
        List<String> l2gwTaskIds = new ArrayList<>();
        int l2gwTaskSize = vpnInfo.getL2gwNum();
        for (int index = 0; index < l2gwTaskSize; index++) {
            String tmpL2gwTaskId = String.format(TaskTag.VPN_L2GW_WILD_TAG, taskId, index);
            if (taskFutureMap.get(tmpL2gwTaskId) != null) {
                l2gwTaskIds.add(tmpL2gwTaskId);
            }
        }
        return l2gwTaskIds;
    }

    private void clearResource(VpnInfo vpnInfo) {
        String taskId = vpnInfo.getTaskId();
        try {
            clearCnaTmpResource(vpnInfo);
        } catch (Exception e) {
            LOG.error("vpn task:" + taskId + "\nfail to clear cna temp resources : ", e);
        }

        try {
            clearL2GWTmpResource(vpnInfo);
        } catch (Exception e) {
            LOG.error("vpn task:" + taskId + "\nfail to clear L2GW temp resources : ", e);
        }

        try {
            clearRfTmpResource(vpnInfo);
        } catch (Exception e) {
            LOG.error("vpn task:" + taskId + "\nfail to clear VRouter temp resources : ", e);
        }

    }

    private void clearCnaTmpResource(VpnInfo vpnInfo) {
        String cnaVpnTaskId = vpnInfo.getTaskId() + TaskTag.VPN_CNA_TAG;
        if (taskFutureMap.get(cnaVpnTaskId) != null) {
            String cnaVpnLogFileName = String.format(LogFileFormat.VPN_CNA_LOG, cnaVpnTaskId);
            try {
                ThreadUtil.cleanTempFile(cnaVpnTaskId, cnaVpnLogFileName, taskFutureMap.get(cnaVpnTaskId).get(),
                        HostType.CNA);
            } catch (Exception e) {
                LOG.error("vpn task:" + cnaVpnTaskId + "\nfail to clear temp resources : ", e);
            }
            taskFutureMap.remove(cnaVpnTaskId);
        }
    }

    private void clearL2GWTmpResource(VpnInfo vpnInfo) {
        List<String> l2gwTaskIds = getL2GWTaskIds(vpnInfo);
        for (String l2gwTaskId : l2gwTaskIds) {
            if (taskFutureMap.get(l2gwTaskId) != null) {
                String l2gwLogFile = String.format(LogFileFormat.VPN_L2GW_LOG, l2gwTaskId);
                try {
                    ThreadUtil.cleanTempFile(l2gwTaskId, l2gwLogFile, taskFutureMap.get(l2gwTaskId).get(),
                            HostType.L2GW);
                } catch (Exception e) {
                    LOG.error("vpn task:" + l2gwTaskId + "\nfail to clear temp resources : ", e);
                }
                taskFutureMap.remove(l2gwTaskId);
            }
        }
    }

    private void clearRfTmpResource(VpnInfo vpnInfo) {
        List<String> rfTaskIds = getRfTaskIds(vpnInfo);
        for (String rfTaskId : rfTaskIds) {
            if (taskFutureMap.get(rfTaskId) != null) {
                String rfLogFile = String.format(LogFileFormat.VPN_RF_LOG, rfTaskId);
                try {
                    ThreadUtil.cleanTempFile(rfTaskId, rfLogFile, taskFutureMap.get(rfTaskId).get(),
                            HostType.ROUTERFORWARDER);
                } catch (Exception e) {
                    LOG.error("vpn task:" + rfTaskId + "\nfail to clear temp resources : ", e);
                }
                taskFutureMap.remove(rfTaskId);
            }
        }
    }

}
