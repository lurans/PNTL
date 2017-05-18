package com.huawei.blackhole.chkflow.wcccrypter.extention;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.wcc.framework.AppRuntimeException;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

class FormatterV1 extends Formatter {
    private static final String MAGIC = "wcc_crypt";
    private static final byte FORMAT_VERSION = 1;
    private static final int CONTENT_START_INDEX = "wcc_crypt".length() + 1;
    private static final String SEPARATOR = ";";

    public String format(List<byte[]> values) {
        if ((null == values) || (values.isEmpty())) {
            return null;
        }
        StringBuilder buf = new StringBuilder();

        buf.append(buildTag());
        for (byte[] value : values) {
            if (null != value) {
                String s = EncryptHelper.parseByte2HexStr(value);
                buf.append(s);
            }
            buf.append(";");
        }
        try {
            return new String(Base64.encodeBase64(buf.toString().getBytes("UTF-8"), false, true), Charsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    public List<byte[]> parse(String formatted) {
        if ((null == formatted) || (formatted.length() == 0)) {
            return null;
        }
        String decoded = null;
        try {
            decoded = new String(Base64.decodeBase64(formatted), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
        if (!isValid(decoded)) {
            return null;
        }
        String content = decoded.substring(CONTENT_START_INDEX);

        String[] values = content.split(";");
        List<byte[]> result = new LinkedList();
        for (String v : values) {
            if ((null == v) || (v.equals(""))) {
                result.add(null);
            } else {
                result.add(EncryptHelper.parseHexStr2Byte(v));
            }
        }
        return result;
    }

    private String buildTag() {
        return "wcc_crypt\001";
    }

    private boolean isValid(String decoded) {
        if (null == decoded) {
            return false;
        }
        return decoded.substring(0, "wcc_crypt".length()).equals("wcc_crypt");
    }
}
