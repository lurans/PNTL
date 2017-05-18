package com.huawei.blackhole.network.common.utils;

import com.huawei.blackhole.network.api.bean.NodeInfo;
import com.huawei.blackhole.network.common.constants.ExceptionType;
import com.huawei.blackhole.network.common.constants.TaskStatus;
import com.huawei.blackhole.network.common.exception.ApplicationException;
import com.huawei.blackhole.network.common.exception.BaseException;
import com.huawei.blackhole.network.common.exception.ScriptException;
import com.huawei.blackhole.network.common.utils.pojo.AuthUser;
import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.CommonCallableParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ThreadUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadUtil.class);

    private static BaseFutureCallableResult getFutureResult(String taskId,
                                                            Future<BaseFutureCallableResult> taskFuture) {
        if (null == taskFuture) {
            String errorInfo = String.format("no such task, taskId=%s", taskId);
            return new BaseFutureCallableResult(TaskStatus.ERROR, ExceptionType.SERVER_ERR, errorInfo);
        }
        if (taskFuture.isDone()) {
            try {
                return taskFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error("task fail", e);
                return new BaseFutureCallableResult(TaskStatus.ERROR, ExceptionType.SERVER_ERR, e.getLocalizedMessage());
            }
        } else {
            LOG.info("{} is still PROCESSING", taskId);
            return new BaseFutureCallableResult(TaskStatus.SUBMITTING);
        }
    }

    private static Map<String, List<NodeInfo>> getTaskExecuteResult(String fileName, BaseFutureCallableResult futureResult, String hostType) throws ScriptException {
        Map<String, List<NodeInfo>> executeResult = null;
        CommonCallableParam commonParam = futureResult.getCommonParam();
        String cmd = String.format("cat ~/blackhole/chkflow/%s", fileName);
        List<String> lines = null;
        try {
            AuthUser authUser = AuthUtil.getKeyFile(hostType);
            lines = JschUtil.submitCommand(commonParam.getHostIp(), authUser, cmd);
        } catch (ApplicationException e) {
            throw new ScriptException(e.getType(), e.getMessage());
        }

        if (lines != null && lines.size() > 0 && lines.get(lines.size() - 1).equals("END")) {
            executeResult = new HashMap<String, List<NodeInfo>>();
            List<NodeInfo> outputRouter = new ArrayList<NodeInfo>();
            List<NodeInfo> inputRouter = new ArrayList<NodeInfo>();

            boolean isOutput = true;
            boolean isError = false;
            StringBuffer buf = new StringBuffer();
            for (String line : lines) {
                if (line.trim().equals("OUTPUT:")) {
                    isOutput = true;
                    continue;
                } else if (line.trim().equals("INPUT:")) {
                    isOutput = false;
                    continue;
                } else if (line.trim().startsWith("ERR:")) {
                    isError = true;
                    buf.append(line);
                    continue;
                } else if (line.trim().equals("END")) {
                    if (isError) {
                        String hostIp = futureResult.getCommonParam().getHostIp();
                        String errParam = String.format(" [host type : %s, host %s, log : %s]", hostType, hostIp,
                                fileName);
                        String errMsg = buf.toString() + errParam;
                        throw new ScriptException(ExceptionType.SCRIPT_ERR, errMsg);
                    }
                    break;
                }

                if (isError) {
                    buf.append(line);
                    continue;
                }

                NodeInfo nodeinfo = packetNodeOfOneLine(line, commonParam);
                if (isOutput) {
                    outputRouter.add(nodeinfo);
                } else {
                    inputRouter.add(nodeinfo);
                }
            }
            executeResult.put("OUTPUT", outputRouter);
            executeResult.put("INPUT", inputRouter);
        } else {
            String hostIp = futureResult.getCommonParam().getHostIp();
            if (lines == null || lines.size() == 0) {
                throw new ScriptException(ExceptionType.REMOTE_EXE_ERR, "fail to execute scripts on " + hostIp);
            }
            if (!lines.get(lines.size() - 1).equals("END")) {
                throw new ScriptException(ExceptionType.SCRIPT_ERR, "fail to execute scripts on " + hostIp);
            }
        }

        return executeResult;
    }

    public static void cleanTempFile(String taskId, String fileName, BaseFutureCallableResult futureResult,
                                     String hostType) {
        CommonCallableParam commonParam = futureResult.getCommonParam();
        try {
            AuthUser authUser = AuthUtil.getKeyFile(hostType);
            String cmd = String.format("cd ~/blackhole/chkflow && rm -rf %s", fileName);
            JschUtil.submitCommand(commonParam.getHostIp(), authUser, cmd);
        } catch (BaseException e) {
            LOG.error("fail to clean temp files on host {} : {}", commonParam.getHostIp(), e.getLocalizedMessage(), e);
        }
    }

    private static NodeInfo packetNodeOfOneLine(String line, CommonCallableParam commonParam) {
        String[] nodeStrinfo = line.split(" ");
        NodeInfo nodeinfo = new NodeInfo(nodeStrinfo[2].split(":")[1], nodeStrinfo[2].split(":")[0],
                Integer.valueOf(nodeStrinfo[3]));

        int size = nodeStrinfo.length;
        for (int k = 4; k < size; k++) {
            if (nodeStrinfo[k].split(":")[0].equals("s_ip")) {
                nodeinfo.setSrcIp(nodeStrinfo[k].split(":")[1]);
            } else if (nodeStrinfo[k].split(":")[0].equals("d_ip")) {
                nodeinfo.setDestIp(nodeStrinfo[k].split(":")[1]);
            }
        }
        nodeinfo.setVmIp(commonParam.getVmIp());
        nodeinfo.setHostIp(commonParam.getHostIp());
        nodeinfo.setHostType(commonParam.getHostType());
        nodeinfo.setAz(commonParam.getAz());
        nodeinfo.setPod(commonParam.getPod());
        return nodeinfo;
    }

    /**
     * @param taskId
     * @param logFileName 日志文件名称
     * @param taskFuture
     * @param hostType
     * @return
     * @throws ApplicationException
     * @throws ScriptException
     */
    public static Map<String, List<NodeInfo>> getOneHostLogInfo(String taskId, String logFileName,
                                                                Future<BaseFutureCallableResult> taskFuture, String hostType) throws ApplicationException, ScriptException {

        BaseFutureCallableResult futureResult = getFutureResult(taskId, taskFuture);

        if (futureResult.getStatus().equals(TaskStatus.ERROR)) {
            throw new ApplicationException(futureResult.getErrType(), futureResult.getErrMsg());
        }
        if (futureResult.getStatus().equals(TaskStatus.SUBMITTING)) {
            return null;
        }
        return getTaskExecuteResult(logFileName, futureResult, hostType);
    }


    private static Map<String, NodeInfo> copmpareTwoResult(Map<String, NodeInfo> firstNGFWNode,
                                                           Map<String, NodeInfo> secondNGFWNode) {
        Map<String, NodeInfo> ngfwNodeMap = new HashMap<String, NodeInfo>();
        NodeInfo outputNode = secondNGFWNode.get("OUTPUT");
        outputNode.setPacketNum(outputNode.getPacketNum() - firstNGFWNode.get("OUTPUT").getPacketNum());
        NodeInfo inputNode = secondNGFWNode.get("INPUT");
        inputNode.setPacketNum(inputNode.getPacketNum() - firstNGFWNode.get("INPUT").getPacketNum());
        ngfwNodeMap.put("OUTPUT", outputNode);
        ngfwNodeMap.put("INPUT", inputNode);
        return ngfwNodeMap;
    }

}
