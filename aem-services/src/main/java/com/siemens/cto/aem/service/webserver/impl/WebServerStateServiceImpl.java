package com.siemens.cto.aem.service.webserver.impl;

import org.joda.time.DateTime;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;

public class WebServerStateServiceImpl extends StateServiceImpl<WebServer, WebServerReachableState> implements StateService<WebServer, WebServerReachableState> {

    public WebServerStateServiceImpl(final StatePersistenceService<WebServer, WebServerReachableState> thePersistenceService,
                                     final StateNotificationService<WebServer> theNotificationService) {
        super(thePersistenceService,
              theNotificationService,
              StateType.WEBSERVER);
    }

    @Override
    protected CurrentState<WebServer, WebServerReachableState> createUnknown(final Identifier<WebServer> anId) {
        return new CurrentState<>(anId,
                                  WebServerReachableState.UNKNOWN,
                                  DateTime.now());
    }
}
