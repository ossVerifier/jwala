package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.exception.MessageResponseStatus;
import com.cerner.jwala.common.rule.ValidNameRule;

public class WebServerNameRule extends ValidNameRule {

    public WebServerNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_WEBSERVER_NAME;
    }

    @Override
    protected String getMessage() {
        return "Invalid WebServer Name : \"" + name + "\"";
    }
}
