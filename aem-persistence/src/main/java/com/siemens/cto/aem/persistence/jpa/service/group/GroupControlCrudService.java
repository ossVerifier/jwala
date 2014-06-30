package com.siemens.cto.aem.persistence.jpa.service.group;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupControlHistory;

public interface GroupControlCrudService {

    JpaGroupControlHistory addIncompleteControlHistoryEvent(final Event<ControlGroupCommand> anEvent);

    JpaGroupControlHistory completeControlHistoryEvent(final Event<CompleteControlGroupCommand> anEvent);
}
