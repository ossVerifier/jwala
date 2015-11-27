package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.command.state.SetStateCommand;
import com.siemens.cto.aem.domain.model.user.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.domain.command.webserver.ControlWebServerCommand;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private WebServerControlServiceImpl impl;

    @Mock
    private WebServerService webServerService;

    @Mock
    private WebServerCommandExecutor commandExecutor;

    @Mock
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    @Mock
    private Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;

    @Captor
    private ArgumentCaptor<SetStateCommand<WebServer, WebServerReachableState>> setStateCommandCaptor;

    private User user;

    @Before
    public void setup() {
        impl = new WebServerControlServiceImpl(webServerService,
                                               commandExecutor,
                                               webServerStateService,
                                               webServerReachableStateMap);

        user = new User("unused");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlWebServerCommand controlCommand = mock(ControlWebServerCommand.class);
        final WebServer webServer = mock(WebServer.class);
        final Identifier<WebServer> webServerId = mock(Identifier.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;

        when(controlCommand.getWebServerId()).thenReturn(webServerId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(webServerService.getWebServer(eq(webServerId))).thenReturn(webServer);

        impl.controlWebServer(controlCommand,
                              user);

        verify(controlCommand, times(1)).validateCommand();

        verify(webServerService, times(1)).getWebServer(eq(webServerId));
        verify(commandExecutor, times(1)).controlWebServer(eq(controlCommand),
                                                           eq(webServer));
        verify(webServerStateService, times(1)).setCurrentState(setStateCommandCaptor.capture(),
                                                                eq(user));

        final SetStateCommand<WebServer, WebServerReachableState> actualSetStateCommand = setStateCommandCaptor.getValue();
        assertEquals(webServerId,
                     actualSetStateCommand.getNewState().getId());
        assertEquals(controlOperation.getOperationState(),
                     actualSetStateCommand.getNewState().getState());
    }
}
