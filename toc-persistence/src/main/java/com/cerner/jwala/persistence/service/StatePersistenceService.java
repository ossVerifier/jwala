package com.cerner.jwala.persistence.service;

import java.util.Set;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.state.OperationalState;
import com.cerner.jwala.common.domain.model.state.StateType;
import com.cerner.jwala.common.request.state.SetStateRequest;

public interface StatePersistenceService<S, T  extends OperationalState> {

    CurrentState<S, T> updateState(SetStateRequest<S, T> setStateRequest);

    CurrentState<S, T> getState(final Identifier<S> anId, StateType stateType);

    Set<CurrentState<S, T>> getAllKnownStates();

}
