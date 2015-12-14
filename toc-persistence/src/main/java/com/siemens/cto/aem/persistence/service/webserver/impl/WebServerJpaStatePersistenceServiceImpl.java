package com.siemens.cto.aem.persistence.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.state.impl.JpaStatePersistenceServiceImpl;

public class WebServerJpaStatePersistenceServiceImpl extends JpaStatePersistenceServiceImpl<WebServer, WebServerReachableState> implements StatePersistenceService<WebServer, WebServerReachableState> {

    public WebServerJpaStatePersistenceServiceImpl(final StateCrudService<WebServer, WebServerReachableState> theService) {
        super(theService);
    }

    @Override
    protected CurrentState<WebServer, WebServerReachableState> build(JpaCurrentState aCurrentState, WebServerReachableState staleState) {
        return new WebServerJpaCurrentStateBuilder(aCurrentState).setStaleOption(staleState).build();
    }
}
