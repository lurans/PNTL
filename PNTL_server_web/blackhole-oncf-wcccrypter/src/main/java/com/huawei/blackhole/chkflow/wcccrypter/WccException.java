package com.huawei.blackhole.chkflow.wcccrypter;

public class WccException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 5093964307339616396L;

    public WccException(final String message) {
        super(message);
    }

    public WccException(final Throwable e) {
        super(e);
    }

    public WccException(final String message, final Throwable e) {
        super(message + "\nCaused by: " + e.getMessage());
    }
}
