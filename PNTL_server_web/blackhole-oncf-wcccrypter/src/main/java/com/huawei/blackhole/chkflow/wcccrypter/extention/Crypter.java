package com.huawei.blackhole.chkflow.wcccrypter.extention;


import java.security.Key;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.wcc.framework.AppRuntimeException;

public abstract class Crypter
{
  protected static final int PARAM_INDEX_ALGORITHM = 0;
  protected static final int PARAM_INDEX_ENCRYPTED = 1;
  protected static final int PARAM_INDEX_ROOTKEY_TIMESTAMP = 2;
  protected static final int PARAM_INDEX_KEYGEN_ITERATION_COUNT = 3;
  protected static final int ALGO_PARAM_START = 4;
  private static ThreadLocal<Map<Integer, byte[]>> crypterParam = new ThreadLocal()
  {
    public Map<Integer, byte[]> initialValue()
    {
      return new HashMap();
    }
  };
  
  public abstract String encrypt(String paramString1, String paramString2)
    throws AppRuntimeException;
  
  public abstract String decrypt(String paramString1, String paramString2)
    throws AppRuntimeException;
  
  public String encryptByRootKey(String content)
    throws AppRuntimeException
  {
    throw new AppRuntimeException("Not Implemented");
  }
  
  public String decryptByRootKey(String content)
    throws AppRuntimeException
  {
    throw new AppRuntimeException("Not Implemented");
  }
  
  protected String encryptByRootKey(String content, Key rootKey)
    throws AppRuntimeException
  {
    throw new AppRuntimeException("Not Implemented");
  }
  
  protected String decryptByRootKey(String content, Key rootKey)
    throws AppRuntimeException
  {
    throw new AppRuntimeException("Not Implemented");
  }
  
  protected static List<byte[]> getParam()
  {
    return new LinkedList(((Map)crypterParam.get()).values());
  }
  
  protected static byte[] getParam(int index)
  {
    return (byte[])((Map)crypterParam.get()).get(Integer.valueOf(index));
  }
  
  protected static void setParam(int index, byte[] param)
  {
    ((Map)crypterParam.get()).put(Integer.valueOf(index), param);
  }
  
  protected static void setParam(List<byte[]> param)
  {
    if ((null == param) || (param.isEmpty())) {
      throw new AppRuntimeException("null == param or empty");
    }
    Map<Integer, byte[]> paramMap = new HashMap();
    int size = param.size();
    for (int i = 0; i < size; i++) {
      paramMap.put(Integer.valueOf(i), param.get(i));
    }
    crypterParam.set(paramMap);
  }
  
  protected static void clearParam()
  {
        crypterParam.remove();
    }
}
