package com.cerner.jwala.service.webserver.impl;

import com.cerner.jwala.common.domain.model.group.Group;
import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.domain.model.user.User;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerControlOperation;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.common.request.webserver.ControlWebServerRequest;
import com.cerner.jwala.control.command.PlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.persistence.jpa.type.EventType;
import com.cerner.jwala.service.*;
import com.cerner.jwala.service.host.HostService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.File;
import java.util.HashSet;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private WebServerControlServiceImpl webServerControlService;


    @Mock
    private HostService hostService;

    @Mock
    private WebServerService webServerService;

    @Mock
    private RemoteCommandExecutor<WebServerControlOperation> commandExecutor;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private HistoryFacadeService mockHistoryFacadeService;

    @Mock
    RemoteCommandExecutorService remoteCommandExecutorService;

    @Mock
    private SshConfiguration mockSshConfig;

    private User user;

    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        webServerControlService = new WebServerControlServiceImpl(webServerService, commandExecutor,
                remoteCommandExecutorService, mockSshConfig, mockHistoryFacadeService);
        webServerControlService.setHostService(hostService);
        when(hostService.getUName(anyString())).thenReturn(HostService.UNAME_CYGWIN);
        user = new User("unused");
    }

    @Test
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        String wsName = "mockWebServerName";
        String wsHostName = "mockWebServerHost";
        final ControlWebServerRequest controlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer webServer = mock(WebServer.class);

        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(webServer);
        when(webServer.getName()).thenReturn(wsName);
        when(webServer.getHost()).thenReturn(wsHostName);
        when(webServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);

        final Identifier<WebServer> webServerId = mock(Identifier.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;
        final ClientHttpResponse mockClientHttpResponse = mock(ClientHttpResponse.class);

        when(controlWebServerRequest.getWebServerId()).thenReturn(webServerId);
        when(controlWebServerRequest.getControlOperation()).thenReturn(controlOperation);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Start succeeded", ""));

        webServerControlService.controlWebServer(controlWebServerRequest, user);

        verify(remoteCommandExecutorService, times(1)).executeCommand(any(RemoteExecCommand.class));
    }

    @Test
    @Ignore
    // TODO: Fix this!
    public void testStart() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(webserver);
        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "SUCCEEDED", ""));
        ControlWebServerRequest controlWSRequest = new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.START);
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(mockMessagingService).send(any(CurrentState.class));

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_PROCESS_KILLED, "", "PROCESS KILLED"));
        CommandOutput returnOutput = webServerControlService.controlWebServer(controlWSRequest, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());
        verify(webServerService).updateState(any(Identifier.class), eq(WebServerReachableState.FORCED_STOPPED), eq(""));
        verify(mockMessagingService, times(2)).send(any(CurrentState.class));
        reset(mockMessagingService);

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(ExecReturnCode.JWALA_EXIT_CODE_ABNORMAL_SUCCESS, "", "ABNORMAL SUCCESS"));
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(mockMessagingService, times(2)).send(any(CurrentState.class));
        reset(mockMessagingService);

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(1, "", "ABNORMAL SUCCESS"));
        webServerControlService.controlWebServer(controlWSRequest, user);
        verify(mockHistoryFacadeService, times(2)).write(anyString(), anyList(), anyString(), eq(EventType.SYSTEM_ERROR), anyString());
        verify(mockMessagingService, times(2)).send(any(CurrentState.class));
        reset(mockMessagingService);

        when(remoteCommandExecutorService.executeCommand(any(RemoteExecCommand.class))).thenReturn(new RemoteCommandReturnInfo(0, "Delete service succeeded", ""));
        webServerControlService.controlWebServer(new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.DELETE_SERVICE), user);
        verify(mockMessagingService).send(any(CurrentState.class));
    }

    @Test
    public void testSecureCopy() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        CommandOutput successReturnOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successReturnOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Directory Created", ""));
        CommandOutput returnOutput = webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
        assertEquals(new ExecReturnCode(0), returnOutput.getReturnCode());
    }

    @Test
    public void testSecureCopyPerformsBackup() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File does exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.BACK_UP), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Back up succeeded", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Directory Created", ""));
        webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.SCP), any(WindowsWebServerPlatformCommandProvider.class), anyString(), anyString());
    }

    @Test (expected = InternalErrorException.class)
    public void testSecureCopyFailsBackup() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Secure copy succeeded", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "File does exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.BACK_UP), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "", "Back up failed"));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Directory Created", ""));
        webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("test-ws");
        when(mockWebServer.getHost()).thenReturn("test-host");
        webServerControlService.changeFileMode(mockWebServer, "777", "./target", "*");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHANGE_FILE_MODE), any(WindowsWebServerPlatformCommandProvider.class), anyString(), anyString(), anyString());
    }

    @Test
    public void testCreateDirectory() throws CommandFailureException {
        WebServer mockWebServer = mock(WebServer.class);
        when(mockWebServer.getName()).thenReturn("test-ws");
        when(mockWebServer.getHost()).thenReturn("test-host");
        webServerControlService.createDirectory(mockWebServer, "./target");
        verify(commandExecutor).executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(WindowsWebServerPlatformCommandProvider.class), anyString());
    }

    @Test
    public void testSecureCopyHomeDir() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        CommandOutput successReturnOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successReturnOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "Directory Created", ""));
        CommandOutput returnOutput = webServerControlService.secureCopyFile("testWebServer", "./source", "~/dest", "user-id");
        assertEquals(new ExecReturnCode(0), returnOutput.getReturnCode());
    }

    @Test (expected = InternalErrorException.class)
    public void testSecureCopyCreateParentFail() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        CommandOutput successReturnOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESS", "");
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successReturnOutput);
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CHECK_FILE_EXISTS), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(1), "File does not exist", ""));
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), eq(WebServerControlOperation.CREATE_DIRECTORY), any(PlatformCommandProvider.class), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(2), "Failed to create directory", ""));
        webServerControlService.secureCopyFile("testWebServer", "./source", "./dest", "user-id");
    }

    @Test
    public void testWaitForState() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.START);
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 120L);
        assertTrue(result);
    }

    @Test
    public void testWaitForStateFail() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.STOP);
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_REACHABLE);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 5L);
        assertFalse(result);
    }

    @Test
    public void testWaitStateForStop() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.STOP);
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 5L);
        assertTrue(result);
    }

    @Test
    public void testWaitStateForForcedStop() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.STOP);
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.FORCED_STOPPED);
        boolean result = webServerControlService.waitForState(mockControlWebServerRequest, 5L);
        assertTrue(result);
    }

    @Test (expected = InternalErrorException.class)
    public void testWaitStateForUnexpectedOperation() {
        final ControlWebServerRequest mockControlWebServerRequest = mock(ControlWebServerRequest.class);
        final WebServer mockWebServer = mock(WebServer.class);
        when(mockControlWebServerRequest.getControlOperation()).thenReturn(WebServerControlOperation.BACK_UP);
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(mockWebServer);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.FORCED_STOPPED);
        webServerControlService.waitForState(mockControlWebServerRequest, 5L);
    }
}