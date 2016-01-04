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

}