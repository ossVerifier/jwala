package com.siemens.cto.aem.service.state;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.user.User;

import java.util.Set;

public interface StateService<S, T extends OperationalState> {

    CurrentState<S, T> setCurrentState(final SetStateRequest<S, T> setStateRequest,
                                       final User aUser);

    CurrentState<S, T> getCurrentState(final Identifier<S> anId);

    Set<CurrentState<S, T>> getCurrentStates(final Set<Identifier<S>> someIds);

    Set<CurrentState<S, T>> getCurrentStates();

    /**
    * Periodically invoked by spring to mark services that 
    * are stuck in SHUTTING DOWN (due to manual termination) 
    * Parameterized in toc-defaults:
    * states.stopped-check.initial-delay.millis=120000
    * states.stopped-check.period.millis=60000
    * states.stopped-check.jvm.max-stop-time.millis=120000
    */
    void checkForStoppedStates();
}