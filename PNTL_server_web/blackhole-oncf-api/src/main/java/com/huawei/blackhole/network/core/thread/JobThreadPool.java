package com.huawei.blackhole.network.core.thread;


import com.huawei.blackhole.network.core.bean.BaseFutureCallableResult;
import com.huawei.blackhole.network.core.bean.FutureCallableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Service("jobThreadPool")
public class JobThreadPool {

    private static final Logger LOG = LoggerFactory.getLogger(JobThreadPool.class);

    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    private ThreadPoolExecutor threadPoolExecutor;

    @PostConstruct
    public void init() {
        threadPoolExecutor = taskExecutor.getThreadPoolExecutor();


    }

    @PreDestroy
    public void close() {
        threadPoolExecutor.shutdown();
        try {
            if (threadPoolExecutor.awaitTermination(5000, TimeUnit.MICROSECONDS)) {
                threadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public int freeThreadCount() {
        int freeThreadCount = taskExecutor.getMaxPoolSize() - taskExecutor.getActiveCount();
        freeThreadCount = freeThreadCount > 0 ? freeThreadCount : 0;
        return freeThreadCount;
    }

    /**
     * 判断是否有空闲线程
     *
     * @return
     */
    public boolean hasFreeThread() {
        return freeThreadCount() > 0 ? true : false;
    }

    /**
     * 等待空闲线程
     */
    public void waitFreeThread() {
        boolean waitNow = true;
        while (waitNow) {
            try {
                if (hasFreeThread()) {
                    waitNow = false;
                } else {
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                LOG.error("wait free thread failed, error is: ", e);
            }
        }
    }

    /**
     * 添加一个任务
     *
     * @param jobThread
     */
    public void addJobThread(Runnable jobThread) {
        taskExecutor.execute(jobThread);
    }


    public Future<FutureCallableResult> addCallBackThread(Callable<FutureCallableResult> commandThread) {
        return taskExecutor.submit(commandThread);
    }

    public Future<BaseFutureCallableResult> addTaskThread(Callable<BaseFutureCallableResult> taskThread) {
        return taskExecutor.submit(taskThread);
    }

}
