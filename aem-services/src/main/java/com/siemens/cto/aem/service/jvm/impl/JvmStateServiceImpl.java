package com.siemens.cto.aem.service.jvm.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.CurrentJvmState;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmStatePersistenceService;
import com.siemens.cto.aem.service.jvm.JvmStateNotificationService;
import com.siemens.cto.aem.service.jvm.JvmStateService;

public class JvmStateServiceImpl implements JvmStateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmStateServiceImpl.class);

    private final JvmStatePersistenceService persistenceService;
    private final JvmStateNotificationService notificationService;

    public JvmStateServiceImpl(final JvmStatePersistenceService theService,
                               final JvmStateNotificationService theNotificationService) {
        persistenceService = theService;
        notificationService = theNotificationService;
    }

    @Override
    @Transactional
    public CurrentJvmState setCurrentJvmState(final SetJvmStateCommand aCommand,
                                              final User aUser) {
        LOGGER.info("Attempting to set state for Jvm {} ", aCommand);
        aCommand.validateCommand();

        notificationService.notifyJvmStateUpdated(aCommand.getNewJvmState().getJvmId());
        return persistenceService.updateJvmState(new Event<>(aCommand,
                                                             AuditEvent.now(aUser)));
    }
}
