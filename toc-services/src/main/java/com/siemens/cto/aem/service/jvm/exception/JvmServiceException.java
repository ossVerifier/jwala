package com.siemens.cto.aem.service.jvm.exception;

/**
 * Exception wrapper for {@link com.siemens.cto.aem.service.jvm.JvmService}.
 *
 * Created by JC043760 on 5/12/2016.
 */
public class JvmServiceException extends RuntimeException {
    public JvmServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
