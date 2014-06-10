package com.siemens.cto.aem.persistence.service.jvm;

import java.util.Set;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;

public interface JvmStatePersistenceService {

    CurrentJvmState updateJvmState(final Event<SetJvmStateCommand> aNewState);

    CurrentJvmState getJvmState(final Identifier<Jvm> aJvmId);

    Set<CurrentJvmState> getAllKnownJvmStates(final PaginationParameter somePagination);
}
