package com.siemens.cto.aem.common.domain.model.webserver;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import org.joda.time.DateTime;

public class WebServerState extends CurrentState<WebServer, WebServerReachableState> {

    public WebServerState(final Identifier<WebServer> theId,
                          final WebServerReachableState theState,
                          final DateTime theAsOf) {
        super(theId,
              theState,
              theAsOf,
              StateType.WEB_SERVER);
    }
}
