package com.siemens.cto.aem.service.state.impl;

import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;
import com.siemens.cto.aem.service.state.*;
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
    private StateNotificationWorker stateNotificationWorker;
    private GroupStateService.API groupStateService;

    public StateServiceImpl(final StatePersistenceService<S, T> thePersistenceService,
                            final StateNotificationService theNotificationService, final StateType theStateType,
                            final GroupStateService.API groupStateService,
                            final StateNotificationWorker stateNotificationWorker) {
        persistenceService = thePersistenceService;
        notificationService = theNotificationService;
        stateType = theStateType;
        this.groupStateService = groupStateService;
        this.stateNotificationWorker = stateNotificationWorker;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CurrentState<S, T> setCurrentState(final SetStateRequest<S, T> setStateRequest, final User aUser) {
        LOGGER.trace("Attempting to set state for {} {} ", stateType, setStateRequest);
        setStateRequest.validate();

        final CurrentState<S, T> currentState = persistenceService.getState(setStateRequest.getNewState().getId());
        final CurrentState<S, T> latestState = persistenceService.updateState(new Event<>(setStateRequest, AuditEvent
                .now(aUser)));

        if (currentState == null || !currentState.getState().equals(latestState.getState()) ||
            !currentState.getMessage().equalsIgnoreCase(latestState.getMessage())) {
            latestState.setUserId(aUser.getId());
            notificationService.notifyStateUpdated(latestState);
        }

        stateNotificationWorker.sendStateChangeNotification(groupStateService, latestState);

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

    protected abstract CurrentState<S, T> createUnknown(final Identifier<S> anId);

    protected abstract void sendNotification(CurrentState<S, T> anUpdatedState);

    protected StateNotificationWorker getStateNotificationWorker() {
        return stateNotificationWorker;
    }
}
