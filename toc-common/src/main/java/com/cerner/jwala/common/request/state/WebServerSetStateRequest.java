package com.cerner.jwala.common.request.state;

import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.BadRequestException;
import com.cerner.jwala.common.rule.MultipleRules;
import com.cerner.jwala.common.rule.webserver.WebServerIdRule;
import com.cerner.jwala.common.rule.webserver.WebServerReachableStateRule;

public class WebServerSetStateRequest extends SetStateRequest<WebServer, WebServerReachableState> {

    public WebServerSetStateRequest(final CurrentState<WebServer, WebServerReachableState> theNewState) {
        super(theNewState);
    }

    @Override
    public void validate() {
        final CurrentState<WebServer, WebServerReachableState> newState = getNewState();
        new MultipleRules(new WebServerIdRule(newState.getId()),
                          new WebServerReachableStateRule(newState.getState())).validate();
    }
}
