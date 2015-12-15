package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
import com.siemens.cto.aem.common.request.state.SetStateRequest;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.HistoryService;
import com.siemens.cto.aem.service.VerificationBehaviorSupport;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.net.URI;
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
    private ArgumentCaptor<SetStateRequest<WebServer, WebServerReachableState>> setStateCommandCaptor;

    @Mock
    private HistoryService mockHistoryService;

    private User user;

    @Mock
    private ClientFactoryHelper mockClientFactory;

    @Before
    public void setup() {
        impl = new WebServerControlServiceImpl(webServerService,
                                               commandExecutor,
                                               webServerStateService,
                                               webServerReachableStateMap,
                mockHistoryService,
                mockClientFactory);

        user = new User("unused");
        when(webServerService.getJpaWebServer(anyLong(), eq(true))).thenReturn(new JpaWebServer());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testVerificationOfBehaviorForSuccess() throws Exception {
        final ControlWebServerRequest controlCommand = mock(ControlWebServerRequest.class);
        final WebServer webServer = mock(WebServer.class);
        final Identifier<WebServer> webServerId = mock(Identifier.class);
        final WebServerControlOperation controlOperation = WebServerControlOperation.START;
        final ClientHttpResponse mockClientHttpResponse = mock(ClientHttpResponse.class);

        when(controlCommand.getWebServerId()).thenReturn(webServerId);
        when(controlCommand.getControlOperation()).thenReturn(controlOperation);
        when(webServerService.getWebServer(eq(webServerId))).thenReturn(webServer);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(mockClientFactory.requestGet(any(URI.class))).thenReturn(mockClientHttpResponse);

        impl.controlWebServer(controlCommand,
                              user);

        verify(controlCommand, times(1)).validate();

        verify(webServerService, times(1)).getWebServer(eq(webServerId));
        verify(commandExecutor, times(1)).controlWebServer(eq(controlCommand),
                                                           eq(webServer));
        verify(webServerStateService, times(1)).setCurrentState(setStateCommandCaptor.capture(),
                                                                eq(user));

        final SetStateRequest<WebServer, WebServerReachableState> actualSetStateCommand = setStateCommandCaptor.getValue();
        assertEquals(webServerId,
                     actualSetStateCommand.getNewState().getId());
        assertEquals(controlOperation.getOperationState(),
                     actualSetStateCommand.getNewState().getState());

        verify(mockHistoryService).createHistory(anyString(), anyList(), anyString(), any(EventType.class), anyString());
    }
}
