package com.siemens.cto.aem.persistence.service.group.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.group.GroupControlHistory;
import com.siemens.cto.aem.domain.model.group.command.CompleteControlGroupCommand;
import com.siemens.cto.aem.domain.model.group.command.GroupCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroupControlHistory;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaGroupControlHistoryBuilder;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupControlCrudService;
import com.siemens.cto.aem.persistence.service.group.GroupControlPersistenceService;

public class JpaGroupControlPersistenceServiceImpl implements GroupControlPersistenceService {

    private final GroupControlCrudService crudService;

    public JpaGroupControlPersistenceServiceImpl(final GroupControlCrudService theService) {
        crudService = theService;
    }

    @Override
    public GroupControlHistory addIncompleteControlHistoryEvent(final Event<GroupCommand> anEvent) {
        final JpaGroupControlHistory history = crudService.addIncompleteControlHistoryEvent(anEvent);
        return new JpaGroupControlHistoryBuilder(history).build();
    }

    @Override
    public GroupControlHistory completeControlHistoryEvent(final Event<CompleteControlGroupCommand> anEvent) {
        final JpaGroupControlHistory history = crudService.completeControlHistoryEvent(anEvent);
        return new JpaGroupControlHistoryBuilder(history).build();
    }
}
