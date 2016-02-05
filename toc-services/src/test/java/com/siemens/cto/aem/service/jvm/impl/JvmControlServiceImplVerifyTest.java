package com.siemens.cto.aem.service.jvm.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.common.domain.model.jvm.JvmState;
import com.siemens.cto.aem.common.domain.model.state.CurrentState;
import com.siemens.cto.aem.common.domain.model.state.StateType;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.ExternalSystemErrorException;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl jvmControlService;
    private JvmService jvmService;
    private RemoteCommandExecutor commandExecutor;
    private User user;

    @Captor
    private ArgumentCaptor<JvmSetStateRequest> setJvmStateCommand;

    @Mock
    private StateService<Jvm, JvmState> jvmStateService;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private StateService<Jvm, JvmState> mockJvmStateStateService;

    private List<JpaGroup> groups = new ArrayList<>();

    public JvmControlServiceImplVerifyTest() {
        this.groups.add(new JpaGroup());
    }

    @Before
    public void setup() {
        jvmService = mock(JvmService.class);
        commandExecutor = mock(RemoteCommandExecutor.class);
        jvmControlService = new JvmControlServiceImpl(jvmService, commandExecutor, mockHistoryService
        );
        user = new User("unused");
        when(jvmService.getJpaJvm(any(Identifier.class), eq(true))).thenReturn(new JpaJvm());
    }

    @Test
    @SuppressWarnings("unchecked")
    // TODO: Fix this test!
    @Ignore
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final Jvm jvm = mock(Jvm.class);
        final String jvmName = "mockJvmName";
        String jvmHost = "mockJvmHost";
        when(jvm.getJvmName()).thenReturn(jvmName);
        when(jvm.getHostName()).thenReturn(jvmHost);
        final Identifier<Jvm> jvmId = mock(Identifier.class);
        final JvmControlOperation controlOperation = JvmControlOperation.START;
        final CommandOutput mockExecData = mock(CommandOutput.class);

        when(jvm.getId()).thenReturn(Identifier.<Jvm>id(1L));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(mockExecData);
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(jvmService.getJvm(jvmId)).thenReturn(jvm);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));

        jvmControlService.controlJvm(controlCommand, user);

        verify(controlCommand, times(1)).validate();
        //TODO change to use @Captor instead, much easier
        verify(jvmService, times(1)).getJvm(eq(jvmId));
        verify(commandExecutor, times(1)).executeRemoteCommand(eq(jvmName), eq(jvmHost), eq(controlOperation),
                any(WindowsJvmPlatformCommandProvider.class));
        verify(jvmService, times(1)).updateState(any(Identifier.class), any(JvmState.class));

        assertEquals(jvmId,
                setJvmStateCommand.getValue().getNewState().getId());
        assertEquals(controlOperation.getOperationState(),
                setJvmStateCommand.getValue().getNewState().getState());

        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());


        // test other command codes
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS), "Abnormal success", ""));
        CommandOutput returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertTrue(returnOutput.getReturnCode().getWasSuccessful());

        when(jvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP), "No op", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertNull(returnOutput);

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_FAST_FAIL), "", "Fast Fail"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), AemFaultType.FAST_FAIL);
        }

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE), "", "No such service"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), AemFaultType.REMOTE_COMMAND_FAILURE);
        }

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.STP_EXIT_PROCESS_KILLED), "", "Process killed"));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(88), "", "process default error"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        }catch (ExternalSystemErrorException ee){
            assertEquals(ee.getMessageResponseStatus(), AemFaultType.REMOTE_COMMAND_FAILURE);
        }

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS), "Abnormal success", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertTrue(returnOutput.getReturnCode().getWasSuccessful());

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(new CommandOutput(new ExecReturnCode(88), "The requested service has already been started.", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertNull(returnOutput);

        // test command failure
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(), any(WindowsJvmPlatformCommandProvider.class))).thenThrow(new CommandFailureException(new ExecCommand("Failed command"), new Throwable("Test command failure")));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (InternalErrorException ie) {
            assertEquals(ie.getMessageResponseStatus(), AemFaultType.REMOTE_COMMAND_FAILURE);
        }
    }

    @Test
    // TODO: Fix this test!
    @Ignore
    public void testVerificationOfBehaviorForOtherReturnCodes() throws CommandFailureException {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final CommandOutput mockExecData = mock(CommandOutput.class);
        final Identifier<Jvm> jvmId = new Identifier<>(1L);
        final Jvm mockJvm = mock(Jvm.class);

        when(mockJvm.getId()).thenReturn(Identifier.<Jvm>id(jvmId.getId()));
        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(mockJvm.getHostName()).thenReturn("testJvmHost");
        when(jvmService.getJvm(any(Identifier.class))).thenReturn(mockJvm);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(1));
        when(mockExecData.getStandardError()).thenReturn("Test standard error");
        when(mockExecData.getStandardOutput()).thenReturn("Test standard out when START or STOP");
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS));
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(commandExecutor.executeRemoteCommand(eq("testJvmName"), eq("testJvmHost"), eq(JvmControlOperation.START), any(WindowsJvmPlatformCommandProvider.class))).thenReturn(mockExecData);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_NO_OP));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);

        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_CODE_FAST_FAIL));

        boolean exceptionThrown = false;
        try {
            jvmControlService.controlJvm(controlCommand, user);
            verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE));
        exceptionThrown = false;
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(ExecReturnCode.STP_EXIT_PROCESS_KILLED));
        final CommandOutput commandOutput = jvmControlService.controlJvm(controlCommand, user);
        assertTrue(commandOutput.getReturnCode().wasSuccessful());

        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(88 /*non-existent return code*/));
        when(mockExecData.standardErrorOrStandardOut()).thenReturn("Test standard error or out");
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);

        jvmControlService.controlJvm(controlCommand, user);
    }

    @Test
    public void testSecureCopyConfFile() throws CommandFailureException {
        ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);
        when(mockControlJvmRequest.getJvmId()).thenReturn(new Identifier<Jvm>(11L));
        when(jvmService.getJpaJvm(any(Identifier.class), anyBoolean())).thenReturn(mockJpaJvm);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(ControlJvmRequest.class), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(mockCommandOutput);
        jvmControlService.secureCopyFile(mockControlJvmRequest, "src path", "dest path");
    }


}
