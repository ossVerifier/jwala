package com.siemens.cto.aem.domain.model.rule;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;

public class HostNameRule extends ValidNameRule {

	private AemFaultType error = AemFaultType.INVALID_HOST_NAME;
	
    public HostNameRule(final String theName) {
        super(theName);
    }

    public HostNameRule(final String theName, AemFaultType errorCode) {
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
