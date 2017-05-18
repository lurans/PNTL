package com.huawei.blackhole.chkflow.wcccrypter.extention;

import org.apache.commons.codec.Charsets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;

public final class AppProperties {
    private static Properties appPpt = new Properties();

    static {
        String fp = AppContext.getInstance().getAppHome();
        String filenamePath;
        if ((fp != null) && (fp.trim().length() > 0)) {
            filenamePath = fp + "application.properties";
        } else {
            filenamePath = "config/application.properties";
        }
        File f = new File(filenamePath);
        if (f.exists()) {
            FileInputStream bis = null;
            try {
                bis = new FileInputStream(f);
                appPpt.load(bis);
                systemErr("find  [application.properties] and  use it!");
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bis = null;
            }
        } else {
            InputStream is = null;
            try {
                is = getResAsStream(filenamePath);
                appPpt.load(is);
            } catch (IOException e) {
                System.out.println("IO exception occurs");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        systemErr("IO exception occurs");
                    }
                }
                is = null;
            }
        }
    }

    public static int getAsInt(String key) {
        return Integer.parseInt(get(key).trim());
    }

    public static float getAsFloat(String key) {
        return Float.parseFloat(get(key).trim());
    }

    public static float getAsFloat(String key, float defaultValue) {
        String a = get(key);
        if ((a == null) || (a.length() == 0)) {
            return defaultValue;
        }
        return Float.parseFloat(get(key).trim());
    }

    public static int getAsInt(String key, int defaultValue) {
        String a = get(key);
        if ((a == null) || (a.length() == 0)) {
            return defaultValue;
        }
        return Integer.parseInt(a.trim());
    }

    public static Properties getProperties() {
        return appPpt;
    }

    public static String get(String key) {
        return appPpt.getProperty(key);
    }

    public static String getByDecode(String key, String charsetName) throws UnsupportedEncodingException {
        String x = get(key);
        if (x == null) {
            return null;
        }
        String xx = new String(x.getBytes(charsetName), Charsets.UTF_8);
        return xx;
    }

    public static String get(String key, String defaultValue) {
        String a = get(key);
        if (a == null) {
            return defaultValue;
        }
        return a;
    }

    public static boolean getAsBoolean(String key) {
        return Boolean.parseBoolean(get(key).trim());
    }

    public static boolean getAsBoolean(String key, boolean defaultValue) {
        String a = get(key);
        if ((a == null) || (a.length() == 0)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(a.trim());
    }

    public static int getCONFIGMASK() {
        return getAsInt("resource_CONFIG_MASK", 0);
    }

    public static String getAppHome() {
        return AppContext.getInstance().getAppHome();
    }

    private static void systemErr(String info) {
        PrintStream ps = System.err;
        ps.println(info);
    }

    private static InputStream getResAsStream(String resource) throws IOException {
        return getResAsStream(Thread.currentThread().getContextClassLoader(), resource);
    }

    private static InputStream getResAsStream(ClassLoader loader, String resource) throws IOException {
        InputStream in = null;
        if (loader != null) {
            in = loader.getResourceAsStream(resource);
        }
        if (in == null) {
            in = ClassLoader.getSystemResourceAsStream(resource);
        }
        if (in == null) {
            URL url = new URL(resource);
            in = url.openStream();
        }
        return in;
    }
}
