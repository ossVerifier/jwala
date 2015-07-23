package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.StateNotificationGateway;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

public abstract class StateServiceImpl<S, T extends OperationalState> implements StateService<S, T> {

    private static final Logger                 LOGGER = LoggerFactory.getLogger(StateServiceImpl.class);

    private final StatePersistenceService<S, T> persistenceService;
    private final StateNotificationService      notificationService;
    private final StateType                     stateType;
    private final StateNotificationGateway    stateNotificationGateway;

    public StateServiceImpl(final StatePersistenceService<S, T> thePersistenceService,
            final StateNotificationService theNotificationService, final StateType theStateType,
            final StateNotificationGateway theStateNotificationGateway) {
        persistenceService = thePersistenceService;
        notificationService = theNotificationService;
        stateType = theStateType;
        stateNotificationGateway = theStateNotificationGateway;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CurrentState<S, T> setCurrentState(final SetStateCommand<S, T> aCommand, final User aUser) {
        LOGGER.trace("Attempting to set state for {} {} ", stateType, aCommand);
        aCommand.validateCommand();

        final CurrentState<S, T> currentState = persistenceService.getState(aCommand.getNewState().getId());

        final CurrentState<S, T> latestState = persistenceService.updateState(new Event<>(aCommand, AuditEvent
                .now(aUser)));

        if (    currentState == null || 
                currentState.getState() != latestState.getState() || 
                !currentState.getMessage().equals(latestState.getMessage())) {

            notificationService.notifyStateUpdated(latestState); // the UI only
                                                                 // cares about
                                                                 // changes.

        }

        // The internal bus is allowed to care about all state updates.
        sendNotification(latestState);
        return latestState;
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
    public Set<CurrentState<S, T>> getCurrentStates() {
        LOGGER.trace("Getting all states for {}", stateType);
        return persistenceService.getAllKnownStates();
    }

    /**
     * Accessor for derived class.
     */
    protected StateNotificationService getNotificationService() {
        return notificationService;
    }

    /**
     * Accessor for derived class.
     */
    protected StatePersistenceService<S,T> getPersistenceService() {
        return persistenceService;
    }
    
    /**
     * Accessor for derived class.
     */
    protected StateNotificationGateway getStateNotificationGateway() {
        return stateNotificationGateway;
    }

    protected abstract CurrentState<S, T> createUnknown(final Identifier<S> anId);

    protected abstract void sendNotification(CurrentState<S, T> anUpdatedState);
}
