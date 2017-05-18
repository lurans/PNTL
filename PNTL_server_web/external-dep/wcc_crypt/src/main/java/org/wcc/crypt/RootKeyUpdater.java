package org.wcc.crypt;

import org.wcc.framework.AppProperties;
import org.wcc.framework.AppRuntimeException;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 根密钥更新器
 * <p>
 * 要使用根密钥更新功能，上层业务需在application.properties中做如下配置: 1. 开启根密钥更新
 * 在配置文件中配置：crypt_rootkey_update_enabled=true
 * <p>
 * 2. 指定根密钥的存活时间(单位：天或秒) crypt_rootkey_lifetime_days=30 或者
 * crypt_rootkey_lifetime_seconds=30 （优先使用seconds）
 * 每次使用根密钥加解密时，都会检查根密钥的存活时间是否已经过期。如果过期则更新根密钥
 * <p>
 * 3. 注册根密钥更新回调函数 crypt_rootkey_update_handler=具体的更新回调实现类。参考 @RootKeyUpdateHandler
 * 的说明 当不需要更新根密钥加密的数据时，无需配置该参数
 */
public class RootKeyUpdater {
    // 密钥组件升级进程锁的名称
    protected static final String UPDATE_LOCKER = "update_lock";

    // 是否开启根密钥更新，true或false，默认false
    private static final String UPDATE_ENABLED = "crypt_rootkey_update_enabled";
    // 根密钥存活时间，单位：天。必须>=1
    private static final String ROOTKEY_LIFETIME_DAYS = "crypt_rootkey_lifetime_days";
    // 根密钥存活时间，单位：秒。必须>=1。
    // 如果同时配置了，那么只有ROOTKEY_LIFETIME_SECONDS起作用
    private static final String ROOTKEY_LIFETIME_SECONDS = "crypt_rootkey_lifetime_seconds";
    // 根密钥升级回调接口
    private static final String UPDATE_HANDLER = "crypt_rootkey_update_handler";
    // 存活时间最小时间单位为 1
    private static final long MIN_LIFETIME = 1;
    // 备份失败返回值：-1
    private static final long BACKUP_FAIL = -1;
    // 一天的毫秒数
    private static final long MS_OF_A_DAY = 24 * 60 * 60 * 1000;
    // 一秒的毫秒数
    private static final long MS_OF_A_SECOND = 1000;

    private static volatile RootKeyUpdater instance = null;

    private static boolean updating = false;
    private static Object updatingLocker = new Object();

    // 是否开启升级
    private boolean updateEnable;
    // 更新周期，毫秒
    private long lifetime = Long.MAX_VALUE;
    // 回调函数。由上层业务提供
    private RootKeyUpdateHandler handler;

    /**
     * 默认构造函数。参数从配置文件中获取
     */
    private RootKeyUpdater() {
        updateEnable = AppProperties.getAsBoolean(UPDATE_ENABLED, false);
        if (!updateEnable) {
            return;
        }

        lifetime = getLifeTime();
        handler = getHandler(AppProperties.get(UPDATE_HANDLER));
    }

    /**
     * 构造函数，升级参数通过参数传入。该函数只用于updateImmediately
     *
     * @param handlerClass 更新回调类的路径
     */
    private RootKeyUpdater(String handlerClass) {
        updateEnable = true;
        handler = getHandler(handlerClass);
    }

    /**
     * 立即更新根密钥组件
     *
     * @param handlerClass 更新回调类的路径。当无需同步更新根密钥加密的数据时，传入null
     * @return 更新成功返回true，否则返回false
     */
    public static boolean updateImmediately(String handlerClass) {
        RootKeyUpdater updater = new RootKeyUpdater(handlerClass);
        // 因为升级过程要读写密钥组件，所以此处必须保证单线程单进程执行
        synchronized (RootKeyUpdater.class) {
            ProcessLocker locker = null;
            try {
                locker = ProcessLocker.getInstance(RootKeyUpdater.UPDATE_LOCKER);
                // 保证只有一个进程执行
                locker.lock();
                return updater.doUpdate();
            } finally {
                if (null != locker) {
                    locker.unlock();
                }
            }
        }
    }

    /**
     * 立即更新根密钥组件 更新回调函数从application.properties配置文件获得
     *
     * @return 更新成功返回true，否则返回false。
     */
    public static boolean updateImmediately() {
        String handlerClass = AppProperties.get(UPDATE_HANDLER);
        return updateImmediately(handlerClass);
    }

    /**
     * 返回根密钥升级对象实例
     *
     * @return 根密钥升级对象实例
     */
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

