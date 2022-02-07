package com.beyt.generator.exception;

/**
 * Created by tdilber at 11/17/2020
 */
public class TestTemplateFileWriteException extends RuntimeException {

    private final String code;

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @since 3.10
     */
    public TestTemplateFileWriteException() {
        this.code = null;
    }

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @param message description of the exception
     */
    public TestTemplateFileWriteException(final String message) {
        this(message, (String) null);
    }

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @param cause cause of the exception
     */
    public TestTemplateFileWriteException(final Throwable cause) {
        this(cause, null);
    }

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @param message description of the exception
     * @param cause   cause of the exception
     */
    public TestTemplateFileWriteException(final String message, final Throwable cause) {
        this(message, cause, null);
    }

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @param message description of the exception
     * @param code    code indicating a resource for more information regarding the lack of implementation
     */
    public TestTemplateFileWriteException(final String message, final String code) {
        super(message);
        this.code = code;
    }

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @param cause cause of the exception
     * @param code  code indicating a resource for more information regarding the lack of implementation
     */
    public TestTemplateFileWriteException(final Throwable cause, final String code) {
        super(cause);
        this.code = code;
    }

    /**
     * Constructs a TestTemplateFileWriteException.
     *
     * @param message description of the exception
     * @param cause   cause of the exception
     * @param code    code indicating a resource for more information regarding the lack of implementation
     */
    public TestTemplateFileWriteException(final String message, final Throwable cause, final String code) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Obtain the not implemented code. This is an unformatted piece of text intended to point to
     * further information regarding the lack of implementation. It might, for example, be an issue
     * tracker ID or a URL.
     *
     * @return a code indicating a resource for more information regarding the lack of implementation
     */
    public String getCode() {
        return this.code;
    }
}
