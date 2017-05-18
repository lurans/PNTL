package org.wcc.framework;

/**
 *
 * 应用异常类
 */
public class AppException extends Exception {
    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_ERR_CODE = -1000;

    private int errCode = DEFAULT_ERR_CODE;

    /**
     * 用指定的详细消息和原因构造一个新的应用异常。
     * 
     * @param message
     *            --详细消息
     * @param cause
     *            --原因
     */
    public AppException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 用指定的错误码、详细消息和原因构造一个新的应用异常。
     * 
     * @param errCode
     *            --错误码
     * @param message
     *            --详细消息
     * @param cause
     *            --原因
     */
    public AppException(int errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    /**
     * 用指定详细消息构造一个新的应用异常。
     * 
     * @param message
     *            --详细消息
     */
    public AppException(String message) {
        super(message);
    }

    /**
     * 用指定的错误码和详细消息构造一个新的应用异常。
     * 
     * @param errCode
     *            --错误码
     * @param message
     *            --详细消息
     */
    public AppException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    /**
     * 用指定的原因构造一个新的应用异常。
     * 
     * @param cause
     *            --原因
     */
    public AppException(Throwable cause) {
        super(cause);
    }

    /**
     * 用指定的错误码和原因构造一个新的应用异常。
     * 
     * @param errCode
     *            --错误码
     * @param cause
     *            --原因
     */
    public AppException(int errCode, Throwable cause) {
        super(cause);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }

}
