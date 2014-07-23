package com.siemens.cto.aem.service.webserver.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.domain.model.webserver.WebServerState;
import com.siemens.cto.aem.service.state.StateService;

public class WebServerStateServiceFacade {

    private final StateService<WebServer, WebServerReachableState> service;

    public WebServerStateServiceFacade(final StateService<WebServer, WebServerReachableState> theService) {
        service = theService;
    }

    public void setState(final Identifier<WebServer> aWebServerId,
                         final WebServerReachableState aNewState,
                         final DateTime anAsOf) {

        service.setCurrentState(createStateCommand(aWebServerId,
                                                   aNewState,
                                                   anAsOf),
                                User.getSystemUser());
    }

    WebServerSetStateCommand createStateCommand(final Identifier<WebServer> aWebServerId,
                                                final WebServerReachableState aNewState,
                                                final DateTime anAsOf) {
        final WebServerSetStateCommand command = new WebServerSetStateCommand(createCurrentState(aWebServerId,
                                                                                                 aNewState,
                                                                                                 anAsOf));
        return command;
    }

    WebServerState createCurrentState(final Identifier<WebServer> aWebServerId,
                                      final WebServerReachableState aNewState,
                                      final DateTime anAsOf) {
        final WebServerState state = new WebServerState(aWebServerId,
                                                        aNewState,
                                                        anAsOf);
        return state;
    }
}
