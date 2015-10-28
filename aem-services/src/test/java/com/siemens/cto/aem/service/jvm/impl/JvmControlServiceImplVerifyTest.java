package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlHistory;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.JvmState;
import com.siemens.cto.aem.domain.model.jvm.command.CompleteControlJvmCommand;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.state.CurrentState;
import com.siemens.cto.aem.domain.model.state.StateType;
import com.siemens.cto.aem.domain.model.state.command.JvmSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.service.jvm.JvmControlPersistenceService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl impl;
    private JvmControlServiceImpl.LifecycleImpl lifecycleImpl;
    private JvmControlPersistenceService persistenceService;
    private JvmService jvmService;
    private JvmCommandExecutor commandExecutor;
    private User user;

    @Captor
    private ArgumentCaptor<JvmSetStateCommand> setJvmStateCommand;

    @Mock
    private StateService<Jvm, JvmState> jvmStateService;

    @Before
    public void setup() {
        persistenceService = mock(JvmControlPersistenceService.class);
        jvmService = mock(JvmService.class);
        commandExecutor = mock(JvmCommandExecutor.class);
        lifecycleImpl = new JvmControlServiceImpl.LifecycleImpl(persistenceService, jvmStateService);
        impl = new JvmControlServiceImpl(jvmService,
                commandExecutor,
                lifecycleImpl);
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
        final ExecData mockExecData = mock(ExecData.class);

        when(commandExecutor.controlJvm(controlCommand, jvm)).thenReturn(mockExecData);
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(jvmService.getJvm(eq(jvmId))).thenReturn(jvm);
        when(incompleteHistory.getId()).thenReturn(historyId);
        when(incompleteHistory.getExecData()).thenReturn(mockExecData);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        when(persistenceService.addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand))).thenReturn(incompleteHistory);

        impl.controlJvm(controlCommand, user);

        verify(controlCommand, times(1)).validateCommand();
        //TODO change to use @Captor instead, much easier
        verify(persistenceService, times(1)).addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand));
        verify(persistenceService, times(1)).completeControlHistoryEvent(Matchers.<Event<CompleteControlJvmCommand>>anyObject());
        verify(jvmService, times(1)).getJvm(eq(jvmId));
        verify(commandExecutor, times(1)).controlJvm(eq(controlCommand),
                eq(jvm));
        verify(jvmStateService, times(1)).setCurrentState(setJvmStateCommand.capture(),
                eq(user));

        assertEquals(jvmId,
                setJvmStateCommand.getValue().getNewState().getId());
        assertEquals(controlOperation.getOperationState(),
                setJvmStateCommand.getValue().getNewState().getState());
    }

    @Test
    public void testVerificationOfBehaviorForFailures() throws CommandFailureException {
        final ControlJvmCommand controlCommand = mock(ControlJvmCommand.class);
        final ExecData mockExecData = mock(ExecData.class);
        final Identifier<Jvm> jvmId = new Identifier<Jvm>(1L);
        final Jvm mockJvm = mock(Jvm.class);
        final JvmControlHistory incompleteHistory = mock(JvmControlHistory.class);

        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(jvmService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("Test standard error");
        when(mockExecData.getStandardOutput()).thenReturn("Test standard out when START or STOP");
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS));
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(incompleteHistory.getId()).thenReturn(new Identifier<JvmControlHistory>(88L));
        when(incompleteHistory.getExecData()).thenReturn(mockExecData);
        when(persistenceService.addIncompleteControlHistoryEvent(matchCommandInEvent(controlCommand))).thenReturn(incompleteHistory);
        when(commandExecutor.controlJvm(controlCommand, mockJvm)).thenReturn(mockExecData);
        JvmControlHistory controlhist = impl.controlJvm(controlCommand, user);
        assertNull(controlhist);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(jvmStateService.getCurrentState(any(Identifier.class))).thenReturn(new CurrentState<Jvm, JvmState>(jvmId, JvmState.JVM_STARTED, DateTime.now(), StateType.JVM));
        controlhist = impl.controlJvm(controlCommand, user);
        assertNull(controlhist);

        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_FAST_FAIL));
        boolean exceptionThrown = false;
        try {
            impl.controlJvm(controlCommand, user);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE));
        exceptionThrown = false;
        try {
            impl.controlJvm(controlCommand, user);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(88 /*non-existent return code*/));
        when(mockExecData.standardErrorOrStandardOut()).thenReturn("Test standard error or out");
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);
        exceptionThrown = false;
        boolean isNPE = false;
        try {
            impl.controlJvm(controlCommand, user);
        } catch (Exception e) {
            exceptionThrown = true;
            isNPE = e instanceof NullPointerException;
        }
        assertTrue(exceptionThrown);
        assertFalse(isNPE); // NPE = unacceptable!

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(commandExecutor.controlJvm(any(ControlJvmCommand.class), any(Jvm.class))).thenReturn(new ExecData(new ExecReturnCode(88), "The requested service has already been started.", "The requested service has already been started."));
        controlhist = impl.controlJvm(controlCommand, user);
        assertNull(controlhist);
    }
}
