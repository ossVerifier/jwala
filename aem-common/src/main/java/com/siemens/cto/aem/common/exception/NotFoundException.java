package com.siemens.cto.aem.common.exception;

public class NotFoundException extends FaultCodeException {

    public NotFoundException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage) {
        super(theMessageResponseStatus,
              theMessage);
    }

    public NotFoundException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage,
                             final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
