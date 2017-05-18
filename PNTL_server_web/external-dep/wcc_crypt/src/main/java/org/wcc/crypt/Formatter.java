/*
 * Copyright Notice:
 *      Copyright  1998-2009, Huawei Technologies Co., Ltd.  ALL Rights Reserved.
 *
 *      Warning: This computer software sourcecode is protected by copyright law
 *      and international treaties. Unauthorized reproduction or distribution
 *      of this sourcecode, or any portion of it, may result in severe civil and
 *      criminal penalties, and will be prosecuted to the maximum extent
 *      possible under the law.
 */
package org.wcc.crypt;

import java.util.List;

import org.wcc.framework.AppRuntimeException;

/**
 * 加密结果格式化
 */
public abstract class Formatter {
    /**
     * 格式化加密结果
     * 
     * @param values
     *            密文及其参数
     * @return 格式化后的密文
     */
    public String format(List<byte[]> values) {
        throw new AppRuntimeException("Not Implemented");
    }

    /**
     * 解析格式化后的加密结果
     * 
     * @param formatted
     *            格式化后的加密结果
     * @return 密文及其参数。格式无效时返回null
     */
    public List<byte[]> parse(String formatted) {
        throw new AppRuntimeException("Not Implemented");
    }
}
