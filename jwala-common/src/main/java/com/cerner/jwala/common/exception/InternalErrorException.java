package com.cerner.jwala.common.exception;

import java.util.Map;

public class InternalErrorException extends FaultCodeException {

    private static final long serialVersionUID = 1L;
    private Map<String, String> errorDetails = null;

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

    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage,
                                  final Throwable theCause,
                                  final Map<String, String> entityDetailsMap) {
        this(theMessageResponseStatus,
                theMessage,
                theCause);
        errorDetails = entityDetailsMap;
    }

    public Map<String, String> getErrorDetails() {
        return errorDetails;
    }
}
