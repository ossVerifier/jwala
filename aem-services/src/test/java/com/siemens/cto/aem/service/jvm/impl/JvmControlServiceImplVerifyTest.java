package com.siemens.cto.aem.service.jvm.impl;

import org.junit.Before;
import org.junit.Test;

import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl impl;
    private JvmControlPersistenceService persistenceService;
    private User user;

    @Before
    public void setup() {
        persistenceService = mock(JvmControlPersistenceService.class);
        impl = new JvmControlServiceImpl(persistenceService);
        user = new User("unused");
    }

    @Test
    public void testControlJvmShouldValidateCommand() {
        final ControlJvmCommand command = mock(ControlJvmCommand.class);
        impl.controlJvm(command,
                        user);
        verify(command, times(1)).validateCommand();
        verify(persistenceService, times(1)).addControlHistoryEvent(matchCommandInEvent(command));
    }
}
