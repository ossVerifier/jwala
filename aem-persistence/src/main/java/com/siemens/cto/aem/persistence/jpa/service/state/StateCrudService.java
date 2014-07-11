package com.siemens.cto.aem.persistence.jpa.service.state;

import java.util.List;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.ExternalizableState;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentState;

public interface StateCrudService<S, T extends ExternalizableState> {

    JpaCurrentState updateState(final Event<SetStateCommand<S, T>> anEvent);

    JpaCurrentState getState(final Identifier<S> anId);

    List<JpaCurrentState> getStates(final PaginationParameter somePagination);
}
