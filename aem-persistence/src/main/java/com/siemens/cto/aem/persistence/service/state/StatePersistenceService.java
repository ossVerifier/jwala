package com.siemens.cto.aem.persistence.service.state;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.OperationalState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface StatePersistenceService<S, T  extends OperationalState> {

    CurrentState<S, T> updateState(final Event<SetStateCommand<S, T>> aNewState);

    CurrentState<S, T> getState(final Identifier<S> anId);

    Set<CurrentState<S, T>> getAllKnownStates(final PaginationParameter somePagination);

    /**
     * Identify states that have not been updated since the cutoff
     * @param cutoff only states before this time will be changed
     * @param jvmStale the state that stale states should be converted to
     * @return modified states
     */
    List<CurrentState<S, T>> markStaleStates(StateType stateType, T staleState, Date cutoff, AuditEvent auditData);


    /**
     * Identify states that have not been updated since the cutoff
     * @param cutoff only states before this time will be changed
     * @param jvmStale the state that stale states should be converted to
     * @param checkStates a list of states that should be checked
     * @return modified states
     */
    List<CurrentState<S, T>> markStaleStates(StateType stateType, T staleState, Collection<T> checkStates, Date cutoff, AuditEvent auditData);
}
