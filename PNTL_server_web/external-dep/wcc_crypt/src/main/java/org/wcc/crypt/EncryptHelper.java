package org.wcc.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;

import org.wcc.framework.AppRuntimeException;

/**
 * 包含加解密要使用的公共函数
 * 
 */
public class EncryptHelper {
    /**
     * 十六进制255
     */
    private static final int HEX_255 = 0xFF;

    /**
     * 十进制2
     */
    private static final int DECIMAL_2 = 2;

    /**
     * 十进制16
     */
    private static final int DECIMAL_16 = 16;

    /**
     * 1KB
     */
    private static final int ONE_KB = 1024 * 1024;

    /**
     * 将二进制转换十六进制
     * 
     * @param buf
     *            字节数组
     * @return 字符串
     */
    public static String parseByte2HexStr(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & HEX_255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase(Locale.US));
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     * 
     * @param hexStr
     *            字符串
     * @return 字节数组
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return new byte[0];
        }
        byte[] result = new byte[hexStr.length() / DECIMAL_2];
        for (int i = 0; i < hexStr.length() / DECIMAL_2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * DECIMAL_2, i * DECIMAL_2 + 1), DECIMAL_16);
            int low = Integer.parseInt(hexStr.substring(i * DECIMAL_2 + 1, i * DECIMAL_2 + DECIMAL_2), DECIMAL_16);
            result[i] = (byte) (high * DECIMAL_16 + low);
        }
        return result;
    }

    /**
     * 复制文件
     * 
     * @param src
     *            源文件
     * @param dest
     *            目标文件
     * @throws AppRuntimeException
     */
    public static void copyFile(File src, File dest) {
        // 每次复制1KB
        int bufSize = ONE_KB;

        if (dest.exists() && dest.isDirectory()) {
            throw new AppRuntimeException("dest file is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            input = fis.getChannel();
            output = fos.getChannel();

            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > bufSize ? bufSize : size - pos;
                pos += output.transferFrom(input, pos, count);
            }

            if (src.length() != dest.length()) {
                throw new AppRuntimeException("Failed to copy full contents from src to dest");
            }
        } catch (FileNotFoundException e) {
            throw new AppRuntimeException("FileNotFound");
        } catch (IOException e) {
            throw new AppRuntimeException(e);
        } finally {
            try {
                if (null != output) {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (null != fos) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                if (null != fis) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}