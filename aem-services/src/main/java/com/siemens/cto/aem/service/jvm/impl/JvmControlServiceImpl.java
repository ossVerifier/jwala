package com.siemens.cto.aem.service.jvm.impl;

import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.commandprocessor.domain.ExecutionData;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.domain.model.audit.AuditEvent;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.jvm.JvmControlService;
import com.siemens.cto.aem.service.jvm.JvmService;

public class JvmControlServiceImpl implements JvmControlService {

    private final JvmControlPersistenceService persistenceService;
    private final JvmService jvmService;
    private final JvmCommandExecutor jvmCommandExecutor;

    public JvmControlServiceImpl(final JvmControlPersistenceService thePersistenceService,
                                 final JvmService theJvmService,
                                 final JvmCommandExecutor theExecutor) {
        persistenceService = thePersistenceService;
        jvmService = theJvmService;
        jvmCommandExecutor = theExecutor;
    }

    @Override
    @Transactional
    public JvmControlHistory controlJvm(final ControlJvmCommand aCommand,
                                        final User aUser) {

        try {
            aCommand.validateCommand();

            final JvmControlHistory history = persistenceService.addControlHistoryEvent(new Event<>(aCommand,
                                                                                                    AuditEvent.now(aUser)));
            final Jvm jvm = jvmService.getJvm(aCommand.getJvmId());

            final ExecutionData returnCode = jvmCommandExecutor.controlJvm(aCommand,
                                                                           jvm);

            //TODO Update history with result of command execution?
            return history;
        } catch (final CommandFailureException cfe) {
            throw new RuntimeException("CommandFailureException when attempting to control a JVM: " + aCommand,
                                       cfe);
        }
    }
}
