package com.cerner.jwala.common.exception;

public class InternalErrorException extends FaultCodeException {

    private static final long serialVersionUID = 1L;

    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage) {
        this(theMessageResponseStatus,
             theMessage,
             null);
    }

    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage,
                                  final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
