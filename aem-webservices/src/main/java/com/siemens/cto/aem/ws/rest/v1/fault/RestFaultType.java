package com.siemens.cto.aem.ws.rest.v1.fault;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;

public enum RestFaultType implements MessageResponseStatus {

    INVALID_PAGINATION_PARAMETER("AEMR1", "InvalidPaginationParameter"),
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
