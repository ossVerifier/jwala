package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;

public class HostNameRule extends ValidNameRule {

    private final AemFaultType error;

    public HostNameRule(final String theName) {
        this(theName, AemFaultType.INVALID_HOST_NAME);
    }

    public HostNameRule(final String theName, final AemFaultType errorCode) {
        super(theName);
        this.error = errorCode;
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return error;
    }

    @Override
    protected String getMessage() {
        return "Invalid Host Name : \"" + name + "\"";
    }
}
