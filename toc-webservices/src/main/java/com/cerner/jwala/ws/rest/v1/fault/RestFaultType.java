package com.cerner.jwala.ws.rest.v1.fault;

import com.cerner.jwala.common.exception.MessageResponseStatus;

public enum RestFaultType implements MessageResponseStatus {

    INVALID_TIMEOUT_PARAMETER("AEMR2", "InvalidTimeoutParameter");

    private final String faultCode;
    private final String faultMessage;

    private RestFaultType(final String theFaultCode,
                          final String theFaultMessage) {
        faultCode = theFaultCode;
        faultMessage = theFaultMessage;
    }

    @Override
    public String getMessageCode() {
        return faultCode;
    }

    @Override
    public String getMessage() {
        return faultMessage;
    }
}
