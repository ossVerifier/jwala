package com.siemens.cto.aem.domain.model.rule.webserver;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.ValidNameRule;

public class WebServerHostNameRule extends ValidNameRule {

    public WebServerHostNameRule(final String theName) {
        super(theName);
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_WEBSERVER_HOST;
    }

    @Override
    protected String getMessage() {
        return "Invalid Host Name : \"" + name + "\"";
    }
}
