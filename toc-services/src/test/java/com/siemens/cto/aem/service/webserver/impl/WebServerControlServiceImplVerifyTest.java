package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.command.PlatformCommandProvider;
import com.siemens.cto.aem.control.command.RemoteCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.MessagingService;
import com.siemens.cto.aem.service.RemoteCommandExecutorService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.state.InMemoryStateManagerService;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private WebServerControlServiceImpl webServerControlService;

    @Mock
    private WebServerService webServerService;

    @Mock
    private RemoteCommandExecutor<WebServerControlOperation> commandExecutor;

    @Mock
    private InMemoryStateManagerService<Identifier<WebServer>, WebServerReachableState> mockInMemoryStateManagerService;

    @Captor
    private ArgumentCaptor<SetStateRequest<WebServer, WebServerReachableState>> setStateCommandCaptor;

    @Mock
    private HistoryService mockHistoryService;

    @Mock
    private StateNotificationService stateNotificationService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    RemoteCommandExecutorService remoteCommandExecutorService;

    @Mock
    private SshConfiguration mockSshConfig;

    private User user;

    @Before
    public void setup() {
        webServerControlService = new WebServerControlServiceImpl(webServerService, commandExecutor, mockInMemoryStateManagerService,
                mockHistoryService, mockMessagingService, remoteCommandExecutorService, mockSshConfig);

        user = new User("unused");
    }

    @Test
    @SuppressWarnings("unchecked")
    @Ignore
    // TODO: Fix this!
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

        webServerControlService.controlWebServer(controlWebServerRequest, user);

        verify(controlWebServerRequest, times(1)).validate();

        verify(commandExecutor, times(1)).executeRemoteCommand(eq(wsName),
                eq(wsHostName),
                eq(WebServerControlOperation.START),
                any(WindowsWebServerPlatformCommandProvider.class)
        );

        verify(webServerService).updateState(any(Identifier.class), eq(WebServerReachableState.WS_UNREACHABLE), anyString());

        final SetStateRequest<WebServer, WebServerReachableState> actualSetStateCommand = setStateCommandCaptor.getValue();
        assertEquals(webServerId, actualSetStateCommand.getNewState().getId());
        assertEquals(controlOperation.getOperationState(), actualSetStateCommand.getNewState().getState());

        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
    }

    @Test
    @Ignore
    // TODO: Fix this!
    public void testStart() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(any(Identifier.class))).thenReturn(webserver);
        ExecReturnCode execReturnCode = new ExecReturnCode(0);
        CommandOutput commandOutput = new CommandOutput(execReturnCode, "SUCCESS", "");
        when(commandExecutor.executeRemoteCommand(anyString(),anyString(),any(WebServerControlOperation.class),any(PlatformCommandProvider.class))).thenReturn(commandOutput);
        ControlWebServerRequest controlWSRequest = new ControlWebServerRequest(webServerIdentifier, WebServerControlOperation.START);
        webServerControlService.controlWebServer(controlWSRequest, user);

        CommandOutput commandOutputProcessKilled = new CommandOutput(new ExecReturnCode(255), "", "PROCESS KILLED");
        when(commandExecutor.executeRemoteCommand(anyString(),anyString(),any(WebServerControlOperation.class),any(PlatformCommandProvider.class))).thenReturn(commandOutputProcessKilled);
        CommandOutput returnOutput = webServerControlService.controlWebServer(controlWSRequest, user);
        assertEquals("FORCED STOPPED", returnOutput.getStandardOutput());

        CommandOutput commandOutputProcessFailed = new CommandOutput(new ExecReturnCode(1), "", "TEST FAILED");
        when(commandExecutor.executeRemoteCommand(anyString(),anyString(),any(WebServerControlOperation.class),any(PlatformCommandProvider.class))).thenReturn(commandOutputProcessFailed);
        webServerControlService.controlWebServer(controlWSRequest, user);

        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class))).thenThrow(new CommandFailureException(new ExecCommand("Failed exec"), new Throwable("Failed remote command")));
        try {
            webServerControlService.controlWebServer(controlWSRequest, user);
        } catch (InternalErrorException iee){
            assertTrue(iee.getMessageResponseStatus().equals(AemFaultType.REMOTE_COMMAND_FAILURE));
        }
    }

    @Test
    public void testSecureCopy() throws CommandFailureException {
        final Identifier<WebServer> webServerIdentifier = new Identifier<>(12L);
        WebServer webserver = new WebServer(webServerIdentifier, new HashSet<Group>(), "testWebServer");
        when(webServerService.getWebServer(anyString())).thenReturn(webserver);

        CommandOutput successReturnOutput = new CommandOutput(new ExecReturnCode(0), "SUCCESSS", "");
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(successReturnOutput);
        CommandOutput returnOutput = webServerControlService.secureCopyFileWithBackup("testWebServer", "./source", "./dest", true);
        assertEquals(new ExecReturnCode(0), returnOutput.getReturnCode());

        CommandOutput failedReturnOutput = new CommandOutput(new ExecReturnCode(1), "FAILED", "");
        when(commandExecutor.executeRemoteCommand(anyString(), anyString(), any(WebServerControlOperation.class), any(PlatformCommandProvider.class), anyString(), anyString())).thenReturn(failedReturnOutput);
        try {
            webServerControlService.secureCopyFileWithBackup("testWebServer", "./source", "./dest", true);
        } catch(InternalErrorException ie) {
            assertEquals(AemFaultType.REMOTE_COMMAND_FAILURE, ie.getMessageResponseStatus());
        }
    }
}
