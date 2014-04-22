package com.siemens.cto.aem.service.jvm.impl;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.jvm.JvmControlService;

public class JvmControlServiceImpl implements JvmControlService {

    private final JvmControlPersistenceService persistenceService;

    public JvmControlServiceImpl(final JvmControlPersistenceService thePersistenceService) {
        persistenceService = thePersistenceService;
    }

    @Override
    @Transactional
    public JvmControlHistory controlJvm(final ControlJvmCommand aCommand,
                                        final User aUser) {

        aCommand.validateCommand();

        final JvmControlHistory history = persistenceService.addControlHistoryEvent(new Event<>(aCommand,
                                                                                                AuditEvent.now(aUser)));

        return history;
    }
}
