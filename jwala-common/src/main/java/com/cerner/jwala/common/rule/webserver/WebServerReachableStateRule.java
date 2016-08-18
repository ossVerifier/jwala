package com.cerner.jwala.common.rule.webserver;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.Rule;

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
