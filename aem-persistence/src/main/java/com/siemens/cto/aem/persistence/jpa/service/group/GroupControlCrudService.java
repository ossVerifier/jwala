package com.siemens.cto.aem.persistence.jpa.service.group;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.GroupCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupControlHistory;

public interface GroupControlCrudService {

    JpaGroupControlHistory addIncompleteControlHistoryEvent(final Event<GroupCommand> anEvent);

    JpaGroupControlHistory completeControlHistoryEvent(final Event<CompleteControlGroupCommand> anEvent);
}
