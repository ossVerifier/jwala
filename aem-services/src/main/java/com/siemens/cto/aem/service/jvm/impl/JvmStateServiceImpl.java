package com.siemens.cto.aem.service.jvm.impl;

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
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.Stability;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.Transience;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;

public class JvmStateServiceImpl extends StateServiceImpl<Jvm, JvmState> implements StateService<Jvm, JvmState> {

    private ArrayList<JvmState> jvmStatesToCheck = new ArrayList<>(10);

    public JvmStateServiceImpl(final StatePersistenceService<Jvm, JvmState> thePersistenceService,
                                        final StateNotificationService theNotificationService,
                                        final StateNotificationGateway theStateNotificationGateway) {
        super(thePersistenceService,
              theNotificationService,
              StateType.JVM,
              theStateNotificationGateway);

        for(JvmState e : JvmState.values()) {
            if(         e.getStability() == Stability.UNSTABLE 
                    ||  e.getTransience() == Transience.TRANSIENT) {
                jvmStatesToCheck.add(e);
            }                
        } 
        jvmStatesToCheck.add(JvmState.JVM_STARTED);
    }

    @Override
    protected CurrentState<Jvm, JvmState> createUnknown(final Identifier<Jvm> anId) {
        return new CurrentState<>(anId,
                                  JvmState.JVM_UNKNOWN,
                                  DateTime.now(),
                                  StateType.JVM);
    }

    @Override
    protected void sendNotification(final CurrentState<Jvm, JvmState> anUpdatedState) {
        getStateNotificationGateway().jvmStateChanged(anUpdatedState);
    }
    
    
    @Value("${states.stale-check.jvm.stagnation.millis}")
    private int stagnationMillis;
    
    
    /** 
     * Periodically invoked by spring to convert states to STALE
     * Parameterized in toc-defaults:
     * states.stale-check.initial-delay.millis=45000
     * states.stale-check.period.millis=60000
     * states.stale-check.jvm.stagnation.millis=60000
     */
    @Scheduled(initialDelayString="${states.stale-check.initial-delay.millis}", fixedRateString="${states.stale-check.period.millis}")
    @Transactional
    @Override
    public void checkForStaleStates() {
        Calendar cutoff = GregorianCalendar.getInstance();
        cutoff.add(Calendar.MILLISECOND, 0-stagnationMillis);        
        List<CurrentState<Jvm, JvmState>> states = getPersistenceService().markStaleStates(StateType.JVM, JvmState.JVM_STALE, jvmStatesToCheck, cutoff.getTime(), AuditEvent.now(User.getSystemUser()));
        for(CurrentState<Jvm, JvmState> anUpdatedState : states) {
            getStateNotificationGateway().jvmStateChanged(anUpdatedState);
            getNotificationService().notifyStateUpdated(anUpdatedState);
        }
    }
}
