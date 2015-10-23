package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.WebServerStateRetrievalScheduledTaskHandler;
import com.siemens.cto.aem.service.webserver.component.WebServerStateSetterWorker;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link com.siemens.cto.aem.service.webserver.WebServerStateRetrievalScheduledTaskHandler}.
 *
 * Created by Z003BPEJ on 6/30/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AsyncWebServerStateRetrievalScheduledTaskHandlerTest.Config.class})
public class AsyncWebServerStateRetrievalScheduledTaskHandlerTest {

    @Autowired
    @Qualifier("webServerStateRetrievalScheduledTaskHandlerBean")
    private WebServerStateRetrievalScheduledTaskHandler webServerStateRetrievalScheduledTaskHandler;

    private WebServer webServer1;
    private WebServer webServer2;
    private List<WebServer> webServers;

    @Mock
    private ClientHttpRequest request;

    @Mock
    private ClientHttpResponse clientHttpResponse;

    // We need this to prevent concurrency modification exception when checking for Futures in the map.
    private Set<Identifier<WebServer>> keys = new HashSet<>();

    final private static long TIMEOUT = 300000;

    @Before
    public void setup() throws IOException {
        webServer1 = new WebServer(new Identifier<WebServer>(1L),
                new ArrayList<Group>(),
                null,
                "localhost",
                80,
                null,
                new Path("/stp.png"),
                null,
                null,
                null);

        webServer2 = new WebServer(new Identifier<WebServer>(2L),
                new ArrayList<Group>(),
                null,
                "localhost",
                90,
                null,
                new Path("/stp.png"),
                null,
                null,
                null);

        webServers = new ArrayList<>();
        webServers.add(webServer1);
        webServers.add(webServer2);

        MockitoAnnotations.initMocks(this);
        reset(Config.webServerService);

        Config.webServerFutureMap.clear();

        keys.clear();
        for (final WebServer webServer : webServers) {
            keys.add(webServer.getId());
        }
    }

