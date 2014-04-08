package com.siemens.cto.aem.domain.model.rule.jvm;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.ValidNameRule;

public class JvmHostNameRule extends ValidNameRule {

    public JvmHostNameRule(final String theName) {
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
