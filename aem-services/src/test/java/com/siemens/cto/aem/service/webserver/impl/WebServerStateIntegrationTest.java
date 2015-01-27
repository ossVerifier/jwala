package com.siemens.cto.aem.service.webserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.control.webserver.command.impl.WebServerServiceExistenceFacade;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.state.command.WebServerSetStateCommand;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.domain.model.temporary.User;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { WebServerStateIntegrationTest.CommonConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WebServerStateIntegrationTest {

    private static final Integer SUCCESS_PORT = 8080;
    private static final Integer FAILURE_PORT = 8181;
    private static final AtomicReference<StateService<WebServer, WebServerReachableState>> STATE_SERVICE_REFERENCE = new AtomicReference<>();
    private static final AtomicReference<CommandExecutor> COMMAND_EXECUTOR_REFERENCE = new AtomicReference<>();
    private static final AtomicReference<SimpleFuture<ExecData>> COMMAND_EXECUTOR_FUTURE_REFERENCE = new AtomicReference<>();

    @Autowired
    private CommonConfiguration configuration;

    @Autowired
    private SourcePollingChannelAdapter webServerStateInitiator;
    
    @Autowired
    private SourcePollingChannelAdapterFactoryBean webServerPollingChannel;

    @Captor
    private ArgumentCaptor<WebServerSetStateCommand> commandCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConfigWhenServiceExists() throws Exception {

        final StateService<WebServer, WebServerReachableState> service = STATE_SERVICE_REFERENCE.get();
        final ExecData successfulExecData = mockExecData(true);
        COMMAND_EXECUTOR_FUTURE_REFERENCE.get().set(successfulExecData);

        Thread.sleep(5000);

        verify(service, atLeastOnce()).setCurrentState(commandCaptor.capture(),
                                                       Matchers.<User>anyObject());

        final List<WebServerSetStateCommand> allValues = commandCaptor.getAllValues();
        assertFalse(allValues.isEmpty());
        for (final WebServerSetStateCommand command : allValues) {
            final WebServerReachableState expectedState;

            expectedState = WebServerReachableState.UNREACHABLE; // in either case, we already failed the 404

            assertEquals(expectedState,
                         command.getNewState().getState());
        }
    }

    @Test
    public void testConfigWhenServiceDoesNotExist() throws Exception {

        final StateService<WebServer, WebServerReachableState> service = STATE_SERVICE_REFERENCE.get();
        final ExecData successfulExecData = mockExecData(false);
        COMMAND_EXECUTOR_FUTURE_REFERENCE.get().set(successfulExecData);

        Thread.sleep(5000);

        verify(service, atLeastOnce()).setCurrentState(commandCaptor.capture(),
                                                       Matchers.<User>anyObject());

        final List<WebServerSetStateCommand> allValues = commandCaptor.getAllValues();
        assertFalse(allValues.isEmpty());
        for (final WebServerSetStateCommand command : allValues) {
            assertEquals(WebServerReachableState.FAILED,
                         command.getNewState().getState());
        }
    }

    @Before
    public void startHeartbeat() {
        webServerStateInitiator.start();
    }
    
    @Configuration
    @ImportResource("classpath*:META-INF/spring/webserver-heartbeat-integration.xml")
    static class CommonConfiguration {

        @Bean(name = "generalCallerRunsPolicy")
        public static CallerRunsPolicy getCallerRunsPolicy() { 
            return new CallerRunsPolicy();
        }
        
        /**
         * Make toc.properties available to spring integration configuration
         * System properties are only used if there is no setting in toc.properties.
         */
        @Bean(name = "propertyPlaceholderConfigurer")
        public static PropertyPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
            PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("META-INF/spring/toc-defaults.properties", StateService.class.getClassLoader()));
            ppc.setLocalOverride(true);
            ppc.setSearchSystemEnvironment(true);
            ppc.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_FALLBACK);
            return ppc; 
        }
        
        @Bean(name = "webServerServiceExistence")
        public WebServerServiceExistenceFacade getWebServerServiceExistenceFacade() {
            return new WebServerServiceExistenceFacade();
        }

        @Bean(name = "webServerService")
        public WebServerService getWebServerService() {
            final List<WebServer> webServers = createWebServers(4);
            final WebServerService service = mock(WebServerService.class);
            when(service.getWebServers(Matchers.<PaginationParameter>anyObject())).thenReturn(webServers);
            return service;
        }

        @Bean(name = "webServerDao")
        public WebServerDao getWebServerDao() {
            final List<WebServer> webServers = createWebServers(1);
            final WebServerDao dao = mock(WebServerDao.class);
            when(dao.getWebServer(eq(Identifier.<WebServer>id(0L)))).thenReturn(webServers.get(0));
            return dao;
        }

        @Bean
        public ClientHttpRequestFactory webServerHttpRequestFactory() throws Exception {
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

        @Bean
        public com.siemens.cto.aem.common.exception.ExceptionUtil exceptionUtilHelper() {
            return com.siemens.cto.aem.common.exception.ExceptionUtil.INSTANCE;
        }

        @Bean(name = "webServerStateService")
        public StateService<WebServer, WebServerReachableState> getWebServerStateService() {
            final StateService<WebServer, WebServerReachableState> service = mock(StateService.class);
            STATE_SERVICE_REFERENCE.set(service);
            return service;
        }

        @Bean
        public SshConfiguration getSshConfiguration() {
            final SshConfiguration config = mock(SshConfiguration.class);
            return config;
        }

        @Bean
        public JschBuilder getJschBuilder() {
            final JschBuilder builder = mock(JschBuilder.class);
            return builder;
        }

        @Bean
        public CommandExecutor getCommandExecutor() throws CommandFailureException {
            final CommandExecutor executor = mock(CommandExecutor.class);
            COMMAND_EXECUTOR_REFERENCE.set(executor);
            final SimpleFuture<ExecData> future = new SimpleFuture<>();
            COMMAND_EXECUTOR_FUTURE_REFERENCE.set(future);
            doAnswer(new Answer() {
                @Override
                public ExecData answer(final InvocationOnMock invocation) throws Throwable {
                    return future.get();
                }
            }).when(executor).execute(Matchers.<CommandProcessorBuilder>anyObject());
            return executor;
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

        private List<WebServer> createWebServers(final int aNumberToCreate) {
            final List<WebServer> servers = new ArrayList<>(aNumberToCreate);
            for (int i = 1; i <= aNumberToCreate; i++) {
                final Identifier<WebServer> id = new Identifier<>((long) i);
                final Integer port;
                /*if (shouldBeSuccessful(id.getId())) {
                    port = SUCCESS_PORT;
                } else {
                    port = FAILURE_PORT;
                }*/
                port = FAILURE_PORT;
                final WebServer server = new WebServer(id,
                                                       Collections.<Group>emptySet(),
                                                       "unused",
                                                       "hostname" + i + ".example.com",
                                                       port,
                                                       null,
                                                       new Path("/statusPath"),
                                                       new FileSystemPath("d:/some-dir/httpd.conf"),
                                                       new Path("./"),
                                                       new Path("htdocs"));
                servers.add(server);
            }
            return servers;
        }
    }

    private ExecData mockExecData(final boolean wasSuccessful) {
        final ExecData data = mock(ExecData.class);
        final Integer returnCodeValue;
        if (wasSuccessful) {
            returnCodeValue = 0;
        } else {
            returnCodeValue = -1;
        }
        final ExecReturnCode returnCode = new ExecReturnCode(returnCodeValue);
        when(data.getReturnCode()).thenReturn(returnCode);
        return data;
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

    private static class SimpleFuture<V> implements Future<V> {

        private final AtomicReference<V> finalValue;
        private final AtomicBoolean wasSet;
        private final CountDownLatch waiter;

        private SimpleFuture() {
            finalValue = new AtomicReference<>();
            wasSet = new AtomicBoolean(false);
            waiter = new CountDownLatch(1);
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return wasSet.get();
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            while (!isDone()) {
                waiter.await();
            }
            return finalValue.get();
        }

        @Override
        public V get(final long timeout,
                     final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            while (!isDone()) {
                waiter.await(timeout,
                             unit);
            }
            return finalValue.get();
        }

        public void set(final V aFinalValue) {
            if (finalValue.compareAndSet(null,
                                         aFinalValue)) {
                if (wasSet.compareAndSet(false,
                                         true)) {
                    waiter.countDown();
                } else {
                    assert false;
                }
            }
        }
    }
}
