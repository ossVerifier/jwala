package com.siemens.cto.aem.domain.model.fault;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;

public enum AemFaultType implements MessageResponseStatus {

    INVALID_GROUP_NAME("AEM1", "InvalidGroupName"),
    GROUP_NOT_FOUND("AEM2", "GroupNotFound");

    private final String faultCode;
    private final String faultMessage;

    private AemFaultType(final String theFaultCode,
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
