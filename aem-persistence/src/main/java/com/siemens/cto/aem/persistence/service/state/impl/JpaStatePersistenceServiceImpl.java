package com.siemens.cto.aem.persistence.service.state.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;
import com.siemens.cto.aem.persistence.jpa.service.state.StateCrudService;
import com.siemens.cto.aem.persistence.service.state.StatePersistenceService;

public abstract class JpaStatePersistenceServiceImpl<S, T extends ExternalizableState> implements StatePersistenceService<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaStatePersistenceServiceImpl.class);

    private final StateCrudService<S, T> stateCrudService;

    public JpaStatePersistenceServiceImpl(final StateCrudService<S, T> theService) {
        stateCrudService = theService;
    }

    public CurrentState<S, T> updateState(final Event<SetStateCommand<S, T>> aNewState) {
        final SetStateCommand<S, T> command = aNewState.getCommand();
        LOGGER.debug("Persisting new state {}", command);
        final JpaCurrentState currentState = stateCrudService.updateState(aNewState);
        return build(currentState);
    }

    @Override
    public CurrentState<S, T> getState(final Identifier<S> anId) {
        final JpaCurrentState currentState = stateCrudService.getState(anId);
        return build(currentState);
    }

    @Override
    public Set<CurrentState<S, T>> getAllKnownStates(final PaginationParameter somePagination) {
        final Set<CurrentState<S, T>> results = new HashSet<>();
        final List<JpaCurrentState> currentJpaStates = stateCrudService.getStates(somePagination);
        for (final JpaCurrentState state : currentJpaStates) {
            results.add(build(state));
        }
        return results;
    }

    protected abstract CurrentState<S, T> build(final JpaCurrentState aCurrentState);
}
