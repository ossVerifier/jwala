package com.cerner.jwala.service.webserver.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.webserver.WebServer;
import com.cerner.jwala.common.domain.model.webserver.WebServerReachableState;
import com.cerner.jwala.common.domain.model.webserver.WebServerState;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.group.GroupStateNotificationService;
import com.cerner.jwala.service.state.InMemoryStateManagerService;
import com.cerner.jwala.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test(s) for WebServerStateSetterWorker.
 *
 * Created by Jedd Cuison on 2/19/2016.
 */
public class WebServerStateSetterWorkerTest {

    private WebServerStateSetterWorker webServerStateSetterWorker;

    @Mock
    private Map mockWebServerReachableStateMap;

    @Mock
    private WebServer mockWebServer;

    @Mock
    private ClientHttpResponse mockClientHttpResponse;

    @Mock
    private ClientHttpRequest mockClientHttpRequest;

    @Mock
    private HttpComponentsClientHttpRequestFactory mockHttpClientRequestFactory;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private MessagingService mockMessagingService;

    @Mock
    private GroupStateNotificationService mockGroupNotificationService;

    @Mock
    private InMemoryStateManagerService mockInMemoryStateManagerService;

    @Mock
    private HttpComponentsClientHttpRequestFactory mockHttpRequestFactory;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        webServerStateSetterWorker = new WebServerStateSetterWorker(mockInMemoryStateManagerService, mockWebServerService,
                mockMessagingService, mockGroupNotificationService, mockHttpRequestFactory);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPingWebServer() throws Exception {
        final URI uri = new URI("any");
        when(mockWebServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(mockWebServer.getStatusUri()).thenReturn(uri);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockHttpRequestFactory.createRequest(eq(uri), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        when(mockClientHttpRequest.execute()).thenReturn(mockClientHttpResponse);
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingService).send(any(WebServerState.class));

        // State did not change so there shouldn't be any updates
        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, new Times(0)).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingService, new Times(0)).send(any(WebServerState.class));

        // State changes so there should be updates
        when(mockClientHttpRequest.execute()).thenThrow(IOException.class);
        when(mockWebServer.getState()).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingService).send(any(WebServerState.class));

        // State did not change but there's an error, db should be updated and notification sent
        reset(mockHttpRequestFactory);
        reset(mockClientHttpRequest);
        when(mockHttpRequestFactory.createRequest(eq(uri), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        when(mockClientHttpRequest.execute()).thenReturn(mockClientHttpResponse);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingService).send(any(WebServerState.class));

        // State did not change but there's an error (but same error), db should'nt be updated and notification is not sent
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, new Times(0)).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingService, new Times(0)).send(any(WebServerState.class));

        // Web server is busy so there shouldn't be any db and notification updates
        when(mockWebServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_STOP_SENT);
        reset(mockWebServerService);
        reset(mockMessagingService);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, new Times(0)).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingService, new Times(0)).send(any(WebServerState.class));
    }

}