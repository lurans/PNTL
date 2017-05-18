package com.huawei.blackhole.chkflow.wcccrypter.extention;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.wcc.framework.AppRuntimeException;

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

class RootKeyComponent {
    protected static final int ROOT_KEY_COMPS_SIZE_MIN = 2;
    private static final String PROP_ROOT_KEY_COMPONENTS = "crypt_keygen_rootkey_components";
    private static final String PROP_ROOT_KEY_COMPONENTS_LEN = "crypt_keygen_rootkey_components_length";
    private static final int ROOT_KEY_COMPONENTS_LEN_MIN = 16;
    private static final int DEFAULT_ROOT_KEY_COMPONENTS_LEN = 16;
    private static final String MAGIC = "wcc_rkc";
    private static final byte RKC_VERSION = 1;
    private static final int CONTENT_START_INDEX = "wcc_rkc".length() + 1;
    private static final String FORMAT_SEPARATOR = ";";
    private static final int VALUE_INDEX = 0;
    private static final int TIMESTAMP_INDEX = 1;
    private static final long INVALID_TIMESTAMP = -1L;
    private String value = null;
    private long timeStamp = -1L;
    private int length = -1;

    public RootKeyComponent() {
        this(getKeyLength());
    }

    public RootKeyComponent(int length) throws AppRuntimeException {
        if (length < 16) {
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

    public RootKeyComponent(File rkcFile) throws AppRuntimeException, FileNotFoundException {
        this(new FileInputStream(rkcFile), rkcFile);
    }

    private RootKeyComponent(InputStream in, File file) throws AppRuntimeException {
        synchronized (RootKeyComponent.class) {
            ProcessLocker locker = ProcessLocker.getInstance();
            try {
                locker.lock();

                String[] content = readFrom(in);

                this.value = content[0];
                this.length = this.value.length();
                String stamp = content[1];
                if (stamp == null) {
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

    private RootKeyComponent(String value) {
        this.value = value;
        this.length = value.length();
    }

    public static RootKeyComponent[] generateBatch(int size) {
        RootKeyComponent[] comps = new RootKeyComponent[size];
        for (int i = 0; i < size; i++) {
            comps[i] = new RootKeyComponent();
        }
        return comps;
    }

    public static synchronized void saveBatch(RootKeyComponent[] comps, String[] paths) {
        if ((null == comps) || (null == paths) || (comps.length != paths.length)) {
            throw new AppRuntimeException("Param Illegal");
        }
        ProcessLocker locker = ProcessLocker.getInstance();
        try {
            locker.lock();

            int length = comps.length;
            for (int i = 0; i < length; i++) {
                comps[i].saveTo(new File(paths[i]));
            }
        } finally {
            locker.unlock();
        }
    }

    public static long currentTimeStamp() {
        return getKeyComps()[0].getTimeStamp();
    }

    public RootKeyComponent combine(RootKeyComponent that) throws AppRuntimeException {
        if (null == that) {
            throw new AppRuntimeException("Param is Null");
        }
        return new RootKeyComponent(xor(this.value, that.value));
    }

    public String format() {
        String out = "wcc_rkc\001" + this.value + ";" + this.timeStamp;
        try {
            return new String(Base64.encodeBase64(out.getBytes("UTF-8"), false, true), Charsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
    }

    public static String[] parse(String rkc) {
        String decoded = null;
        try {
            decoded = new String(Base64.decodeBase64(rkc), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AppRuntimeException(e);
        }
        if (decoded.substring(0, "wcc_rkc".length()).equals("wcc_rkc")) {
            return decoded.substring(CONTENT_START_INDEX).split(";");
        }
        return new String[]{rkc, null};
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getLength() {
        return this.length;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    protected static String[] getRKCPaths() {
        String keyCompPaths = AppProperties.get("crypt_keygen_rootkey_components");
        if (null == keyCompPaths) {
            return null;
        }
        String[] rkcPaths = keyCompPaths.split(";");
        if ((null == rkcPaths) || (rkcPaths.length < 2)) {
            throw new AppRuntimeException("Config Error. crypt_keygen_rootkey_components in config file is wrong");
        }
        return rkcPaths;
    }

    protected static RootKeyComponent[] getKeyComps() {
        RootKeyComponent[] comps = null;

        String[] paths = getRKCPaths();
        if (null != paths) {
            if (paths.length < 2) {
                throw new AppRuntimeException("Config Error. Key Component Path in config file is wrong");
            }
            if (!isValid(paths)) {
                comps = generateBatch(paths.length);
                saveBatch(comps, paths);
            }
            comps = getExternKeyComps(paths);
            if (null == comps) {
                comps = generateBatch(paths.length);
                saveBatch(comps, paths);
            }
        } else {
            comps = getDefaultKeyComps();
        }
        return comps;
    }

    private static int getKeyLength() {
        return AppProperties.getAsInt("crypt_keygen_rootkey_components_length", 16);
    }

    private void saveTo(File file) throws AppRuntimeException {
        if (null == file) {
            throw new AppRuntimeException("file is null");
        }
        if (!file.exists()) {
            try {
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
            if (-1L == this.timeStamp) {
                this.timeStamp = System.currentTimeMillis();
            }
            bw.write(format());
            return;
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

    private String xor(String left, String right) {
        if ((null == left) || (null == right) || (left.length() != right.length())) {
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
        for (int i = 0; i < size; i++) {
            bresult[i] = ((byte) (bl[i] ^ br[i]));
        }
        return EncryptHelper.parseByte2HexStr(bresult);
    }

    private static RootKeyComponent[] getExternKeyComps(String[] paths) throws AppRuntimeException {
        RootKeyComponent[] comps = null;

        int size = paths.length;

        InputStream[] in = new FileInputStream[size];
        comps = new RootKeyComponent[size];
        try {
            for (int i = 0; i < size; i++) {
                File f = new File(paths[i]);
                in[i] = new FileInputStream(f);
                comps[i] = new RootKeyComponent(in[i], f);
            }
        } catch (FileNotFoundException e) {
            int i;
            throw new AppRuntimeException("File Not Found");
        } finally {
            for (int i = 0; i < size; i++) {
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

    private static RootKeyComponent[] getDefaultKeyComps() throws AppRuntimeException {
        RootKeyComponent[] comps = null;

        String[] defaultKeyCompsPath = {"org/wcc/crypt/rkc1", "org/wcc/crypt/rkc2", "org/wcc/crypt/rkc3"};

        int compsNum = defaultKeyCompsPath.length;

        comps = new RootKeyComponent[compsNum];
        InputStream[] in = new InputStream[compsNum];
        try {
            for (int i = 0; i < compsNum; i++) {
                in[i] = ClassLoader.getSystemResourceAsStream(defaultKeyCompsPath[i]);
                if (null == in[i]) {
                    throw new AppRuntimeException("Get Key Components from jar error");
                }
                comps[i] = new RootKeyComponent(in[i], null);
            }
        } finally {
            for (int i = 0; i < compsNum; i++) {
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

    private static boolean isValid(String[] paths) throws AppRuntimeException {
        if (null == paths) {
            return false;
        }
        for (String path : paths) {
            if (null == path) {
                return false;
            }
            File file = new File(path);
            if ((!file.exists()) || (!file.canRead()) || (0L == file.length())) {
                return false;
            }
        }
        return true;
    }
}
