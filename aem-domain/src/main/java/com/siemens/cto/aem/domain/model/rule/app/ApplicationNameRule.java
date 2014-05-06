package com.siemens.cto.aem.domain.model.rule.app;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.ValidNameRule;

public class ApplicationNameRule extends ValidNameRule {

    public ApplicationNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_APPLICATION_NAME;
    }

    @Override
    protected String getMessage() {
        return "Invalid WebApp Name : \"" + name + "\"";
    }
}
