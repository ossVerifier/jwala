package com.siemens.cto.aem.service.jvm.impl;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;

import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.jvm.JvmService;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl impl;
    private JvmControlPersistenceService persistenceService;
    private JvmService jvmService;
    private JvmCommandExecutor commandExecutor;
    private User user;

    @Before
    public void setup() {
        persistenceService = mock(JvmControlPersistenceService.class);
        jvmService = mock(JvmService.class);
        commandExecutor = mock(JvmCommandExecutor.class);
        impl = new JvmControlServiceImpl(persistenceService,
                                         jvmService,
                                         commandExecutor);
        user = new User("unused");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlJvmCommand controlCommand = mock(ControlJvmCommand.class);
        final Jvm jvm = mock(Jvm.class);
        final Identifier<Jvm> jvmId = mock(Identifier.class);
        final Identifier<JvmControlHistory> historyId = mock(Identifier.class);
        final JvmControlHistory incompleteHistory = mock(JvmControlHistory.class);

        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(jvmService.getJvm(eq(jvmId))).thenReturn(jvm);
        when(incompleteHistory.getId()).thenReturn(historyId);
        when(persistenceService.addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand))).thenReturn(incompleteHistory);

        impl.controlJvm(controlCommand,
                        user);

        verify(controlCommand, times(1)).validateCommand();
        verify(persistenceService, times(1)).addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand));
        verify(persistenceService, times(1)).completeControlHistoryEvent(Matchers.<Event<CompleteControlJvmCommand>>anyObject());
        verify(jvmService, times(1)).getJvm(eq(jvmId));
        verify(commandExecutor, times(1)).controlJvm(eq(controlCommand),
                                                     eq(jvm));
    }
}
