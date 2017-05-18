package org.wcc.crypt;

/**
 * <pre>
 * 密钥更新回调接口
 * 
 * 根密钥更新时，如果需要同步更新使用根密钥加密的数据，需要在更新根密钥之前，
 * 先使用旧的根密钥解密数据，然后更新根密钥，最后再使用新的根密钥重新加密数据。
 * 
 * 由于wcc保存历史根密钥组件，所以即便不同步更新根密钥加密的数据，该密文也可以继续更新，
 * 但是这样会降低安全性，不推荐这么做。
 * 
 * 上层业务需实现该接口，并通过如下方式注册给WCC：
 *  在application.properties文件中增加如下配置项：
 *      crypt_rootkey_update_handler=full.path.of.HandlerImpl
 *  当不配置该配置项时，不会更新上层数据
 * </pre>
 *
 */
public interface RootKeyUpdateHandler {
    /**
     * 
     * 使用旧的根密钥解密数据。如果之前使用根密钥加密了多个数据，必须在此全部解密
     * 
     * @return 成功返回true，否则返回false
     */
    public boolean doBeforeUpdate();

    /**
     * 
     * 使用新的根密钥重新加密数据。如果之前使用根密钥加密了多个数据，必须在此全部重新加密
     * 
     * @return 成功返回true，否则返回false
     */
    public boolean doAfterUpdate();
}
