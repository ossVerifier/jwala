package com.siemens.cto.aem.common.request.state;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.rule.MultipleRules;
import com.siemens.cto.aem.common.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.common.rule.webserver.WebServerReachableStateRule;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;

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
