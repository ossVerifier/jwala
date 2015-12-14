package com.siemens.cto.aem.common.rule.webserver;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.rule.Rule;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;

public class WebServerReachableStateRule implements Rule {

    private final WebServerReachableState state;

    public WebServerReachableStateRule(final WebServerReachableState theState) {
        state = theState;
    }

    @Override
    public boolean isValid() {
        return state != null;
    }

    @Override
    public void validate() throws BadRequestException {
        if (!isValid()) {
            throw new BadRequestException(AemFaultType.WEB_SERVER_REACHABLE_STATE_NOT_SPECIFIED,
                                          "A non-null WebServerReachableState was not specified");
        }
    }
}
