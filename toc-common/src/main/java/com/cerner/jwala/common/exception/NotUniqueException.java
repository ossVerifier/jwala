package com.cerner.jwala.common.exception;

/**
 * Created by z003e5zv on 4/1/2015.
 */
public class NotUniqueException extends FaultCodeException {

    private static final long serialVersionUID = 1L;

    public NotUniqueException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage) {
        this(theMessageResponseStatus,
                theMessage,
                null);
    }

    public NotUniqueException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage,
                             final Throwable theCause) {
        super(theMessageResponseStatus,
                theMessage,
                theCause);
    }
}
