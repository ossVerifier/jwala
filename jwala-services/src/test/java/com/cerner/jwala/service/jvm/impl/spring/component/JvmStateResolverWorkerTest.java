package com.cerner.jwala.service.jvm.impl.spring.component;

import com.cerner.jwala.common.domain.model.id.Identifier;
import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.jvm.JvmState;
import com.cerner.jwala.common.domain.model.state.CurrentState;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.service.HistoryService;
import com.cerner.jwala.service.MessagingService;
import com.cerner.jwala.service.RemoteCommandReturnInfo;
import com.cerner.jwala.service.jvm.JvmStateService;
import com.cerner.jwala.service.ssl.hc.HttpClientRequestFactory;
import com.cerner.jwala.service.webserver.component.ClientFactoryHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Test for {@link JvmStateResolverWorker}.
 *
 * Created by JC043760 on 4/18/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {JvmStateResolverWorkerTest.Config.class})
public class JvmStateResolverWorkerTest {

    @Autowired
    private JvmStateResolverWorker jvmStateResolverWorker;

    @Mock
    private Jvm mockJvm;

    @Mock
    private JvmStateService mockJvmStateService;

    @Mock
    private ClientHttpResponse mockResponse;

    @BeforeClass
    public static void init() {
        // Prevents Failed to load properties file null/vars.properties
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH,
                JvmStateResolverWorkerTest.class.getClassLoader().getResource("vars.properties").getPath().replace("vars.properties", ""));
    }

    @AfterClass
    public static void destroy() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Before
    public void setup() {
        initMocks(this);
        reset(Config.mockClientFactoryHelper, Config.mockHistoryService, Config.mockHttpClientRequestFactory, Config.mockMessagingService);
    }

    @Test
    public void testPingAndUpdateJvmStateNew() throws ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_NEW);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_NEW, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusOk() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.OK);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusNotOk() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusNotOkAndRetCodeNotZero() throws ExecutionException, InterruptedException, IOException {
        when(mockJvm.getId()).thenReturn(new Identifier<Jvm>(1L));
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenReturn(mockResponse);
        when(mockResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_STARTED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusWithIoE() throws IOException, ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new IOException());
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertEquals(JvmState.JVM_STOPPED, future.get().getState());
    }

    @Test
    public void testPingAndUpdateJvmStateHttpStatusWithRuntimeException() throws IOException, ExecutionException, InterruptedException {
        when(mockJvm.getState()).thenReturn(JvmState.JVM_STOPPED);
        when(Config.mockClientFactoryHelper.requestGet(any(URI.class))).thenThrow(new RuntimeException());
        final RemoteCommandReturnInfo remoteCommandReturnInfo = new RemoteCommandReturnInfo(-1, "STOPPED", "");
        when(mockJvmStateService.getServiceStatus(eq(mockJvm))).thenReturn(remoteCommandReturnInfo);
        Future<CurrentState<Jvm, JvmState>> future = jvmStateResolverWorker.pingAndUpdateJvmState(mockJvm, mockJvmStateService);
        assertNull(future.get());
    }

    @Configuration
    static class Config {

        static HttpClientRequestFactory mockHttpClientRequestFactory = mock(HttpClientRequestFactory.class);

        static ClientFactoryHelper mockClientFactoryHelper = mock(ClientFactoryHelper.class);

        static HistoryService mockHistoryService = mock(HistoryService.class);

        static MessagingService mockMessagingService = mock(MessagingService.class);

        @Bean(name = "webServerHttpRequestFactory")
        public HttpClientRequestFactory getMockHttpClientRequestFactory() {
            return mockHttpClientRequestFactory;
        }

        @Bean
        public ClientFactoryHelper getMockClientFactoryHelper() {
            return mockClientFactoryHelper;
        }

        @Bean
        public HistoryService getMockHistoryService() {
            return mockHistoryService;
        }

        @Bean
        public MessagingService getMockMessagingService() {
            return mockMessagingService;
        }

        @Bean
        public JvmStateResolverWorker getJvmStateResolverWorker() {
            return new JvmStateResolverWorker();
        }

    }


}
