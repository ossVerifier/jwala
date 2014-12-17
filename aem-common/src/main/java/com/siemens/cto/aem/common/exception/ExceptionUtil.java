package com.siemens.cto.aem.common.exception;

public enum ExceptionUtil {

    INSTANCE;

    public static final String NO_EXCEPTION_MESSAGE = "No message, no exception; check logs.";
    
    public Throwable getPenultimateRootCause(final Throwable aThrowable) {
        Throwable cause = aThrowable;
        while ((cause.getCause() != null) && (cause.getCause().getCause() != null)) {
            cause = cause.getCause();
        }
        return cause;
    }

    public static Throwable penultimateRootCause(final Throwable aThrowable) {
        return INSTANCE.getPenultimateRootCause(aThrowable);
    }
}
