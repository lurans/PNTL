package org.wcc.crypt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.wcc.framework.AppProperties;
import org.wcc.framework.AppRuntimeException;

/**
 * 基于组件的根密钥保护方案。方案中的密钥组件
 * 
 */
class RootKeyComponent {
    // 最少需要2个组件
    protected static final int ROOT_KEY_COMPS_SIZE_MIN = 2;

    // 配置项：密钥组件所在文件路径，至少2个。可选。当不配置时使用内置的密钥组件
    private static final String PROP_ROOT_KEY_COMPONENTS = "crypt_keygen_rootkey_components";
    // 配置项：密钥组件长度（单位：byte）。可选。默认值为16。《密钥管理安全规范V1.1》 5.1.2节规定密钥组件最小长度为128位。
    private static final String PROP_ROOT_KEY_COMPONENTS_LEN = "crypt_keygen_rootkey_components_length";
    // 密钥组件最小长度为16字节
    private static final int ROOT_KEY_COMPONENTS_LEN_MIN = 16;
    // 密钥组件默认长度为16字节
    private static final int DEFAULT_ROOT_KEY_COMPONENTS_LEN = ROOT_KEY_COMPONENTS_LEN_MIN;
    // RKC格式标记
    private static final String MAGIC = "wcc_rkc";
    // RKC格式版本。暂未使用
    private static final byte RKC_VERSION = (byte) 0x01;
    // RKC核心内容起始下标
    private static final int CONTENT_START_INDEX = MAGIC.length() + 1;
    // 分隔符
    private static final String FORMAT_SEPARATOR = ";";
    // value在最终输出结果中的位置
    private static final int VALUE_INDEX = 0;
    // TimeStamp在最终输出结果中的位置
    private static final int TIMESTAMP_INDEX = 1;

    private static final long INVALID_TIMESTAMP = -1;
    // 密钥组件的值
    private String value = null;

    // 密钥组件的生成时间，也用于区分不同的密钥组件
    private long timeStamp = INVALID_TIMESTAMP;

    // 密钥组件的长度
    private int length = -1;

    /**
     * 生成默认长度的根密钥组件
     */
    public RootKeyComponent() {
        this(getKeyLength());
    }

