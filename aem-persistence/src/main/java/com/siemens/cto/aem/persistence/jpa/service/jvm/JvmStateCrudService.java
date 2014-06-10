package com.siemens.cto.aem.persistence.jpa.service.jvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaCurrentJvmState;

public interface JvmStateCrudService {

    JpaCurrentJvmState updateJvmState(final Event<SetJvmStateCommand> anEvent);

    JpaCurrentJvmState getJvmState(final Identifier<Jvm> aJvmId);
}
