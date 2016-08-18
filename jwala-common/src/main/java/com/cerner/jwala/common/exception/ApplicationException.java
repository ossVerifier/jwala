package com.cerner.jwala.common.exception;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 1;

    public ApplicationException() {
        super();
    }

    public ApplicationException(final String message) {
        super(message);
    }

    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ApplicationException(final Throwable cause) {
        super(cause);
    }
}
