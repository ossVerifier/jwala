package com.cerner.jwala.common.rule.jvm;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

public class JvmNameRule extends ValidNameRule {

    public JvmNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_JVM_NAME;
    }

    @Override
    protected String getMessage() {
        return "Invalid Jvm Name : \"" + name + "\"";
    }
}
