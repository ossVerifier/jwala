package com.siemens.cto.aem.persistence.jpa.service.state;

import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.common.domain.model.event.Event;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.state.OperationalState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface StateCrudService<S, T extends OperationalState> {

    JpaCurrentState updateState(final Event<SetStateRequest<S, T>> anEvent);

    JpaCurrentState getState(final Identifier<S> anId);

    List<JpaCurrentState> getStates();

    List<JpaCurrentState> markStaleStates(StateType stateType, T staleState, Date cutoff, AuditEvent auditData);

    List<JpaCurrentState> markStaleStates(StateType stateType, T staleState, Collection<String> checkStates, Date cutoff,
            AuditEvent auditData);

}
