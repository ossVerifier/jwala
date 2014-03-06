package com.siemens.cto.aem.common.exception;

public class NotFoundException extends FaultCodeException {

    public NotFoundException(final MessageResponseStatus theMessageResponseStatus,
                             final String theMessage) {
        super(theMessageResponseStatus,
              theMessage);
    }
}
