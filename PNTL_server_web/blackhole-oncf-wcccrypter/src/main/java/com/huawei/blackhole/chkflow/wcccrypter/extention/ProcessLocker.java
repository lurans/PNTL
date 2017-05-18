package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.wcc.framework.AppRuntimeException;

public class ProcessLocker {
    private static final String DEFAULT_LOCKER_NAME_STRING = "_wcc_file_locker_";
    private static Map<String, ProcessLocker> instances = new ConcurrentHashMap();
    private FileLock flocker = null;
    private FileChannel fc = null;
    private String lockName = null;

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

    public static synchronized ProcessLocker getInstance(String name) {
        ProcessLocker instance = (ProcessLocker) instances.get(name);
        if (null == instance) {
            instance = new ProcessLocker(name);
            instances.put(name, instance);
        }
        return instance;
    }

    protected static synchronized ProcessLocker getInstance() {
        return getInstance("_wcc_file_locker_");
    }

    public synchronized void lock() throws AppRuntimeException {
        if (null == this.fc) {
            throw new AppRuntimeException("Lock error : fc not init");
        }
        try {
            this.flocker = this.fc.lock();
        } catch (OverlappingFileLockException e) {
            throw new AppRuntimeException("Another thread has locked. lock = " + this.lockName);
        } catch (IOException e) {
            throw new AppRuntimeException("Lock error : " + e.getMessage());
        }
    }

    public synchronized void unlock() throws AppRuntimeException {
        try {
            if (null != this.flocker) {
                this.flocker.release();
            }
        } catch (IOException e) {
            throw new AppRuntimeException("Unlock error(flocker) : " + e.getMessage());
        }
    }

    private String getTempFilePath() {
        String path = null;
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile("_wcc_test_", null);
            path = tmpFile.getParentFile().getCanonicalPath() + "/";
        } catch (IOException e) {
            throw new AppRuntimeException("getTmpDir Error");
        } finally {
            if ((null != tmpFile) && (tmpFile.exists())) {
                if (!tmpFile.delete()) {
                    throw new AppRuntimeException("failed to delete tmpfile");
                }
            }
        }
        return path;
    }
}
