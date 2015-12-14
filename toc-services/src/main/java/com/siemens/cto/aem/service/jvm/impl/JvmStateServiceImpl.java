package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.*;
import com.siemens.cto.aem.service.state.impl.StateServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class JvmStateServiceImpl extends StateServiceImpl<Jvm, JvmState> implements StateService<Jvm, JvmState> {

    private ArrayList<JvmState> jvmStatesToCheck = new ArrayList<>(10);
    private ArrayList<JvmState> jvmStoppingStatesToCheck = new ArrayList<>(1);
    

    @Value("${states.stopped-check.jvm.max-stop-time.millis}")
    private int serviceStoppedMillis;

    private final GroupStateService.API groupStateService;

    public JvmStateServiceImpl(final StatePersistenceService<Jvm, JvmState> thePersistenceService,
                                        final StateNotificationService theNotificationService,
                                        final GroupStateService.API groupStateService,
                                        final StateNotificationWorker stateNotificationWorker) {
        super(thePersistenceService, theNotificationService, StateType.JVM, groupStateService, stateNotificationWorker);

        jvmStatesToCheck.add(JvmState.JVM_INITIALIZING);
        jvmStatesToCheck.add(JvmState.JVM_INITIALIZED);
        jvmStatesToCheck.add(JvmState.JVM_START);
        jvmStatesToCheck.add(JvmState.JVM_STARTING);
        jvmStatesToCheck.add(JvmState.JVM_STARTED);
        jvmStatesToCheck.add(JvmState.JVM_STOP);
        jvmStatesToCheck.add(JvmState.JVM_STOPPING);
        jvmStatesToCheck.add(JvmState.JVM_DESTROYING);
        jvmStatesToCheck.add(JvmState.JVM_UNKNOWN);

        jvmStoppingStatesToCheck.add(JvmState.JVM_STOPPED);

        this.groupStateService = groupStateService;
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
        throw new UnsupportedOperationException("Deprecated!");
    }

    /** 
     * Periodically invoked by spring to mark services that 
     * are stuck in SHUTTING DOWN (due to manual termination) 

     * Parameterized in toc-defaults:
     * states.stopped-check.initial-delay.millis=120000
     * states.stopped-check.period.millis=60000
     * states.stopped-check.jvm.max-stop-time.millis=120000
     */
    @Scheduled(initialDelayString="${states.stopped-check.initial-delay.millis}", fixedRateString="${states.stopped-check.period.millis}")
    @Transactional
    @Override
    public void checkForStoppedStates() {
        Calendar cutoff = GregorianCalendar.getInstance();
        cutoff.add(Calendar.MILLISECOND, 0-serviceStoppedMillis);
        List<CurrentState<Jvm, JvmState>> states = getPersistenceService().markStaleStates(StateType.JVM, JvmState.SVC_STOPPED, jvmStoppingStatesToCheck, cutoff.getTime(), AuditEvent.now(User.getSystemUser()));
        for(CurrentState<Jvm, JvmState> anUpdatedState : states) {
            getStateNotificationWorker().sendStateChangeNotification(groupStateService, anUpdatedState);
            getNotificationService().notifyStateUpdated(anUpdatedState);
        }
    }
}
