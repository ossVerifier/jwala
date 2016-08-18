package com.cerner.jwala.common.rule.app;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

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
