package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class WebServerStateServiceImpl extends StateServiceImpl<WebServer, WebServerReachableState> implements StateService<WebServer, WebServerReachableState> {

    private ArrayList<WebServerReachableState> wsStatesToCheck = new ArrayList<>(2);

    public WebServerStateServiceImpl(final StatePersistenceService<WebServer, WebServerReachableState> thePersistenceService,
                                     final StateNotificationService theNotificationService,
                                     final StateNotificationGateway theStateNotificationGateway) {
        super(thePersistenceService,
              theNotificationService,
              StateType.WEB_SERVER,
              theStateNotificationGateway);

        wsStatesToCheck.add(WebServerReachableState.WS_STARTING);
        wsStatesToCheck.add(WebServerReachableState.WS_STOPPING);
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
        getStateNotificationGateway().webServerStateChanged(anUpdatedState);
    }

    @Override
    public void checkForStoppedStates() {
        throw new UnsupportedOperationException("WebServer terminated state checking not required for reverse polling.");
    }
}
