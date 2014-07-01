package com.siemens.cto.aem.persistence.jpa.service.group.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.ControlGroupCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupControlHistory;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupControlCrudService;

public class GroupControlCrudServiceImpl implements GroupControlCrudService {

    @Override
    public JpaGroupControlHistory addIncompleteControlHistoryEvent(Event<ControlGroupCommand> anEvent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public JpaGroupControlHistory completeControlHistoryEvent(Event<CompleteControlGroupCommand> anEvent) {
        // TODO Auto-generated method stub
        return null;
    }

}
