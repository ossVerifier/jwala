package com.cerner.jwala.common.exception;

import java.util.List;
import java.util.Map;

public class InternalErrorException extends FaultCodeException {

    private static final long serialVersionUID = 1L;
    private Map<String, List<String>> errorDetails = null;

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
                                  final Map<String, List<String>> entityDetailsMap) {
        this(theMessageResponseStatus,
                theMessage,
                theCause);
        errorDetails = entityDetailsMap;
    }

    public Map<String, List<String>> getErrorDetails() {
        return errorDetails;
    }
}
