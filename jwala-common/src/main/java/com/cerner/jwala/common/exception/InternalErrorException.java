package com.cerner.jwala.common.exception;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalErrorException extends FaultCodeException {

    private Map<String, List<String>> errorDetails = null;
    private static final Throwable NULL_THROWABLE = null;

    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus, final String theMessage) {
        this(theMessageResponseStatus, theMessage, NULL_THROWABLE);
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

    public InternalErrorException(final MessageResponseStatus theMessageResponseStatus,
                                  final String theMessage,
                                  final Collection<String> entityDetailsCollection) {
        this(theMessageResponseStatus, theMessage, NULL_THROWABLE);
        errorDetails = new HashMap<>();
        for (final String key: entityDetailsCollection) {
            errorDetails.put(key, null);
        }
    }

    public Map<String, List<String>> getErrorDetails() {
        return errorDetails;
    }
}
