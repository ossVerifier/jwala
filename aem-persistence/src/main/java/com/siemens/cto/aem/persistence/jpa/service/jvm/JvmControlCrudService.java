package com.siemens.cto.aem.persistence.jpa.service.jvm;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory;

public interface JvmControlCrudService {

    JpaJvmControlHistory addIncompleteControlHistoryEvent(final Event<ControlJvmCommand> anEvent);

    JpaJvmControlHistory completeControlHistoryEvent(final Event<CompleteControlJvmCommand> anEvent);
}
