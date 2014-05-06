package com.siemens.cto.aem.persistence.service.jvm.impl;

import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvmControlHistory;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaJvmControlHistoryBuilder;
import com.siemens.cto.aem.persistence.jpa.service.jvm.JvmControlCrudService;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;

public class JpaJvmControlPersistenceServiceImpl implements JvmControlPersistenceService {

    private final JvmControlCrudService crudService;

    public JpaJvmControlPersistenceServiceImpl(final JvmControlCrudService theService) {
        crudService = theService;
    }

    @Override
    public JvmControlHistory addIncompleteControlHistoryEvent(final Event<ControlJvmCommand> anEvent) {
        final JpaJvmControlHistory history = crudService.addIncompleteControlHistoryEvent(anEvent);
        return new JpaJvmControlHistoryBuilder(history).build();
    }

    @Override
    public JvmControlHistory completeControlHistoryEvent(final Event<CompleteControlJvmCommand> anEvent) {
        final JpaJvmControlHistory history = crudService.completeControlHistoryEvent(anEvent);
        return new JpaJvmControlHistoryBuilder(history).build();
    }
}
