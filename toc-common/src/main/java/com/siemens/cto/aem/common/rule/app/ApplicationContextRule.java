package com.siemens.cto.aem.common.rule.app;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.rule.ValidNameRule;

public class ApplicationContextRule extends ValidNameRule {

    public ApplicationContextRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_APPLICATION_CTX;
    }

    @Override
    protected String getMessage() {
        return "Invalid WebApp Context: \"" + name + "\"";
    }
}
