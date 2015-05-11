package com.siemens.cto.aem.common.exception;

public class ExternalSystemErrorException extends FaultCodeException {

    private static final long serialVersionUID = 1L;

    public ExternalSystemErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage) {
        this(theMessageResponseStatus,
             theMessage,
             null);
    }

    public ExternalSystemErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage,
                                  final Throwable theCause) {
        super(theMessageResponseStatus,
              theMessage,
              theCause);
    }
}
