package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.*;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class WebServerStateServiceImpl extends StateServiceImpl<WebServer, WebServerReachableState> implements StateService<WebServer, WebServerReachableState> {

    private ArrayList<WebServerReachableState> wsStatesToCheck = new ArrayList<>(2);

    public WebServerStateServiceImpl(final StatePersistenceService<WebServer, WebServerReachableState> thePersistenceService,
                                     final StateNotificationService theNotificationService,
                                     final GroupStateService.API groupStateService,
                                     final StateNotificationWorker stateNotificationWorker) {
        super(thePersistenceService, theNotificationService, StateType.WEB_SERVER, groupStateService, stateNotificationWorker);

        wsStatesToCheck.add(WebServerReachableState.WS_START_SENT);
        wsStatesToCheck.add(WebServerReachableState.WS_STOP_SENT);
    }

    @Override
    protected CurrentState<WebServer, WebServerReachableState> createUnknown(final Identifier<WebServer> anId) {
        return new CurrentState<>(anId,
                                  WebServerReachableState.WS_UNKNOWN,
                                  DateTime.now(),
                                  StateType.WEB_SERVER);
    }

    @Override
    protected void sendNotification(final CurrentState<WebServer, WebServerReachableState> anUpdatedState) {
        throw new UnsupportedOperationException("Deprecated!");
    }

    @Override
    public void checkForStoppedStates() {
        throw new UnsupportedOperationException("WebServer terminated state checking not required for reverse polling.");
    }
}
