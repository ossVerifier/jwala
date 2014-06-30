package com.siemens.cto.aem.persistence.service.group;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;

public interface GroupControlPersistenceService {

    GroupControlHistory addIncompleteControlHistoryEvent(final Event<ControlGroupCommand> anEvent);

    GroupControlHistory completeControlHistoryEvent(final Event<CompleteControlGroupCommand> anEvent);

}
