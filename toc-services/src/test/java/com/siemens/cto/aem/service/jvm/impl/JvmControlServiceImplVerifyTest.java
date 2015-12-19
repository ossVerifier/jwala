package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.common.request.state.JvmSetStateRequest;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.jvm.command.impl.WindowsJvmPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.jvm.JvmService;
import com.siemens.cto.aem.service.state.StateService;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl impl;
    private JvmControlServiceImpl.LifecycleImpl lifecycleImpl;
    private JvmService jvmService;
    private RemoteCommandExecutor commandExecutor;
    private User user;

    @Captor
    private ArgumentCaptor<JvmSetStateRequest> setJvmStateCommand;

    @Mock
    private StateService<Jvm, JvmState> jvmStateService;

    @Mock
    private HistoryService mockHistoryService;

    private List<JpaGroup> groups = new ArrayList<>();

    public JvmControlServiceImplVerifyTest() {
        this.groups.add(new JpaGroup());
    }

    @Before
    public void setup() {
        jvmService = mock(JvmService.class);
        commandExecutor = mock(RemoteCommandExecutor.class);
        lifecycleImpl = new JvmControlServiceImpl.LifecycleImpl(jvmStateService);
        impl = new JvmControlServiceImpl(jvmService, commandExecutor, lifecycleImpl, mockHistoryService);
        user = new User("unused");
        when(jvmService.getJpaJvm(any(Identifier.class), eq(true))).thenReturn(new JpaJvm());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final JpaJvm jvm = mock(JpaJvm.class);
        final String jvmName = "mockJvmName";
        String jvmHost = "mockJvmHost";
        when(jvm.getName()).thenReturn(jvmName);
        when(jvm.getHostName()).thenReturn(jvmHost);
        final Identifier<Jvm> jvmId = mock(Identifier.class);
        final JvmControlOperation controlOperation = JvmControlOperation.START;
        final CommandOutput mockExecData = mock(CommandOutput.class);

        when(jvm.getId()).thenReturn(1l);
        when(commandExecutor.executeRemoteCommand(jvmName, jvmHost, controlOperation, new WindowsJvmPlatformCommandProvider())).thenReturn(mockExecData);
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(jvmService.getJpaJvm(jvmId, true)).thenReturn(jvm);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));

        impl.controlJvm(controlCommand, user);

        verify(controlCommand, times(1)).validate();
        //TODO change to use @Captor instead, much easier
        verify(jvmService, times(1)).getJpaJvm(eq(jvmId), eq(true));
        verify(commandExecutor, times(1)).executeRemoteCommand(
                eq(jvmName),
                eq(jvmHost),
                eq(controlOperation),
                any(WindowsJvmPlatformCommandProvider.class));
        verify(jvmStateService, times(1)).setCurrentState(setJvmStateCommand.capture(),
                eq(user));

        assertEquals(jvmId,
                setJvmStateCommand.getValue().getNewState().getId());
        assertEquals(controlOperation.getOperationState(),
                setJvmStateCommand.getValue().getNewState().getState());

        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
    }

    @Test
    public void testVerificationOfBehaviorForFailures() throws CommandFailureException {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final CommandOutput mockExecData = mock(CommandOutput.class);
        final Identifier<Jvm> jvmId = new Identifier<>(1L);
        final JpaJvm mockJvm = mock(JpaJvm.class);

        when(mockJvm.getId()).thenReturn(jvmId.getId());
        when(mockJvm.getName()).thenReturn("testJvmName");
        when(mockJvm.getHostName()).thenReturn("testJvmHost");
        when(jvmService.getJpaJvm(any(Identifier.class), eq(true))).thenReturn(mockJvm);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("Test standard error");
        when(mockExecData.getStandardOutput()).thenReturn("Test standard out when START or STOP");
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS));
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(commandExecutor.executeRemoteCommand("testJvmName", "testJvmHost", JvmControlOperation.START, new WindowsJvmPlatformCommandProvider())).thenReturn(mockExecData);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(jvmStateService.getCurrentState(any(Identifier.class))).thenReturn(new CurrentState<>(jvmId, JvmState.JVM_STARTED, DateTime.now(), StateType.JVM));

        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_FAST_FAIL));
        boolean exceptionThrown = false;
        try {
            impl.controlJvm(controlCommand, user);
            verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE));
        exceptionThrown = false;
        try {
            impl.controlJvm(controlCommand, user);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(88 /*non-existent return code*/));
        when(mockExecData.standardErrorOrStandardOut()).thenReturn("Test standard error or out");
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);
        exceptionThrown = false;
        boolean isNPE = false;
        try {
            impl.controlJvm(controlCommand, user);
        } catch (Exception e) {
            e.printStackTrace();
            exceptionThrown = true;
            isNPE = e instanceof NullPointerException;
        }
        assertFalse(exceptionThrown);
        assertFalse(isNPE); // NPE = unacceptable!

    }

    @Test
    public void testSecureCopyConfFile() throws CommandFailureException {
        ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockControlJvmRequest.getJvmId()).thenReturn(new Identifier<Jvm>(11L));
        when(jvmService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(ControlJvmRequest.class), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(mockCommandOutput);
        impl.secureCopyFile(mockControlJvmRequest, "src path", "dest path");
    }


}
