package com.siemens.cto.aem.request.state;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.rule.webserver.WebServerReachableStateRule;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

public class WebServerSetStateRequest extends SetStateRequest<WebServer, WebServerReachableState> {

    public WebServerSetStateRequest(final CurrentState<WebServer, WebServerReachableState> theNewState) {
        super(theNewState);
    }

    @Override
    public void validate() throws BadRequestException {
        final CurrentState<WebServer, WebServerReachableState> newState = getNewState();
        new MultipleRules(new WebServerIdRule(newState.getId()),
                          new WebServerReachableStateRule(newState.getState())).validate();
    }
}
