package com.siemens.cto.aem.persistence.jpa.service.jvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory;

public interface JvmControlCrudService {

    JpaJvmControlHistory addControlHistoryEvent(final Event<ControlJvmCommand> anEvent);
}
