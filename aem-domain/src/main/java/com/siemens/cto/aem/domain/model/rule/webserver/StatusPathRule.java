package com.siemens.cto.aem.domain.model.rule.webserver;

import com.siemens.cto.aem.common.exception.MessageResponseStatus;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.rule.ValidNameRule;

public class StatusPathRule extends ValidNameRule {

    public StatusPathRule(final String theName) {
        super(theName);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && isAbsolutePath();
    }

    @Override
    protected MessageResponseStatus getMessageResponseStatus() {
        return AemFaultType.INVALID_WEBSERVER_STATUS_PATH;
    }

    @Override
    protected String getMessage() {
        return "Invalid status path url : \"" + name + "\"";
    }

    private boolean isAbsolutePath() {
        return name.startsWith("/");
    }
}
