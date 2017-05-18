package org.wcc.crypt;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.wcc.framework.AppRuntimeException;

/**
 * 加密结果格式化，V1版本
 * 
 * V1版本的加密结果的格式： MAGIC(9个char)FORMAT_VERSION(1个char);加密算法标记;密文;参数1;参数2;...; =>
 * Base64编码 => 最终结果
 * 
 *
 */
class FormatterV1 extends Formatter {
    // wcc加密结果标记
    private static final String MAGIC = "wcc_crypt";
    // 加密结果格式的版本。
    private static final byte FORMAT_VERSION = (byte) 0x01;
    // 加密结果中密文的起始下标
    private static final int CONTENT_START_INDEX = MAGIC.length() + 1;
    // 分隔符
    private static final String SEPARATOR = ";";

    /**
     * 格式化加密结果
     * 
     * @param values
     *            密文及其参数
     * @return 格式化后的密文
     */
    @Override
    public String format(List<byte[]> values) {
        if (null == values || values.isEmpty()) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        // 添加标签
        buf.append(buildTag());
        // 添加密文及其参数
        for (byte[] value : values) {
            if (null != value) {
                String s = EncryptHelper.parseByte2HexStr(value);
                buf.append(s);
            }

            buf.append(SEPARATOR);
        }

        // 最终结果使用Base64编码
        try {
            return new String(Base64.encodeBase64(buf.toString().getBytes("UTF-8"), false, true), Charsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 解析格式化后的加密结果
     * 
     * @param formatted
     *            格式化后的加密结果
     * @return 密文及其参数。格式无效时返回null
     */
    @Override
    public List<byte[]> parse(String formatted) {
        if (null == formatted || formatted.length() == 0) {
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

        // 按照参数的顺序依次添加到列表中
        String[] values = content.split(SEPARATOR);
        List<byte[]> result = new LinkedList<byte[]>();
        for (String v : values) {
            if (null == v || v.equals("")) {
                result.add(null);
            } else {
                result.add(EncryptHelper.parseHexStr2Byte(v));
            }
        }

        return result;
    }

    /**
     * 构造标签。标签为：MAGIC+FORMAT_VERSION
     * 
     * @return 标签
     */
    private String buildTag() {
        return MAGIC + (char) FORMAT_VERSION;
    }

    /**
     * 判断密文是否有效
     * 
     * @param decoded
     *            经过Base64解码后的密文
     * @return 有效返回true，否则返回false
     */
    private boolean isValid(String decoded) {
        if (null == decoded) {
            return false;
        }

        // 比对MAGIC
        return decoded.substring(0, MAGIC.length()).equals(MAGIC);
    }
}
