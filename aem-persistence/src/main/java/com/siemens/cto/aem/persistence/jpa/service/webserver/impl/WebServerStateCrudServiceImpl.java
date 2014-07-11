package com.siemens.cto.aem.persistence.jpa.service.webserver.impl;

import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;
import com.siemens.cto.aem.persistence.jpa.service.state.impl.StateCrudServiceImpl;

public class WebServerStateCrudServiceImpl extends StateCrudServiceImpl<WebServer, WebServerReachableState> implements StateCrudService<WebServer, WebServerReachableState> {

    public WebServerStateCrudServiceImpl() {
        super(StateType.WEBSERVER);
    }
}
