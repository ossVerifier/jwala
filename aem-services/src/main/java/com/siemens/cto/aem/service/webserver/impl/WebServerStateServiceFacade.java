package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.state.impl.AbstractStateServiceFacade;
import com.siemens.cto.aem.service.state.StateService;

public class WebServerStateServiceFacade extends AbstractStateServiceFacade<WebServer, WebServerReachableState> {

    public WebServerStateServiceFacade(final StateService<WebServer, WebServerReachableState> theService) {
        super(theService,
              StateType.WEB_SERVER);
    }

    @Override
    protected SetStateCommand<WebServer, WebServerReachableState> createCommand(final CurrentState<WebServer, WebServerReachableState> aNewCurrentState) {
        return new WebServerSetStateCommand(aNewCurrentState);
    }
}
