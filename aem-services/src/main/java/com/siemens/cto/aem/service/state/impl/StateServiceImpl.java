package com.siemens.cto.aem.service.state.impl;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;

public abstract class StateServiceImpl<S, T extends ExternalizableState> implements StateService<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateServiceImpl.class);

    private final StatePersistenceService<S, T> persistenceService;
    private final StateNotificationService notificationService;
    private final StateType stateType;

    protected final StateNotificationGateway stateNotificationGateway;

    public StateServiceImpl(final StatePersistenceService<S, T> thePersistenceService,
                            final StateNotificationService theNotificationService,
                            final StateType theStateType,
                            final StateNotificationGateway theStateNotificationGateway) {
        persistenceService = thePersistenceService;
        notificationService = theNotificationService;
        stateType = theStateType;
        stateNotificationGateway = theStateNotificationGateway;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CurrentState<S, T> setCurrentState(final SetStateCommand<S, T> aCommand,
                                              final User aUser) {
        LOGGER.trace("Attempting to set state for {} {} ", stateType, aCommand);
        aCommand.validateCommand();

        final CurrentState<S, T> currentState = persistenceService.updateState(new Event<>(aCommand,
                                                                                           AuditEvent.now(aUser)));
        notificationService.notifyStateUpdated(currentState);
        sendNotification(currentState);
        return currentState;
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentState<S, T> getCurrentState(final Identifier<S> anId) {
        LOGGER.trace("Getting state for {} {}", stateType, anId);
        CurrentState<S, T> state = persistenceService.getState(anId);
        if (state == null) {
            state = createUnknown(anId);
        }
        return state;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CurrentState<S, T>> getCurrentStates(final Set<Identifier<S>> someIds) {
        LOGGER.trace("Getting states for {} {}", stateType, someIds);
        final Set<CurrentState<S, T>> results = new HashSet<>();
        for (final Identifier<S> id : someIds) {
            final CurrentState<S, T> currentState = getCurrentState(id);
            results.add(currentState);
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<CurrentState<S, T>> getCurrentStates(final PaginationParameter somePagination) {
        LOGGER.trace("Getting all states for {}", stateType);
        return persistenceService.getAllKnownStates(somePagination);
    }

    protected abstract CurrentState<S, T> createUnknown(final Identifier<S> anId);

    protected abstract void sendNotification(CurrentState<S, T> anUpdatedState);
}