    @Test
    public void testWebServerStatePollerTaskExecuteHttpStatusOk() throws IOException, InterruptedException {
        when(Config.webServerService.getWebServers()).thenReturn(webServers);
        when(Config.webServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        try {
            assertEquals(0, Config.webServerFutureMap.size());
            webServerStateRetrievalScheduledTaskHandler.setEnabled(true);
            waitForAsyncThreadsToComplete();
        } finally {
            webServerStateRetrievalScheduledTaskHandler.setEnabled(false);
        }

        verify(Config.webServerService, times(1)).getWebServers();
        verify(request, times(2)).execute();
        verify(clientHttpResponse, times(2)).close();

        assertEquals(2, Config.webServerFutureMap.size());
    }

    @Test
    public void testWebServerStatePollerTaskExecuteHttpStatusNotFound() throws IOException, InterruptedException {
        when(Config.webServerService.getWebServers()).thenReturn(webServers);
        when(Config.webServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

        try {
            assertEquals(0, Config.webServerFutureMap.size());
            webServerStateRetrievalScheduledTaskHandler.setEnabled(true);
            waitForAsyncThreadsToComplete();
        } finally {
            webServerStateRetrievalScheduledTaskHandler.setEnabled(false);
        }

        verify(Config.webServerService, times(1)).getWebServers();
        verify(request, atMost(2)).execute();
        verify(clientHttpResponse, times(2)).close();

        assertEquals(2, Config.webServerFutureMap.size());
    }

    @Test
    public void testWebServerStatePollerTaskExecuteIoException() throws IOException, InterruptedException {
        when(Config.webServerService.getWebServers()).thenReturn(webServers);
        when(Config.webServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenThrow(new IOException());

        try {
            assertEquals(0, Config.webServerFutureMap.size());
            webServerStateRetrievalScheduledTaskHandler.setEnabled(true);
            waitForAsyncThreadsToComplete();
        } finally {
            webServerStateRetrievalScheduledTaskHandler.setEnabled(false);
        }

        verify(Config.webServerService, times(1)).getWebServers();
        verify(request, times(0)).execute();
        verify(clientHttpResponse, times(0)).close();

        assertEquals(2, Config.webServerFutureMap.size());
    }

    @Test
    @Ignore
    // TODO: Fix the problem wherein this test Intermittently fails.
    public void testWebServerStatePollerTaskExecuteHttpStatusOkWithCleanup() throws IOException, InterruptedException {
        when(Config.webServerService.getWebServers()).thenReturn(webServers);
        when(Config.webServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);

        try {
            Config.webServerFutureMap.put(new Identifier<WebServer>(999L), null);
            assertEquals(1, Config.webServerFutureMap.size());
            webServerStateRetrievalScheduledTaskHandler.setEnabled(true);
            waitForAsyncThreadsToComplete();

            final long startTime = System.currentTimeMillis();
            while (Config.webServerFutureMap.size() != 2) {
                if ((System.currentTimeMillis() - startTime) > TIMEOUT) {
                    fail("Timeout of 5 minute was reached while waiting for scheduler thread to cleanup futures map!");
                    break;
                }
            }

        } finally {
            webServerStateRetrievalScheduledTaskHandler.setEnabled(false);
        }

        verify(Config.webServerService, times(1)).getWebServers();
        verify(request, atMost(2)).execute();

        // Note: If response is null. close will not be executed!
        // TODO: Find a way to test the line below correctly!
        // verify(clientHttpResponse, times(2)).close();

        assertEquals(2, Config.webServerFutureMap.size());

        for (Identifier<WebServer> key : Config.webServerFutureMap.keySet()) {
            assertNotEquals(999L, key.getId().longValue());
        }
    }

    @Test
    public void testWebServerStatePollerTaskExecuteThrowRuntimeException() throws IOException, InterruptedException {
        when(Config.webServerService.getWebServers()).thenReturn(webServers);
        when(Config.webServerReachableStateMap.get(any(Identifier.class))).thenReturn(WebServerReachableState.WS_REACHABLE);
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenThrow(UnsupportedOperationException.class);

        try {
            assertEquals(0, Config.webServerFutureMap.size());
            webServerStateRetrievalScheduledTaskHandler.setEnabled(true);
            waitForAsyncThreadsToComplete();
        } finally {
            webServerStateRetrievalScheduledTaskHandler.setEnabled(false);
        }

        verify(Config.webServerService, times(1)).getWebServers();
        verify(request, times(2)).execute();
        verify(clientHttpResponse, times(0)).close();

        assertEquals(2, Config.webServerFutureMap.size());
    }

    private void waitForAsyncThreadsToComplete() {
        boolean done = false;
        final long startTime = System.currentTimeMillis();
        while (!done) {
            for (Identifier<WebServer> key : keys) {
                if (Config.webServerFutureMap.containsKey(key)) {
                    done = Config.webServerFutureMap.get(key).isDone();
                    if (!done) {
                        break;
                    }
                }
            }

            if ((System.currentTimeMillis() - startTime) > TIMEOUT) {
                fail("Timeout of 5 minutes was reached while waiting for asynchronous threads to complete!");
                break;
            }
        }
    }

    @Configuration
    @EnableScheduling
    @EnableAsync
    @ComponentScan("com.siemens.cto.aem.service.webserver.component")
    static class Config {

        @Mock
        private static WebServerService webServerService;

        @Mock
        private static HttpClientRequestFactory httpClientRequestFactory;

        @Mock
        private static Map<Identifier<WebServer>, WebServerReachableState> webServerReachableStateMap;

        @Mock
        private static StateService<WebServer, WebServerReachableState> webServerStateService;

        @Autowired
        private WebServerStateSetterWorker webServerStateSetterWorker;

        private static volatile Map<Identifier<WebServer>, Future<?>> webServerFutureMap = new HashMap<>();

        public Config() {
            MockitoAnnotations.initMocks(this);
        }

        @Bean(name = "propConfig")
        public static PropertySourcesPlaceholderConfigurer configurer() {
            PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
            Properties props = new Properties();
            props.setProperty("ping.webServer.period.millis",  "3000");
            ppc.setProperties(props);
            return ppc;
        }

        @Bean(name = "webServerStateRetrievalScheduledTaskHandlerBean")
        @Autowired
        @DependsOn("propConfig")
        WebServerStateRetrievalScheduledTaskHandler getWebServerStateRetrievalScheduledTaskHandler() {
            webServerStateSetterWorker.setWebServerReachableStateMap(webServerReachableStateMap);
            webServerStateSetterWorker.setWebServerStateService(webServerStateService);
            return new WebServerStateRetrievalScheduledTaskHandler(webServerService,
                                                                   webServerStateSetterWorker,
                                                                   webServerFutureMap);
        }

        @Bean(name = "webServerTaskExecutor")
        public TaskExecutor getWebServerTaskExecutor() {
            final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
            threadPoolTaskExecutor.setCorePoolSize(5);
            threadPoolTaskExecutor.setMaxPoolSize(5);
            threadPoolTaskExecutor.setQueueCapacity(100);
            threadPoolTaskExecutor.setKeepAliveSeconds(5);
            threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

            final CustomizableThreadFactory tf = new CustomizableThreadFactory("polling-");
            tf.setDaemon(true);
            threadPoolTaskExecutor.setThreadFactory(tf);
            return threadPoolTaskExecutor;
        }

        @Bean(name="webServerHttpRequestFactory")
        public HttpClientRequestFactory getHttpClientRequestFactory(){
            return httpClientRequestFactory;
        }

    }

}
