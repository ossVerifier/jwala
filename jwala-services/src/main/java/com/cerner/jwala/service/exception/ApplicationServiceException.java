package com.cerner.jwala.service.exception;

/**
 * Wrapper for exceptions that happens inside the {@link com.cerner.jwala.service.app.ApplicationService} implementation(s).
 *
 * Created by z003bpej on 9/16/2015.
 */
public class ApplicationServiceException extends RuntimeException {

    public ApplicationServiceException(String s) {
        super(s);
    }

    public ApplicationServiceException(final String msg, final Throwable t) {
        super(msg, t);
    }

}
