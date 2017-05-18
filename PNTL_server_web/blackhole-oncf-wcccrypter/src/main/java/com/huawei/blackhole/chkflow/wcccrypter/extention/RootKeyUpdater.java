package com.huawei.blackhole.chkflow.wcccrypter.extention;

import org.wcc.crypt.EncryptHelper;
import org.wcc.crypt.RootKeyUpdateHandler;
import org.wcc.framework.AppRuntimeException;

import java.io.File;
import java.io.FileNotFoundException;

public class RootKeyUpdater {
    protected static final String UPDATE_LOCKER = "update_lock";
    private static final String UPDATE_ENABLED = "crypt_rootkey_update_enabled";
    private static final String ROOTKEY_LIFETIME_DAYS = "crypt_rootkey_lifetime_days";
    private static final String ROOTKEY_LIFETIME_SECONDS = "crypt_rootkey_lifetime_seconds";
    private static final String UPDATE_HANDLER = "crypt_rootkey_update_handler";
    private static final long MIN_LIFETIME = 1L;
    private static final long BACKUP_FAIL = -1L;
    private static final long MS_OF_A_DAY = 86400000L;
    private static final long MS_OF_A_SECOND = 1000L;
    private static volatile RootKeyUpdater instance = null;
    private static boolean updating = false;
    private static Object updatingLocker = new Object();
    private boolean updateEnable;
    private long lifetime = Long.MAX_VALUE;
    private RootKeyUpdateHandler handler;

    private RootKeyUpdater() {
        this.updateEnable = AppProperties.getAsBoolean("crypt_rootkey_update_enabled", false);
        if (!this.updateEnable) {
            return;
        }
        this.lifetime = getLifeTime();
        this.handler = getHandler(AppProperties.get("crypt_rootkey_update_handler"));
    }

    private RootKeyUpdater(String handlerClass) {
        this.updateEnable = true;
        this.handler = getHandler(handlerClass);
    }

    public static boolean updateImmediately(String handlerClass) {
        RootKeyUpdater updater = new RootKeyUpdater(handlerClass);
        synchronized (RootKeyUpdater.class) {
            ProcessLocker locker = null;
            try {
                locker = ProcessLocker.getInstance("update_lock");

                locker.lock();
                boolean bool = updater.doUpdate();
                if (null != locker) {
                    locker.unlock();
                }
                return bool;
            } finally {
                if (null != locker) {
                    locker.unlock();
                }
            }
        }
    }

    public static boolean updateImmediately() {
        String handlerClass = AppProperties.get("crypt_rootkey_update_handler");
        return updateImmediately(handlerClass);
    }

    protected static RootKeyUpdater getInstance() {
        if (null == instance) {
            synchronized (RootKeyUpdater.class) {
                if (null == instance) {
                    instance = new RootKeyUpdater();
                }
            }
        }
        return instance;
    }

    protected boolean needUpdate() {
        if (!this.updateEnable) {
            return false;
        }
        long current = System.currentTimeMillis();
        long keyGenTime = RootKeyComponent.currentTimeStamp();
        if (current - keyGenTime > this.lifetime) {
            return true;
        }
        return false;
    }

    protected boolean doUpdate() {
        if (!this.updateEnable) {
            return false;
        }
        String[] rkcPaths = RootKeyComponent.getRKCPaths();
        if (null == rkcPaths) {
            return false;
        }
        long backupStamp = backupRKC(rkcPaths);
        if (-1L == backupStamp) {
            return false;
        }
        try {
            synchronized (updatingLocker) {
                updating = true;
            }
            if ((null != this.handler) && (!this.handler.doBeforeUpdate())) {
                return false;
            }
            try {
                int rkcNum = rkcPaths.length;
                RootKeyComponent[] rkcs = RootKeyComponent.generateBatch(rkcNum);
                RootKeyComponent.saveBatch(rkcs, rkcPaths);
                if ((null != this.handler) && (!this.handler.doAfterUpdate())) {
                    throw new AppRuntimeException("doAfterUpdate Error");
                }
            } catch (Exception e) {
                RootKeyComponent[] rkcs;
                e.printStackTrace();

                return restoreRKC(rkcPaths, backupStamp);
            }
        } finally {
            synchronized (updatingLocker) {
                updating = false;
            }
        }
        return true;
    }

    protected RootKeyComponent[] getOldRKCS(long stamp) {
        String[] paths = RootKeyComponent.getRKCPaths();
        if (paths == null) {
            throw new AppRuntimeException("get old RKC failed");
        }
        int size = paths.length;
        RootKeyComponent[] rkcs = new RootKeyComponent[size];
        for (int i = 0; i < size; i++) {
            paths[i] = (paths[i] + "." + stamp);
            try {
                rkcs[i] = new RootKeyComponent(new File(paths[i]));
            } catch (FileNotFoundException e) {
                throw new AppRuntimeException("RKC with tag " + stamp + " Not Found");
            }
        }
        return rkcs;
    }

    private long backupRKC(String[] rkcPaths) {
        if (null == rkcPaths) {
            return -1L;
        }
        long stamp = 0L;
        try {
            stamp = new RootKeyComponent(new File(rkcPaths[0])).getTimeStamp();
        } catch (FileNotFoundException e) {
            throw new AppRuntimeException("rkcPaths[0] Not Found");
        }
        for (String path : rkcPaths) {
            EncryptHelper.copyFile(new File(path), new File(path + "." + stamp));
        }
        return stamp;
    }

    private boolean restoreRKC(String[] rkcPaths, long timeStamp) {
        if (null == rkcPaths) {
            return false;
        }
        try {
            for (String path : rkcPaths) {
                EncryptHelper.copyFile(new File(path + "." + timeStamp), new File(path));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private long getLifeTime() {
        try {
            long time = Long.MAX_VALUE;

            String value = AppProperties.get("crypt_rootkey_lifetime_seconds");
            if (null != value) {
                time = Long.parseLong(value);
                if (time < 1L) {
                    this.updateEnable = false;
                    time = Long.MAX_VALUE;
                } else {
                    time *= 1000L;
                }
            } else {
                value = AppProperties.get("crypt_rootkey_lifetime_days");
                time = Long.parseLong(value);
                if (time < 1L) {
                    this.updateEnable = false;
                    time = Long.MAX_VALUE;
                }
            }
            return time * 86400000L;
        } catch (Exception e) {
            throw new AppRuntimeException("[crypt_rootkey_lifetime_*] config error", e);
        }
    }

    private RootKeyUpdateHandler getHandler(String handler) {
        if ((null == handler) || (handler.isEmpty())) {
            return null;
        }
        try {
            Class<?> handlerClass = Class.forName(handler);
            return (RootKeyUpdateHandler) handlerClass.newInstance();
        } catch (Exception e) {
            throw new AppRuntimeException("Update Handler " + handler + " Not Found, will not update", e);
        }
    }
}
