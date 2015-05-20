package com.siemens.cto.aem.service.state;

import java.util.Set;

import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;

public interface StateService<S, T extends OperationalState> {

    CurrentState<S, T> setCurrentState(final SetStateCommand<S, T> aCommand,
                                       final User aUser);

    CurrentState<S, T> getCurrentState(final Identifier<S> anId);

    Set<CurrentState<S, T>> getCurrentStates(final Set<Identifier<S>> someIds);

    Set<CurrentState<S, T>> getCurrentStates(final PaginationParameter somePagination);

    /**
     * Periodically invoked by spring to convert states to STALE
     * Implementations are supposed to parameterized in toc-defaults, e.g.:
     * states.stale-check.initial-delay.millis=45000
     * states.stale-check.period.millis=60000
     * states.stale-check.jvm.stagnation.millis=60000
     */
    void checkForStaleStates();
}