    /**
     * 判断是否需要升级
     *
     * @return 如果需要升级返回true，否则返回false
     */
    protected boolean needUpdate() {
        if (!updateEnable) {
            return false;
        }

        if (isUpdating()) {
            return false;
        }

        long current = System.currentTimeMillis();
        long keyGenTime = RootKeyComponent.currentTimeStamp();
        if (current - keyGenTime > lifetime) {
            return true;
        }

        return false;
    }

    /**
     * 升级根密钥
     *
     * @return 升级成功返回true，否则返回false
     */
    protected boolean doUpdate() {
        if (!updateEnable) {
            return false;
        }

        String[] rkcPaths = RootKeyComponent.getRKCPaths();
        if (null == rkcPaths) {
            // null == rkcPaths，表明没有外置密钥组件，则无需升级
            return false;
        }
        // 备份
        long backupStamp = backupRKC(rkcPaths);
        if (BACKUP_FAIL == backupStamp) {
            return false;
        }

        try {
            synchronized (updatingLocker) {
                updating = true;
            }

            // 更新前调用上层业务的回调函数，该函数使用旧的根密钥解密数据
            if (null != handler && !handler.doBeforeUpdate()) {
                return false;
            }

            try {
                // 更新。生成新的密钥组件并保存
                int rkcNum = rkcPaths.length;
                RootKeyComponent[] rkcs = RootKeyComponent.generateBatch(rkcNum);
                RootKeyComponent.saveBatch(rkcs, rkcPaths);

                // 更新后调用上层业务的回调函数，该函数使用新的根密钥加密数据
                if (null != handler && !handler.doAfterUpdate()) {
                    throw new AppRuntimeException("doAfterUpdate Error");
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 升级失败，恢复RKC
                return restoreRKC(rkcPaths, backupStamp);
            }
        } finally {
            synchronized (updatingLocker) {
                updating = false;
            }
        }

        return true;
    }

    /**
     * 获得生成时间为stamp的历史密钥组件
     *
     * @param stamp 时间戳
     * @return 生成时间为stamp的密钥组件
     */
    protected RootKeyComponent[] getOldRKCS(long stamp) {
        String[] paths = RootKeyComponent.getRKCPaths();
        if (paths == null) {
            throw new AppRuntimeException("can not find path for root key");
        }
        int size = paths.length;
        RootKeyComponent[] rkcs = new RootKeyComponent[size];

        for (int i = 0; i < size; ++i) {
            paths[i] = paths[i] + "." + stamp;
            try {
                rkcs[i] = new RootKeyComponent(new File(paths[i]));
            } catch (FileNotFoundException e) {
                throw new AppRuntimeException("RKC with tag " + stamp + " Not Found");
            }
        }

        return rkcs;
    }

    /**
     * 判断是否正在升级
     *
     * @return 正在升级返回true，否则返回false
     */
    private static boolean isUpdating() {
        synchronized (updatingLocker) {
            return updating;
        }
    }

    /**
     * 备份密钥组件。备份后的组件名称为 rkc.[密钥组件生成时间]
     *
     * @param rkcPaths 现有密钥组件的路径
     * @return 备份成功返回备份后的时间戳，否则返回-1
     */
    private long backupRKC(String[] rkcPaths) {
        if (null == rkcPaths) {
            return BACKUP_FAIL;
        }

        long stamp = 0;
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

    /**
     * 升级失败时恢复RKC
     *
     * @param rkcPaths  现有密钥组件的路径
     * @param timeStamp 备份后密钥组件的时间戳
     * @return 成功返回true，否则返回false
     */
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

    /**
     * 获取根密钥存活时间
     *
     * @return 根密钥存活时间，单位：ms
     */
    private long getLifeTime() {
        try {
            long time = Long.MAX_VALUE;
            // 优先使用ROOTKEY_LIFETIME_SECONDS配置项
            String value = AppProperties.get(ROOTKEY_LIFETIME_SECONDS);
            if (null != value) {
                time = Long.parseLong(value);
                // 配置项最小值为1
                if (time < MIN_LIFETIME) {
                    updateEnable = false;
                    time = Long.MAX_VALUE;
                } else {
                    time = time * MS_OF_A_SECOND;
                }
            } else {
                value = AppProperties.get(ROOTKEY_LIFETIME_DAYS);
                time = Long.parseLong(value);
                if (time < MIN_LIFETIME) {
                    updateEnable = false;
                    time = Long.MAX_VALUE;
                } else {
                    time = time * MS_OF_A_DAY;
                }
            }

            return time;
        } catch (Exception e) {
            throw new AppRuntimeException("[crypt_rootkey_lifetime_*] config error", e);
        }
    }

    /**
     * 获取升级回调对象
     *
     * @return 升级回调对象
     */
    private RootKeyUpdateHandler getHandler(String handler) {
        if (null == handler || handler.isEmpty()) {
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