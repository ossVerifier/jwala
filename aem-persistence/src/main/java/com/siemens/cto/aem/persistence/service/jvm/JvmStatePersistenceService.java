package com.siemens.cto.aem.persistence.service.jvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;

public interface JvmStatePersistenceService {

    CurrentJvmState updateJvmState(final Event<SetJvmStateCommand> aNewState);

    CurrentJvmState getJvmState(final Identifier<Jvm> aJvmId);
}
