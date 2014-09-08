package com.siemens.cto.aem.persistence.service.state;

import java.util.Set;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface StatePersistenceService<S, T  extends OperationalState> {

    CurrentState<S, T> updateState(final Event<SetStateCommand<S, T>> aNewState);

    CurrentState<S, T> getState(final Identifier<S> anId);

    Set<CurrentState<S, T>> getAllKnownStates(final PaginationParameter somePagination);
}
