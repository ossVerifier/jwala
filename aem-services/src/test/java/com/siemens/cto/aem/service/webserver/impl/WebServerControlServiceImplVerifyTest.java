package com.siemens.cto.aem.service.webserver.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.domain.model.event.Event;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.state.command.SetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlHistory;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.domain.model.webserver.command.CompleteControlWebServerCommand;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerControlHistoryService;
import com.siemens.cto.aem.service.webserver.WebServerService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WebServerControlServiceImplVerifyTest extends VerificationBehaviorSupport {

    private WebServerControlServiceImpl impl;

    @Mock
    private WebServerService webServerService;

    @Mock
    private WebServerCommandExecutor commandExecutor;

    @Mock
    private WebServerControlHistoryService controlHistoryService;

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
                                               controlHistoryService,
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
        final Identifier<WebServerControlHistory> historyId = mock(Identifier.class);
        final WebServerControlHistory incompleteHistory = mock(WebServerControlHistory.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;

        when(controlCommand.getWebServerId()).thenReturn(webServerId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(webServerService.getWebServer(eq(webServerId))).thenReturn(webServer);
        when(incompleteHistory.getId()).thenReturn(historyId);

        when(controlHistoryService.beginIncompleteControlHistory(matchCommandInEvent(controlCommand))).thenReturn(incompleteHistory);

        impl.controlWebServer(controlCommand,
                              user);

        verify(controlCommand, times(1)).validateCommand();

        verify(controlHistoryService, times(1)).beginIncompleteControlHistory(matchCommandInEvent(controlCommand));
        verify(controlHistoryService, times(1)).completeControlHistory(Matchers.<Event<CompleteControlWebServerCommand>>anyObject());

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
