package com.huawei.blackhole.network.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("restriction")
public class AESUtil {
    private static final Logger LOG = LoggerFactory.getLogger(AESUtil.class);
    private static final String FC_KEY = "crypt key";

    public static String decrypt(String sSrc) throws Exception {
        try {
            StringBuffer buf = new StringBuffer(FC_KEY);
            while (buf.length() < 32) {
                buf.append(FC_KEY);
            }
            String sKey = buf.toString().substring(0, 32);
            byte[] raw = sKey.getBytes("ASCII");
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);
            IvParameterSpec iv = new IvParameterSpec(encrypted1, 0, 16);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            // 先用base64解密
            try {
                int newLength = encrypted1.length - 16;
                byte[] newByte = new byte[newLength];
                for (int i = 0; i < newLength; i++) {
                    newByte[i] = encrypted1[i + 16];
                }
                byte[] original = cipher.doFinal(newByte);
                String originalString = new String(original, StandardCharsets.UTF_8);
                return originalString;
            } catch (Exception e) {
                LOG.error("ASE Exception", e);
                return null;
            }
        } catch (Exception e) {
            LOG.error("ASE Exception", e);
            return null;
        }
    }

    public static String getFcKey() {
        return FC_KEY;
    }
}
