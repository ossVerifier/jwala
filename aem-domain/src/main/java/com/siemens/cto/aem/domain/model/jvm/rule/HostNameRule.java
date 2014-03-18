package com.siemens.cto.aem.domain.model.jvm.rule;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.ValidNameRule;

public class HostNameRule extends ValidNameRule {

    public HostNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_HOST_NAME;
    }

    @Override
    protected String getMessage() {
        return "Invalid Host Name : \"" + name + "\"";
    }
}
