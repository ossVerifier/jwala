package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.service.StateCrudService;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public abstract class JpaStatePersistenceServiceImpl<S, T extends OperationalState> implements StatePersistenceService<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaStatePersistenceServiceImpl.class);

    private final StateCrudService<S, T> stateCrudService;

    public JpaStatePersistenceServiceImpl(final StateCrudService<S, T> theService) {
        stateCrudService = theService;
    }

    public CurrentState<S, T> updateState(SetStateRequest<S, T> setStateRequest) {
        LOGGER.debug("Persisting new state {}", setStateRequest);
        final JpaCurrentState currentState = stateCrudService.updateState(setStateRequest);
        return build(currentState);
    }

    @Override
    public CurrentState<S, T> getState(final Identifier<S> anId) {
        final JpaCurrentState currentState = stateCrudService.getState(anId);
        return build(currentState);
    }

    @Override
    public Set<CurrentState<S, T>> getAllKnownStates() {
        final Set<CurrentState<S, T>> results = new HashSet<>();
        final List<JpaCurrentState> currentJpaStates = stateCrudService.getStates();
        for (final JpaCurrentState state : currentJpaStates) {
            results.add(build(state));
        }
        return results;
    }

    protected CurrentState<S, T> build(final JpaCurrentState aCurrentState) {
        return build(aCurrentState, null);
    }
    
    protected abstract CurrentState<S, T> build(final JpaCurrentState aCurrentState, T staleState);
}
