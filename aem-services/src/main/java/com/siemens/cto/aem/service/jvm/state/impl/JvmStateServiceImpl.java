package com.siemens.cto.aem.service.jvm.state.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.service.jvm.state.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;

public class JvmStateServiceImpl implements JvmStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateServiceImpl.class);

    private final JvmStatePersistenceService persistenceService;
    private final JvmStateNotificationService notificationService;
    private final StateNotificationGateway stateNotificationGateway;

    public JvmStateServiceImpl(final JvmStatePersistenceService theService,
                               final JvmStateNotificationService theNotificationService, 
                               final StateNotificationGateway theStateNotificationGateway) {
        persistenceService = theService;
        notificationService = theNotificationService;
        stateNotificationGateway = theStateNotificationGateway;
    }

    @Override
    @Transactional
    public CurrentJvmState setCurrentJvmState(final SetJvmStateCommand aCommand,
                                              final User aUser) {
        LOGGER.info("Attempting to set state for Jvm {} ", aCommand);
        aCommand.validateCommand();

        // send to direct listeners
        notificationService.notifyJvmStateUpdated(aCommand.getNewJvmState().getJvmId());
        
        // persist
        CurrentJvmState jvmState = persistenceService.updateJvmState(new Event<>(aCommand,
                                                             AuditEvent.now(aUser)));

        // send to bus - TODO limit to transitions only.
        stateNotificationGateway.jvmStateChanged(jvmState);
        
        return jvmState;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentJvmState getCurrentJvmState(final Identifier<Jvm> aJvmId) {
        LOGGER.info("Getting state for Jvm {}", aJvmId);
        CurrentJvmState jvmState = persistenceService.getJvmState(aJvmId);
        if (jvmState == null) {
            jvmState = CurrentJvmState.createUnknownState(aJvmId);
        }
        return jvmState;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CurrentJvmState> getCurrentJvmStates(final Set<Identifier<Jvm>> someJvmIds) {
        LOGGER.info("Getting states for Jvms {}", someJvmIds);
        final Set<CurrentJvmState> results = new HashSet<>();
        for (final Identifier<Jvm> jvmId : someJvmIds) {
            final CurrentJvmState currentJvmState = getCurrentJvmState(jvmId);
            results.add(currentJvmState);
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CurrentJvmState> getCurrentJvmStates(final PaginationParameter somePagination) {
        LOGGER.info("Getting states for all Jvms");
        return persistenceService.getAllKnownJvmStates(somePagination);
    }
}
