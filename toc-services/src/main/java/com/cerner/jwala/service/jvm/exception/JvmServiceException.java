package com.cerner.jwala.service.jvm.exception;

/**
 * Exception wrapper for {@link com.cerner.jwala.service.jvm.JvmService}.
 *
 * Created by JC043760 on 5/12/2016.
 */
public class JvmServiceException extends RuntimeException {

    public JvmServiceException(final String s) {
        super(s);
    }

    public JvmServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }
}
