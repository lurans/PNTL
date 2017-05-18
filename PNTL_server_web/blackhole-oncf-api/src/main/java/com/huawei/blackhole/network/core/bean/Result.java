package com.huawei.blackhole.network.core.bean;

import java.io.Serializable;

/**
 * 结果
 **/
public class Result<T> extends BaseResult implements Serializable, Cloneable {
    private static final long serialVersionUID = 8643895848044878063L;

    private T model;


    public T getModel() {
        return model;
    }

    public void setModel(T model) {
        this.model = model;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
