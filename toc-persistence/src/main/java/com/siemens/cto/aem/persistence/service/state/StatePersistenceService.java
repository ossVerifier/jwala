package com.siemens.cto.aem.persistence.service.state;

import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface StatePersistenceService<S, T  extends OperationalState> {

    CurrentState<S, T> updateState(final Event<SetStateRequest<S, T>> aNewState);

    CurrentState<S, T> getState(final Identifier<S> anId);

    Set<CurrentState<S, T>> getAllKnownStates();

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
