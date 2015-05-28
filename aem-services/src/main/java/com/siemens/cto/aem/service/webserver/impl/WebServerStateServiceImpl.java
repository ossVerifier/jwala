package com.siemens.cto.aem.service.webserver.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.Transience;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;

public class WebServerStateServiceImpl extends StateServiceImpl<WebServer, WebServerReachableState> implements StateService<WebServer, WebServerReachableState> {

    private ArrayList<WebServerReachableState> wsStatesToCheck = new ArrayList<>(2);
    
    @Value("${states.stale-check.ws.stagnation.millis}")
    private int stagnationMillis;

    public WebServerStateServiceImpl(final StatePersistenceService<WebServer, WebServerReachableState> thePersistenceService,
                                     final StateNotificationService theNotificationService,
                                     final StateNotificationGateway theStateNotificationGateway) {
        super(thePersistenceService,
              theNotificationService,
              StateType.WEB_SERVER,
              theStateNotificationGateway);

        for(WebServerReachableState e : WebServerReachableState.values()) {
            if( e.getTransience() == Transience.TRANSIENT) {
                wsStatesToCheck.add(e);
            }                
        } 
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

    /** 
     * Periodically invoked by spring to convert 
     * STOPPING/STARTING states to UNKNOWN
     * 
     * Parameterized in toc-defaults:
     * states.stale-check.initial-delay.millis=45000
     * states.stale-check.period.millis=60000
     * states.stale-check.ws.stagnation.millis=60000
     */
    @Scheduled(initialDelayString="${states.stale-check.initial-delay.millis}", fixedRateString="${states.stale-check.period.millis}")
    @Transactional
    @Override
    public void checkForStaleStates() {
        Calendar cutoff = GregorianCalendar.getInstance();
        cutoff.add(Calendar.MILLISECOND, 0-stagnationMillis);        
        List<CurrentState<WebServer, WebServerReachableState>> states = getPersistenceService().markStaleStates(StateType.WEB_SERVER, WebServerReachableState.WS_UNKNOWN, wsStatesToCheck, cutoff.getTime(), AuditEvent.now(User.getSystemUser()));
        for(CurrentState<WebServer, WebServerReachableState> anUpdatedState : states) {
            getStateNotificationGateway().webServerStateChanged(anUpdatedState);
            getNotificationService().notifyStateUpdated(anUpdatedState);
        }
    }

    @Override
    public void checkForStoppedStates() {
        throw new UnsupportedOperationException("WebServer terminated state checking not required for reverse polling.");
    }
}
