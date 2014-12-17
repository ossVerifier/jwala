package com.siemens.cto.aem.common.exception;

public class NotFoundException extends FaultCodeException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage) {
        this(theMessageResponseStatus,
             theMessage,
             null);
    }

    public NotFoundException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage,
                             final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
