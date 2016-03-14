package com.siemens.cto.aem.service.webserver.component;

import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerState;
import com.siemens.cto.aem.service.group.GroupStateNotificationService;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.webserver.WebServerService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;

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
 * Created by JC043760 on 2/19/2016.
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
    private ClientFactoryHelper mockClientFactoryHelper;

    @Mock
    private HttpClientRequestFactory mockHttpClientRequestFactory;

    @Mock
    private WebServerService mockWebServerService;

    @Mock
    private SimpMessagingTemplate mockMessagingTemplate;

    @Mock
    private GroupStateNotificationService mockGroupNotificationService;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        webServerStateSetterWorker = new WebServerStateSetterWorker();
        webServerStateSetterWorker.clientFactoryHelper = mockClientFactoryHelper;
        webServerStateSetterWorker.setWebServerReachableStateMap(mockWebServerReachableStateMap);
        webServerStateSetterWorker.setWebServerService(mockWebServerService);
        webServerStateSetterWorker.setMessagingTemplate(mockMessagingTemplate);
        webServerStateSetterWorker.setGroupStateNotificationService(mockGroupNotificationService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPingWebServer() throws Exception {
        final URI uri = new URI("any");
        when(mockWebServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_UNREACHABLE);
        when(mockWebServer.getStatusUri()).thenReturn(uri);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockClientHttpResponse);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        when(mockHttpClientRequestFactory.createRequest(eq(uri), eq(HttpMethod.GET))).thenReturn(mockClientHttpRequest);
        when(mockWebServer.getId()).thenReturn(new Identifier<WebServer>(1L));
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingTemplate).convertAndSend (anyString(), any(WebServerState.class));

        // State did not change so there shouldn't be any updates
        reset(mockWebServerService);
        reset(mockMessagingTemplate);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, new Times(0)).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingTemplate, new Times(0)).convertAndSend (anyString(), any(WebServerState.class));

        // State changes so there should be updates
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(IOException.class);
        reset(mockWebServerService);
        reset(mockMessagingTemplate);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingTemplate).convertAndSend (anyString(), any(WebServerState.class));

        // State did not change but there's an error, db should be updated and notification sent
        reset(mockClientFactoryHelper);
        when(mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockClientHttpResponse);
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        reset(mockWebServerService);
        reset(mockMessagingTemplate);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingTemplate).convertAndSend (anyString(), any(WebServerState.class));

        // State did not change but there's an error (but same error), db should'nt be updated and notification is not sent
        when(mockClientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NO_CONTENT);
        reset(mockWebServerService);
        reset(mockMessagingTemplate);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, new Times(0)).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingTemplate, new Times(0)).convertAndSend (anyString(), any(WebServerState.class));

        // Web server is busy so there shouldn't be any db and notification updates
        when(mockWebServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_STOP_SENT);
        reset(mockWebServerService);
        reset(mockMessagingTemplate);
        webServerStateSetterWorker.pingWebServer(mockWebServer);
        verify(mockWebServerService, new Times(0)).updateState(any(Identifier.class), any(WebServerReachableState.class), anyString());
        verify(mockMessagingTemplate, new Times(0)).convertAndSend (anyString(), any(WebServerState.class));
    }

}