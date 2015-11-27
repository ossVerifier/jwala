package com.siemens.cto.aem.domain.command.state;

import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.rule.MultipleRules;
import com.siemens.cto.aem.rule.webserver.WebServerIdRule;
import com.siemens.cto.aem.rule.webserver.WebServerReachableStateRule;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;

public class WebServerSetStateCommand extends SetStateCommand<WebServer, WebServerReachableState> {

    public WebServerSetStateCommand(final CurrentState<WebServer, WebServerReachableState> theNewState) {
        super(theNewState);
    }

    @Override
    public void validateCommand() throws BadRequestException {
        final CurrentState<WebServer, WebServerReachableState> newState = getNewState();
        new MultipleRules(new WebServerIdRule(newState.getId()),
                          new WebServerReachableStateRule(newState.getState())).validate();
    }
}
