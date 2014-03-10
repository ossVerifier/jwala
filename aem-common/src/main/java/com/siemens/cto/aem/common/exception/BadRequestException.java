package com.siemens.cto.aem.common.exception;

public class BadRequestException extends FaultCodeException {

    public BadRequestException(final MessageResponseStatus theMessageResponseStatus,
                               final String theMessage) {
        super(theMessageResponseStatus,
              theMessage);
    }

    public BadRequestException(final MessageResponseStatus theMessageResponseStatus,
                               final String theMessage,
                               final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
