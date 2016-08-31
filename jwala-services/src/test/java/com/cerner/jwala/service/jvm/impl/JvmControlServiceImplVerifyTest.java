package com.cerner.jwala.service.jvm.impl;

import com.cerner.jwala.common.domain.model.fault.AemFaultType;
import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.exception.ExternalSystemErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.request.jvm.ControlJvmRequest;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.jvm.command.impl.WindowsJvmPlatformCommandProvider;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.domain.JpaGroup;
import com.cerner.jwala.persistence.jpa.domain.JpaJvm;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.persistence.service.JvmPersistenceService;
import com.cerner.jwala.service.*;
import com.cerner.jwala.service.jvm.JvmStateService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JvmControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private JvmControlServiceImpl jvmControlService;
    private RemoteCommandExecutor commandExecutor;
    private User user;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private JvmStateService mockJvmStateService;

    @Mock
    private RemoteCommandExecutorService mockRemoteCommandExecutorService;

    @Mock
    private SshConfiguration mockSshConfig;

    @Mock
    private JvmPersistenceService mockJvmPersistenceService;

    private List<JpaGroup> groups = new ArrayList<>();

    public JvmControlServiceImplVerifyTest() {
        this.groups.add(new JpaGroup());
    }

    @Before
    public void setup() {
        commandExecutor = mock(RemoteCommandExecutor.class);
        jvmControlService = new JvmControlServiceImpl(mockJvmPersistenceService, commandExecutor, mockHistoryService, mockMessagingService,
                mockJvmStateService, mockRemoteCommandExecutorService, mockSshConfig);
        user = new User("unused");
    }

    @Test
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final Jvm jvm = mock(Jvm.class);
        final String jvmName = "mockJvmName";
        String jvmHost = "mockJvmHost";
        when(jvm.getJvmName()).thenReturn(jvmName);
        when(jvm.getHostName()).thenReturn(jvmHost);
        final Identifier<Jvm> jvmId = mock(Identifier.class);
        final JvmControlOperation controlOperation = JvmControlOperation.STOP;
        final CommandOutput mockExecData = mock(CommandOutput.class);
        final RemoteCommandReturnInfo remoteCommandSuccess = new RemoteCommandReturnInfo(0, "Success!", "");

        when(jvm.getId()).thenReturn(Identifier.<Jvm>id(1L));
        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(remoteCommandSuccess);
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(mockJvmPersistenceService.getJvm(jvmId)).thenReturn(jvm);
        when(mockExecData.getReturnCode()).thenReturn(new ExecReturnCode(0));

        jvmControlService.controlJvm(controlCommand, user);

        verify(mockJvmPersistenceService, times(1)).getJvm(eq(jvmId));
        verify(mockRemoteCommandExecutorService, times(1)).executeCommand(any(RemoteExecCommand.class));
        verify(mockJvmStateService, times(1)).updateState(any(Identifier.class), any(JvmState.class));
        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
        verify(mockMessagingService).send(any(CurrentState.class));


        // test other command codes
        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS, "Abnormal success", ""));
        CommandOutput returnOutput = jvmControlService.controlJvm(controlCommand, user);
        // abnormal success is not a successful return code
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());

        when(jvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_CODE_NO_OP, "No op", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_CODE_FAST_FAIL, "", "Fast Fail"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), AemFaultType.FAST_FAIL);
        }

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE, "", "No such service"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), AemFaultType.REMOTE_COMMAND_FAILURE);
        }

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_PROCESS_KILLED, "", "Process killed"));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(88, "", "Process default error"));
        try {
            jvmControlService.controlJvm(controlCommand, user);
        } catch (ExternalSystemErrorException ee) {
            assertEquals(ee.getMessageResponseStatus(), AemFaultType.REMOTE_COMMAND_FAILURE);
        }

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);
        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_CODE_ABNORMAL_SUCCESS, "Abnormal success", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(88, "The requested service has already been started", ""));
        returnOutput = jvmControlService.controlJvm(controlCommand, user);
        assertFalse(returnOutput.getReturnCode().getWasSuccessful());
    }

    @Test
    public void testVerificationOfBehaviorForOtherReturnCodes() throws CommandFailureException {
        final ControlJvmRequest controlCommand = mock(ControlJvmRequest.class);
        final Identifier<Jvm> jvmId = new Identifier<>(1L);
        final Jvm mockJvm = mock(Jvm.class);

        when(mockJvm.getId()).thenReturn(Identifier.<Jvm>id(jvmId.getId()));
        when(mockJvm.getJvmName()).thenReturn("testJvmName");
        when(mockJvm.getHostName()).thenReturn("testJvmHost");
        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);

        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STARTED);
        when(controlCommand.getJvmId()).thenReturn(jvmId);
        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_CODE_FAST_FAIL, "Test standard out when START or STOP", "Test standard error"));

        jvmControlService.controlJvm(controlCommand, user);
        verify(mockHistoryService, times(2)).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_NO_SUCH_SERVICE, "Test standard out when START or STOP", "Test standard error"));
        jvmControlService.controlJvm(controlCommand, user);

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.STP_EXIT_PROCESS_KILLED, "Test standard out when START or STOP", "Test standard error"));
        final CommandOutput commandOutput = jvmControlService.controlJvm(controlCommand, user);
        assertTrue(commandOutput.getReturnCode().wasSuccessful());

        when(mockRemoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(88, "", "Test standard error or out"));
        when(controlCommand.getControlOperation()).thenReturn(JvmControlOperation.HEAP_DUMP);

        jvmControlService.controlJvm(controlCommand, user);
    }

    @Test
    public void testSecureCopyConfFile() throws CommandFailureException {
        ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SECURE_COPY);
        when(mockControlJvmRequest.getJvmId()).thenReturn(new Identifier<Jvm>(11L));

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getGroups()).thenReturn(new HashSet<Group>());
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);

        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(ControlJvmRequest.class), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(mockCommandOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.CHECK_FILE_EXISTS), any(WindowsJvmPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File doesn't exist", ""));
        jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/path", "./dest/path", "user-id");

        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.SECURE_COPY), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString());
    }

    @Test
    public void testSecureCopyConfFilePerformsBackup() throws CommandFailureException {
        ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SECURE_COPY);
        when(mockControlJvmRequest.getJvmId()).thenReturn(new Identifier<Jvm>(11L));

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getGroups()).thenReturn(new HashSet<Group>());
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);

        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(ControlJvmRequest.class), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(mockCommandOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.CHECK_FILE_EXISTS), any(WindowsJvmPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists - do backup", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.BACK_UP_FILE), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Backup succeeded", ""));
        jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/path", "./dest/path", "user-id");

        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.SECURE_COPY), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString());
    }

    @Test
    public void testSecureCopyConfFileFailsBackup() throws CommandFailureException {
        ControlJvmRequest mockControlJvmRequest = mock(ControlJvmRequest.class);
        when(mockControlJvmRequest.getControlOperation()).thenReturn(JvmControlOperation.SECURE_COPY);
        when(mockControlJvmRequest.getJvmId()).thenReturn(new Identifier<Jvm>(11L));

        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("testJvm");
        when(mockJvm.getGroups()).thenReturn(new HashSet<Group>());
        JpaJvm mockJpaJvm = mock(JpaJvm.class);
        CommandOutput mockCommandOutput = mock(CommandOutput.class);

        when(mockJvmPersistenceService.getJvm(any(Identifier.class))).thenReturn(mockJvm);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(ControlJvmRequest.class), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(mockCommandOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.CHECK_FILE_EXISTS), any(WindowsJvmPlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File exists - do backup", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.BACK_UP_FILE), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Back up failed"));
        jvmControlService.secureCopyFile(mockControlJvmRequest, "./source/path", "./dest/path", "user-id");

        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.SECURE_COPY), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString());
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm");
        when(mockJvm.getHostName()).thenReturn("test-host");
        jvmControlService.makeExecutableUnixFormat(mockJvm, "777", "./target", "*");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.MAKE_UNIX_EXEC), any(WindowsJvmPlatformCommandProvider.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testCreateDirectory() throws CommandFailureException {
        Jvm mockJvm = mock(Jvm.class);
        when(mockJvm.getJvmName()).thenReturn("test-jvm");
        when(mockJvm.getHostName()).thenReturn("test-host");
        jvmControlService.createDirectory(mockJvm, "./target");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(JvmControlOperation.CREATE_DIRECTORY), any(WindowsJvmPlatformCommandProvider.class), anyString());
    }
}
