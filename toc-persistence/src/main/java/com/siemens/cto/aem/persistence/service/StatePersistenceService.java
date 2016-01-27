package com.siemens.cto.aem.persistence.service;

import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;

import java.util.Set;

public interface StatePersistenceService<S, T  extends OperationalState> {

    CurrentState<S, T> updateState(SetStateRequest<S, T> setStateRequest);

    CurrentState<S, T> getState(final Identifier<S> anId, StateType stateType);

    Set<CurrentState<S, T>> getAllKnownStates();

}
