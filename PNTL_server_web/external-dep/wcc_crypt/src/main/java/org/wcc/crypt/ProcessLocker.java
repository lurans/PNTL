package org.wcc.crypt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wcc.framework.AppRuntimeException;

/**
 * 进程锁，用于同步多个进程的操作
 *
 */
public class ProcessLocker {
    private static final String DEFAULT_LOCKER_NAME_STRING = "_wcc_file_locker_";
    private static Map<String, ProcessLocker> instances = new ConcurrentHashMap<String, ProcessLocker>();
    private FileLock flocker = null;
    private FileChannel fc = null;
    private String lockName = null;

    /**
     * 构造函数
     * 
     * @param lockFileName
     *            锁的名称
     */
    private ProcessLocker(String lockFileName) throws AppRuntimeException {
        String tmpPath = getTempFilePath();
        File lockFile = new File(tmpPath + lockFileName);

        this.lockName = lockFileName;

        try {
            this.fc = new FileOutputStream(lockFile).getChannel();
        } catch (Exception e) {
            throw new AppRuntimeException("Create Locker Error : new FileOutputStream Error");
        }
    }

    /**
     * 获取进程锁
     * 
     * @param name
     *            进程锁的名称
     * @return 进程锁实例
     */
    public static synchronized ProcessLocker getInstance(String name) {
        ProcessLocker instance = instances.get(name);

        if (null == instance) {
            instance = new ProcessLocker(name);
            instances.put(name, instance);
        }

        return instance;
    }

    /**
     * 获取默认进程锁
     * 
     * @return 默认的进程锁实例
     */
    protected static synchronized ProcessLocker getInstance() {
        return ProcessLocker.getInstance(DEFAULT_LOCKER_NAME_STRING);
    }

    /**
     * 加锁
     * 
     * @throws AppRuntimeException
     */
    public synchronized void lock() throws AppRuntimeException {
        if (null == fc) {
            throw new AppRuntimeException("Lock error : fc not init");
        }

        try {
            // 使用FileChannel实现对进程的锁定
            flocker = fc.lock();
        } catch (OverlappingFileLockException e) {
            throw new AppRuntimeException("Another thread has locked. lock = " + this.lockName);
        } catch (IOException e) {
            throw new AppRuntimeException("Lock error : " + e.getMessage());
        }
    }

    /**
     * 解锁
     * 
     * @throws AppRuntimeException
     */
    public synchronized void unlock() throws AppRuntimeException {
        try {
            if (null != flocker) {
                flocker.release();
            }
        } catch (IOException e) {
            throw new AppRuntimeException("Unlock error(flocker) : " + e.getMessage());
        }
    }

    /**
     * 获取系统临时目录
     * 
     * @return 临时目录
     */
    private String getTempFilePath() {
        String path = null;
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("_wcc_test_", null);
            path = tmpFile.getParentFile().getCanonicalPath() + "/";
        } catch (IOException e) {
            throw new AppRuntimeException("getTmpDir Error");
        } finally {
            if (null != tmpFile && tmpFile.exists()) {
                if (!tmpFile.delete()) {
                    throw new AppRuntimeException("failed to delete tmpfile");
                }
            }
        }

        return path;
    }
}
