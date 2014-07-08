package com.siemens.cto.aem.service.jvm.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.SetJvmStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.jvm.state.JvmStateService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl impl;
    private JvmControlPersistenceService persistenceService;
    private JvmService jvmService;
    private JvmCommandExecutor commandExecutor;
    private JvmStateService jvmStateService;
    private User user;

    @Captor
    private ArgumentCaptor<SetJvmStateCommand> setJvmStateCommand;

    @Before
    public void setup() {
        persistenceService = mock(JvmControlPersistenceService.class);
        jvmService = mock(JvmService.class);
        commandExecutor = mock(JvmCommandExecutor.class);
        jvmStateService = mock(JvmStateService.class);
        impl = new JvmControlServiceImpl(persistenceService,
                                         jvmService,
                                         commandExecutor,
                                         jvmStateService);
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
        final JvmControlOperation controlOperation = JvmControlOperation.START;

        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(jvmService.getJvm(eq(jvmId))).thenReturn(jvm);
        when(incompleteHistory.getId()).thenReturn(historyId);
        when(persistenceService.addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand))).thenReturn(incompleteHistory);

        impl.controlJvm(controlCommand,
                        user);

        verify(controlCommand, times(1)).validateCommand();
        //TODO change to use @Captor instead, much easier
        verify(persistenceService, times(1)).addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand));
        verify(persistenceService, times(1)).completeControlHistoryEvent(Matchers.<Event<CompleteControlJvmCommand>>anyObject());
        verify(jvmService, times(1)).getJvm(eq(jvmId));
        verify(commandExecutor, times(1)).controlJvm(eq(controlCommand),
                                                     eq(jvm));
        verify(jvmStateService, times(1)).setCurrentJvmState(setJvmStateCommand.capture(),
                                                             eq(user));

        assertEquals(jvmId,
                     setJvmStateCommand.getValue().getNewJvmState().getJvmId());
        assertEquals(controlOperation.getOperationState(),
                     setJvmStateCommand.getValue().getNewJvmState().getJvmState());
    }
}
