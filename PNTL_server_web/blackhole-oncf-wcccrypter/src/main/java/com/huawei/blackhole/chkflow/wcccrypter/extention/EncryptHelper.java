package com.huawei.blackhole.chkflow.wcccrypter.extention;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Locale;
import org.wcc.framework.AppRuntimeException;

public class EncryptHelper {
    private static final int HEX_255 = 255;
    private static final int DECIMAL_2 = 2;
    private static final int DECIMAL_16 = 16;
    private static final int ONE_KB = 1048576;

    public static String parseByte2HexStr(byte[] buf) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase(Locale.US));
        }
        return sb.toString();
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return new byte[0];
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);

            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);

            result[i] = ((byte) (high * 16 + low));
        }
        return result;
    }

    public static void copyFile(File src, File dest) {
        int bufSize = 1048576;
        if ((dest.exists()) && (dest.isDirectory())) {
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
            long pos = 0L;
            long count = 0L;
            while (pos < size) {
                count = size - pos > bufSize ? bufSize : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
            if (src.length() != dest.length()) {
                throw new AppRuntimeException("Failed to copy full contents from src to dest");
            }
            return;
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
