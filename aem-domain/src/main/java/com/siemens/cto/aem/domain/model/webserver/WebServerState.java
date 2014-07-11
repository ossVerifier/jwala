package com.siemens.cto.aem.domain.model.webserver;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;

public class WebServerState extends CurrentState<WebServer, WebServerReachableState> {

    public WebServerState(final Identifier<WebServer> theId,
                          final WebServerReachableState theState,
                          final DateTime theAsOf) {
        super(theId,
              theState,
              theAsOf);
    }
}