    /**
     * 生成密钥组件
     * 
     * @param length
     *            密钥组件的字节数。必须大于等于16
     * @throws AppRuntimeException
     */
    public RootKeyComponent(int length) throws AppRuntimeException {
        if (length < ROOT_KEY_COMPONENTS_LEN_MIN) {
            throw new AppRuntimeException("Components of RootKey must not less than 128 bits");
        }

        try {
            SecureRandom rand = SecureRandom.getInstance("SHA1PRNG");
            byte[] data = new byte[length];
            rand.nextBytes(data);
            this.value = EncryptHelper.parseByte2HexStr(data);
            this.length = length;
        } catch (NoSuchAlgorithmException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 从文件中读取数据并构建密钥组件
     * 
     * @param rkcFile
     *            密钥组件所在的文件
     * @throws AppRuntimeException
     *             , FileNotFoundException
     */
    public RootKeyComponent(File rkcFile) throws AppRuntimeException, FileNotFoundException {
        this(new FileInputStream(rkcFile), rkcFile);
    }

    /**
     * 从流中读取数据并构建密钥组件
     * 
     * @param in
     *            数据流
     * @param file
     *            数据流对应的文件
     * @throws AppRuntimeException
     */
    private RootKeyComponent(InputStream in, File file) throws AppRuntimeException {
        synchronized (RootKeyComponent.class) {
            ProcessLocker locker = ProcessLocker.getInstance();
            try {
                locker.lock();

                String[] content = readFrom(in);

                this.value = content[VALUE_INDEX];
                this.length = value.length();
                String stamp = content[TIMESTAMP_INDEX];
                if (stamp == null) {
                    // stamp==null 表明该rkc文件V1.0.0版本的，需要将该文件转化为新版本
                    this.timeStamp = System.currentTimeMillis();
                    if (file != null) {
                        saveTo(file);
                    }
                } else {
                    this.timeStamp = Long.parseLong(stamp);
                }
            } finally {
                locker.unlock();
            }
        }
    }

    /**
     * 使用value构造密钥组件
     * 
     * @param value
     *            密钥组件的值
     */
    private RootKeyComponent(String value) {
        this.value = value;
        this.length = value.length();
    }

    /**
     * 批量生成密钥组件
     * 
     * @param size
     *            个数
     * @return size个密钥组件
     */
    public static RootKeyComponent[] generateBatch(int size) {
        RootKeyComponent[] comps = new RootKeyComponent[size];
        for (int i = 0; i < size; ++i) {
            comps[i] = new RootKeyComponent();
        }

        return comps;
    }

    /**
     * 将密钥组件批量保存到外部文件中
     * 
     * @param comps
     *            密钥组件
     * @param paths
     *            外部文件
     */
    public static synchronized void saveBatch(RootKeyComponent[] comps, String[] paths) {
        if (null == comps || null == paths || comps.length != paths.length) {
            throw new AppRuntimeException("Param Illegal");
        }

        ProcessLocker locker = ProcessLocker.getInstance();
        try {
            locker.lock();

            int length = comps.length;
            for (int i = 0; i < length; ++i) {
                comps[i].saveTo(new File(paths[i]));
            }
        } finally {
            locker.unlock();
        }
    }

    /**
     * 获取当前密钥组件的生成时间
     * 
     * @return
     */
    public static long currentTimeStamp() {
        return getKeyComps()[0].getTimeStamp();
    }

    /**
     * 将该密钥组件与参数中的密钥组件组合起来
     * 
     * @param that
     *            另一个密钥组件
     * @return 组合后的密钥组件
     * @throws AppRuntimeException
     */
    public RootKeyComponent combine(RootKeyComponent that) throws AppRuntimeException {
        if (null == that) {
            throw new AppRuntimeException("Param is Null");
        }

        return new RootKeyComponent(xor(this.value, that.value));
    }

    /**
     * 格式化根密钥组件的内容
     * 
     * @return
     */
    public String format() {
        String out = MAGIC + (char) RKC_VERSION + this.value + FORMAT_SEPARATOR + timeStamp;
        try {
            return new String(Base64.encodeBase64(out.getBytes("UTF-8"), false, true), Charsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    /**
     * 
     * 解析密钥组件
     * 
     * @param rkc
     *            密钥组件的内容
     * @return 密钥组件的值和生成时间
     */
    public static String[] parse(String rkc) {
        String decoded = null;
        try {
            decoded = new String(Base64.decodeBase64(rkc), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }

        if (decoded.substring(0, MAGIC.length()).equals(MAGIC)) {
            return decoded.substring(CONTENT_START_INDEX).split(";");
        } else {
            // 标记校验失败，则认为次rkc为V1版本的，只有rkc的值，没有timeStamp
            return new String[] { rkc, null };
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLength() {
        return this.length;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * 
     * 获得外置密钥组件的路径
     * 
     * @return 所有外置密钥组件的路径。没有外置组件时返回null
     */
    protected static String[] getRKCPaths() {
        String keyCompPaths = AppProperties.get(PROP_ROOT_KEY_COMPONENTS);
        if (null == keyCompPaths) {
            return null;
        }

        String[] rkcPaths = keyCompPaths.split(";");
        if (null == rkcPaths || rkcPaths.length < ROOT_KEY_COMPS_SIZE_MIN) {
            throw new AppRuntimeException("Config Error. " + PROP_ROOT_KEY_COMPONENTS + " in config file is wrong");
        }

        return rkcPaths;
    }

    /**
     * 获取密钥组件
     * 
     * @return 密钥组件，出错返回null
     */
    protected static RootKeyComponent[] getKeyComps() {
        RootKeyComponent[] comps = null;

        String[] paths = getRKCPaths();

        if (null != paths) {
            // 从外部文件加载密钥组件
            if (paths.length < ROOT_KEY_COMPS_SIZE_MIN) {
                throw new AppRuntimeException("Config Error. Key Component Path in config file is wrong");
            }

            // 外部文件中有非法密钥组件，需要生成新组件并保存到其中
            if (!isValid(paths)) {
                comps = RootKeyComponent.generateBatch(paths.length);
                RootKeyComponent.saveBatch(comps, paths);
            }

            comps = getExternKeyComps(paths);

            if (null == comps) {
                comps = RootKeyComponent.generateBatch(paths.length);
                RootKeyComponent.saveBatch(comps, paths);
            }
        } else {
            // 没有配置密钥组件的外部文件路径，则加载默认组件
            comps = getDefaultKeyComps();
        }

        return comps;
    }

    /**
     * 
     * 获取根密钥长度
     * 
     * @return 根密钥长度
     */
    private static int getKeyLength() {
        return AppProperties.getAsInt(PROP_ROOT_KEY_COMPONENTS_LEN, DEFAULT_ROOT_KEY_COMPONENTS_LEN);
    }

    /**
     * 以RKC_VERSION规定的格式将密钥组件保存到文件中。调用者须保证单线程单进程调用该方法
     * 
     * @param file
     *            要写入的文件
     * @throws AppRuntimeException
     */
    private void saveTo(File file) throws AppRuntimeException {
        if (null == file) {
            throw new AppRuntimeException("file is null");
        }

        // 如果RKC文件不存在，则创建文件
        if (!file.exists()) {
            try {
                // 如果目录不存在，先创建目录
                File parent = file.getParentFile();
                if (null == parent) {
                    throw new AppRuntimeException("RKC: pathname does not name a parent");
                }

                if (!parent.exists()) {
                    if (!parent.mkdirs()) {
                        throw new AppRuntimeException("mkdirs Error");
                    }
                }

                if (!file.createNewFile()) {
                    throw new AppRuntimeException("createNewFile Error");
                }
            } catch (IOException e) {
                throw new AppRuntimeException("Exception when createNewFile");
            }
        }

        if (!file.setWritable(true)) {
            throw new AppRuntimeException(file.getName() + "can not be written");
        }

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(file);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);

            if (INVALID_TIMESTAMP == timeStamp) {
                timeStamp = System.currentTimeMillis();
            }
            bw.write(this.format());
        } catch (FileNotFoundException e) {
            throw new AppRuntimeException("Key Component File not found");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException("Unsupported Encoding of Key Component File");
        } catch (IOException e) {
            throw new AppRuntimeException("Write Component Error");
        } finally {
            try {
                if (null != bw) {
                    bw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (null != osw) {
                    osw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (null != fos) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * 从输入流中读取rkc的内容。调用者须保证单线程单进程调用该方法
     * 
     * @param in
     *            输入流
     * @return 经过解析的rkc值
     */
    private static String[] readFrom(InputStream in) {
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            isr = new InputStreamReader(in, "UTF-8");
            br = new BufferedReader(isr);

            return parse(br.readLine());
        } catch (IOException e) {
            throw new AppRuntimeException("IOException in reading rkc");
        } catch (Exception e) {
            throw new AppRuntimeException(e);
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (null != isr) {
                    isr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 异或两个16进制形式的字符串，两个字符串必须等长，且只能包含0-9, A-F, a-f(即16进制形式)
     * 
     * @param left
     *            要异或的字符串1
     * @param right
     *            要异或的字符串2
     * @return 两个字符串的异或结果（16进制形式）
     */
    private String xor(String left, String right) {
        if (null == left || null == right || left.length() != right.length()) {
            throw new AppRuntimeException("Parameter illegal: null or size not equal");
        }

        byte[] bl = null;
        byte[] br = null;
        try {
            bl = left.getBytes("UTF-8");
            br = right.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }

        bl = EncryptHelper.parseHexStr2Byte(left);
        br = EncryptHelper.parseHexStr2Byte(right);

        int size = bl.length;
        byte[] bresult = new byte[size];
        for (int i = 0; i < size; ++i) {
            bresult[i] = (byte) (bl[i] ^ br[i]);
        }

        return EncryptHelper.parseByte2HexStr(bresult);
    }

    /**
     * 从外部文件加载密钥组件。调用者须保证paths所指向的文件有合法的密钥组件
     * 
     * @param paths
     *            保存密钥组件的文件路径
     * @return 密钥组件，出错返回null
     * @throws AppRuntimeException
     */
    private static RootKeyComponent[] getExternKeyComps(String[] paths) throws AppRuntimeException {
        RootKeyComponent[] comps = null;

        int size = paths.length;

        InputStream[] in = new FileInputStream[size];
        comps = new RootKeyComponent[size];
        try {
            for (int i = 0; i < size; ++i) {
                File f = new File(paths[i]);
                in[i] = new FileInputStream(f);
                comps[i] = new RootKeyComponent(in[i], f);
            }
        } catch (FileNotFoundException e) {
            throw new AppRuntimeException("File Not Found");
        } finally {
            for (int i = 0; i < size; ++i) {
                try {
                    if (null != in[i]) {
                        in[i].close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return comps;
    }

    /**
     * 加载默认密钥组件
     * 
     * @return
     * @throws AppRuntimeException
     */
    private static RootKeyComponent[] getDefaultKeyComps() throws AppRuntimeException {
        RootKeyComponent[] comps = null;

        // 内置密钥组件，在jar包中
        String[] defaultKeyCompsPath = { "org/wcc/crypt/rkc1", "org/wcc/crypt/rkc2", "org/wcc/crypt/rkc3" };

        int compsNum = defaultKeyCompsPath.length;

        comps = new RootKeyComponent[compsNum];
        InputStream in[] = new InputStream[compsNum];
        try {
            for (int i = 0; i < compsNum; ++i) {
                // 从jar包中读取密钥组件
                in[i] = ClassLoader.getSystemResourceAsStream(defaultKeyCompsPath[i]);
                if (null == in[i]) {
                    throw new AppRuntimeException("Get Key Components from jar error");
                }
                comps[i] = new RootKeyComponent(in[i], null);
            }
        } finally {
            for (int i = 0; i < compsNum; ++i) {
                try {
                    if (null != in[i]) {
                        in[i].close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return comps;
    }

    /**
     * 判断文件是否是合法的密钥组件
     * 
     * @param paths
     * @return
     * @throws AppRuntimeException
     */
    private static boolean isValid(String[] paths) throws AppRuntimeException {
        if (null == paths) {
            return false;
        }

        for (String path : paths) {
            if (null == path) {
                return false;
            }

            File file = new File(path);
            if (!file.exists() || !file.canRead() || 0 == file.length()) {
                return false;
            }
        }

        return true;
    }
}
