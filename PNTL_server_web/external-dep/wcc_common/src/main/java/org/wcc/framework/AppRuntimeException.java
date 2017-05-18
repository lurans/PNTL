package org.wcc.framework;

/**
 *
 * 应用运行时异常类，继承自RuntimeException
 *
 */
public class AppRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private static final int INITIAL_ERR_CODE = -1000;

    // CHECKSTYLE:OFF
    protected int errCode = INITIAL_ERR_CODE;

    // CHECKSTYLE:ON
    /**
     * 用指定的详细消息和原因构造一个新的运行时异常。
     * 
     * @param message
     *            --详细消息
     * @param cause
     *            --原因
     */
    public AppRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 用指定的错误码、详细消息和原因构造一个新的运行时异常。
     * 
     * @param errCode
     *            --错误码
     * @param message
     *            --详细消息
     * @param cause
     *            --原因
     */
    public AppRuntimeException(int errCode, String message, Throwable cause) {
        super(message, cause);
        this.errCode = errCode;
    }

    /**
     * 用指定的详细消息构造一个新的运行时异常。
     * 
     * @param message
     *            --详细消息
     */
    public AppRuntimeException(String message) {
        super(message);
    }

    /**
     * 用指定的错误码、详细消息构造一个新的运行时异常。
     * 
     * @param errCode
     *            --错误码
     * @param message
     *            --详细消息
     */
    public AppRuntimeException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    /**
     * 用指定的原因构造一个新的运行时异常。
     * 
     * @param cause
     *            --原因
     */
    public AppRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * 用指定的错误码和原因构造一个新的运行时异常。
     * 
     * @param errCode
     *            --错误码
     * @param cause
     *            --原因
     */
    public AppRuntimeException(int errCode, Throwable cause) {
        super(cause);
        this.errCode = errCode;
    }

    public int getErrCode() {
        return errCode;
    }
}
