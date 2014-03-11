package com.siemens.cto.aem.common.exception;

public class FaultCodeException extends RuntimeException {

    private final MessageResponseStatus messageResponseStatus;

    public FaultCodeException(final MessageResponseStatus theMessageResponseStatus,
                              final String theMessage) {
        super(theMessage);
        messageResponseStatus = theMessageResponseStatus;
    }

    public FaultCodeException(final MessageResponseStatus theMessageResponseStatus,
                              final String theMessage,
                              final Throwable theCause) {
        super(theMessage,
              theCause);
        messageResponseStatus = theMessageResponseStatus;
    }

    public MessageResponseStatus getMessageResponseStatus() {
        return messageResponseStatus;
    }
}
