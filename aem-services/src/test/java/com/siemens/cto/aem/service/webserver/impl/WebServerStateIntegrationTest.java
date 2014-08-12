package com.siemens.cto.aem.service.webserver.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { WebServerStateIntegrationTest.CommonConfiguration.class })
public class WebServerStateIntegrationTest {

    private static final Integer SUCCESS_PORT = 8080;
    private static final Integer FAILURE_PORT = 8181;

    @Autowired
    private CommonConfiguration configuration;

    @Captor
    private ArgumentCaptor<WebServerSetStateCommand> commandCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConfig() throws Exception {
        Thread.sleep(5000);

        final StateService<WebServer, WebServerReachableState> service = configuration.getActualMockService();

        verify(service, atLeastOnce()).setCurrentState(commandCaptor.capture(),
                                                       Matchers.<User>anyObject());

        final List<WebServerSetStateCommand> allValues = commandCaptor.getAllValues();
        assertFalse(allValues.isEmpty());
        for (final WebServerSetStateCommand command : allValues) {
            final WebServerReachableState expectedState;
            if (shouldBeSuccessful(command.getNewState().getId().getId())) {
                expectedState = WebServerReachableState.REACHABLE;
            } else {
                expectedState = WebServerReachableState.UNREACHABLE;
            }
            assertEquals(expectedState,
                         command.getNewState().getState());
        }
    }

    @Configuration
    @ImportResource("classpath*:META-INF/spring/webserver-heartbeat-integration.xml")
    static class CommonConfiguration {

        private StateService<WebServer, WebServerReachableState> mockService;

        @Bean(name = "webServerService")
        public WebServerService getWebServerService() {
            final List<WebServer> webServers = createWebServers(3);
            final WebServerService service = mock(WebServerService.class);
            when(service.getWebServers(Matchers.<PaginationParameter>anyObject())).thenReturn(webServers);
            return service;
        }

        @Bean
        public ClientHttpRequestFactory httpRequestFactory() throws Exception {
            final ClientHttpRequestFactory factory = mock(ClientHttpRequestFactory.class);
            final ClientHttpResponse successResponse = mockResponse(true);
            final ClientHttpResponse failureResponse = mockResponse(false);
            final ClientHttpRequest successRequest = mockRequest(successResponse);
            final ClientHttpRequest failureRequest = mockRequest(failureResponse);

            when(factory.createRequest(isUri(SUCCESS_PORT),
                                       eq(HttpMethod.GET))).thenReturn(successRequest);
            when(factory.createRequest(isUri(FAILURE_PORT),
                                       eq(HttpMethod.GET))).thenReturn(failureRequest);
            return factory;
        }

        @Bean(name = "webServerStateService")
        public StateService<WebServer, WebServerReachableState> getWebServerStateService() {
            //Not sure if this will work...
            final StateService<WebServer, WebServerReachableState> service = mock(StateService.class);
            mockService = service;
            return service;
        }

        private ClientHttpRequest mockRequest(final ClientHttpResponse aResponse) throws IOException {
            final MockClientHttpRequest request = new MockClientHttpRequest();
            request.setResponse(aResponse);
            return request;
        }

        private ClientHttpResponse mockResponse(final boolean shouldBeSuccessful) throws IOException {
            final HttpStatus status;
            if (shouldBeSuccessful) {
                status = HttpStatus.OK;
            } else {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            final MockClientHttpResponse response = new MockClientHttpResponse(new byte[0],
                                                                               status);
            return response;
        }

        public StateService<WebServer, WebServerReachableState> getActualMockService() {
            return mockService;
        }

        private List<WebServer> createWebServers(final int aNumberToCreate) {
            final List<WebServer> servers = new ArrayList<>(aNumberToCreate);
            for (int i = 1; i <= aNumberToCreate; i++) {
                final Identifier<WebServer> id = new Identifier<>((long) i);
                final Integer port;
                if (shouldBeSuccessful(id.getId())) {
                    port = SUCCESS_PORT;
                } else {
                    port = FAILURE_PORT;
                }
                final WebServer server = new WebServer(id,
                                                       Collections.<Group>emptySet(),
                                                       "unused",
                                                       "hostname" + i + ".example.com",
                                                       port,
                                                       null,
                                                       new Path("/statusPath"));
                servers.add(server);
            }
            return servers;
        }
    }

    static class UriPortMatcher extends ArgumentMatcher<URI> {

        private final Integer port;

        UriPortMatcher(final Integer thePort) {
            port = thePort;
        }

        @Override
        public boolean matches(final Object item) {
            if (! (item instanceof URI)) {
                return false;
            }

            final URI other = (URI)item;

            return port.equals(other.getPort());
        }
    }

    private static boolean shouldBeSuccessful(final Long anId) {
        return (anId % 2) == 0;
    }

    private static URI isUri(final Integer aPort) {
        return argThat(new UriPortMatcher(aPort));
    }
}
