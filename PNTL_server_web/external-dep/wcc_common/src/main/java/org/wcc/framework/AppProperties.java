package org.wcc.framework; //NOPMD

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * <pre>
 * 应用配置文件[application.properties]目录资源读取器
 * 此文件可以通过jvm参数[-Dbeetle.application.home.path=d://xxx//yyy]指定其存放目录
 * 若不显性指定路径，框架会按照以下顺序及方式寻找此文件：
 * 1，从appContext里面查找定义，如果存在则按照定义路径加载（提供在程序设置路径的接口）
 * 2，在当前应用的工作目录下（相对路径）config子目录下寻找并加载
 * 3，在当前应用的classpath的config子目录下寻找并加载
 * </pre>
 */
public final class AppProperties {
    private static Properties appPpt = new Properties();

    private static final String RESOURCE_SYSCONFIG_MASK = "resource_CONFIG_MASK";
    static {
        String filenamePath;
        String fp = AppContext.getInstance().getAppHome();
        if (fp != null && fp.trim().length() > 0) {
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
                ex.printStackTrace(); // NOPMD
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace(); // NOPMD
                }
                bis = null;
            }
        } else {
            InputStream is = null;
            try {
                is = getResAsStream(filenamePath);
                appPpt.load(is);
                systemErr("find  [application.properties] in classpath and  use it!");
            } catch (IOException e) {
                systemErr("no [application.properties]"); // NOPMD
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        systemErr("IO exception occurs"); // NOPMD
                    }
                }
                is = null;
            }
        }
    }

    /**
     * 根据key获取值，并转成int类型返回
     * 
     * @param key
     *            --key字符串
     * @return int --返回int类型值
     */
    public static int getAsInt(String key) {
        return Integer.parseInt(get(key).trim());
    }

    /**
     * 根据key获取值，并转成float类型返回
     * 
     * @param key
     *            --key字符串
     * @return float --返回int类型值
     */
    public static float getAsFloat(String key) {
        return Float.parseFloat(get(key).trim());
    }

    /**
     * 根据key获取值，并转成float类型返回
     * 
     * @param key
     *            --key字符串
     * @param defaultValue
     *            --无法根据key获取值时，返回该默认值
     * @return float --返回int类型值
     */
    public static float getAsFloat(String key, float defaultValue) {
        String a = get(key);
        if (a == null || a.length() == 0) {
            return defaultValue;
        }
        return Float.parseFloat(get(key).trim());
    }

    /**
     * 根据key获取值，如果值不存在，则返回输入默认值
     * 
     * @param key
     *            --key字符串
     * @param defaultValue
     *            --无法根据key获取值时，返回该默认值
     * @return int --获得的int类型值
     */
    public static int getAsInt(String key, int defaultValue) {
        String a = get(key);
        if (a == null || a.length() == 0) {
            return defaultValue;
        }
        return Integer.parseInt(a.trim());
    }

    /**
     * 获取整个应用属性文件内容
     * 
     * @return Properties --属性对象
     */
    public static Properties getProperties() {
        return appPpt;
    }

    /**
     * 根据key获取文件对应的值，以字符串类型返回
     * 
     * @param key
     *            --指定key
     * @return String --属性值字符串
     */
    public static String get(String key) {
        return appPpt.getProperty(key);
    }

    /**
     * 通过字符集编码形式获取值 。application.properties文件默认编码为ansi，如果值为中文的话，
     * 可以charsetName=“8859_1”进行decode
     * 
     * @param key
     *            --指定key
     * @param charsetName
     *            --指定字符集
     * @return String --属性值字符串
     * @throws UnsupportedEncodingException
     *             --不支持指定字符集编码
     */
    public static String getByDecode(String key, String charsetName) throws UnsupportedEncodingException {
        String x = get(key);
        if (x == null) {
            return null;
        }
        String xx = new String(x.getBytes(charsetName), StandardCharsets.UTF_8);
        return xx;
    }

    /**
     * 根据key获取值，如果这个值不存在，返回默认值
     * 
     * @param key
     *            --指定key
     * @param defaultValue
     *            --获取不到属性值时返回的默认值
     * @return String --属性值字符串
     */
    public static String get(String key, String defaultValue) {
        String a = get(key);
        if (a == null) {
            return defaultValue;
        } else {
            return a;
        }
    }

    /**
     * 根据key获取值，并转成boolean类型返回
     * 
     * @param key
     *            --指定key
     * @return boolean --用boolean类型表示的属性值
     */
    public static boolean getAsBoolean(String key) {
        return Boolean.parseBoolean(get(key).trim());
    }

    /**
     * 根据key获取值，并转成boolean类型返回；如果获取不到，返回给定默认值
     * 
     * @param key
     *            --指定key
     * @param defaultValue
     *            --获取不到属性值时返回的默认值
     * @return boolean --用boolean类型表示的属性值
     */
    public static boolean getAsBoolean(String key, boolean defaultValue) {
        String a = get(key);
        if (a == null || a.length() == 0) {
            return defaultValue;
        }
        return Boolean.parseBoolean(a.trim());
    }

    public static int getCONFIGMASK() {
        return AppProperties.getAsInt(RESOURCE_SYSCONFIG_MASK, 0);
    }

    public static String getAppHome() {
        return AppContext.getInstance().getAppHome();
    }

    /**
     * 打印字符串
     * 
     * @param info
     *            --待打印字符串
     */
    private static void systemErr(String info) {
        // 规避静态检查
        PrintStream ps = System.err;
        ps.println(info);
    }

    /**
     * 获取指定资源输入流
     * 
     * @param resource
     *            --指定的资源
     * @return InputStream --指定资源的输入流
     * @throws IOException
     *             --找不到指定资源异常
     */
    private static InputStream getResAsStream(String resource) throws IOException {
        return getResAsStream(Thread.currentThread().getContextClassLoader(), resource);
    }

    /**
     * 获取指定资源输入流
     * 
     * @param loader
     *            --指定的类加载器，用来加载指定资源
     * @param resource
     *            --指定的资源
     * @return InputStream --指定资源的输入流
     * @throws IOException
     *             --找不到指定资源异常
     */
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
