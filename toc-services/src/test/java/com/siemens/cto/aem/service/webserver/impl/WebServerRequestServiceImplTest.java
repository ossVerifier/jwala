package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RuntimeCommand;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.ssl.hc.HttpClientRequestFactory;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link WebServerCommandServiceImpl}.
 * <p>
 * Created by z003bpej on 8/27/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebServerRequestServiceImplTest.Config.class})
public class WebServerRequestServiceImplTest {

    @Mock
    private WebServerService webServerService;

    @Mock
    private CommandExecutor executor;

    @Mock
    private JschBuilder jschBuilder;

    @Mock
    private JSch jSch;

    @Mock
    private SshConfiguration sshConfig;

    @Mock
    private WebServer aWebServer;

    @Mock
    private ClientHttpRequest request;

    @Mock
    private ClientHttpResponse clientHttpResponse;

    @Mock
    private RuntimeCommandBuilder rtCommandBuilder;

    @Mock
    private RuntimeCommand rtCommand;

    @Mock
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    final private Identifier<WebServer> id = new Identifier<>(1L);

    private WebServerCommandServiceImpl impl;

    @Autowired
    @Qualifier("factoryHelper")
    private ClientFactoryHelper factoryHelper;

    @Before
    public void setup() throws JSchException, CommandFailureException {
        MockitoAnnotations.initMocks(this);

        when(aWebServer.getName()).thenReturn("Apache2.2");
        when(aWebServer.getHttpConfigFile()).thenReturn(new FileSystemPath("d:/some-dir/httpd.conf"));
        when(webServerService.getWebServer(eq(id))).thenReturn(aWebServer);
        when(jschBuilder.build()).thenReturn(jSch);
        when(executor.execute(any(CommandProcessorBuilder.class)))
                .thenReturn(new CommandOutput(new ExecReturnCode(1), "The content of httpd.conf", ""));
        assertNotNull(factoryHelper);
        impl = new WebServerCommandServiceImpl(webServerService, executor, jschBuilder, sshConfig, channelPool);
    }

    @Test
    public void testGetHttpdConf() throws CommandFailureException {
        final CommandOutput execData = impl.getHttpdConf(id);
        assertEquals(execData.getStandardOutput(), "The content of httpd.conf");
    }

    @Test
    @Ignore
    public void testSecureCopyHttpdConf() throws CommandFailureException, IOException, URISyntaxException {
        when(Config.httpClientRequestFactory.createRequest(any(URI.class), eq(HttpMethod.GET))).thenReturn(request);
        when(request.execute()).thenReturn(clientHttpResponse);
        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.REQUEST_TIMEOUT);
        when(webServerService.getWebServer(anyString())).thenReturn(aWebServer);
        when(aWebServer.getStatusUri()).thenReturn(new URI("http://context/status.png"));
        when(rtCommandBuilder.build()).thenReturn(rtCommand);
        when(rtCommand.execute()).thenReturn(new CommandOutput(new ExecReturnCode(0), "", ""));

        // TODO move to web server control service impl test
        /*final CommandOutput execData = impl.secureCopyHttpdConf("ANY-SERVER-NAME", "d:/path/with/forward/slashes/new-httpd.conf", rtCommandBuilder);
        assertEquals("Expecting no errors so standard out should be empty", "", execData.getStandardOutput());
        assertEquals("Expecting no errors so standard error should be empty", "", execData.getStandardError());*/
    }

    // TODO do we need this anymore??
    @Configuration
    @ComponentScan("com.siemens.cto.aem.service.webserver.component")
    static class Config {
        @Mock
        private static HttpClientRequestFactory httpClientRequestFactory;

        public Config() {
            MockitoAnnotations.initMocks(this);
        }

        @Bean(name="webServerHttpRequestFactory")
        public HttpClientRequestFactory getHttpClientRequestFactory() {
            return httpClientRequestFactory;
        }

        @Bean(name = "factoryHelper")
        @Autowired
        public ClientFactoryHelper getClientFactoryHelper(ClientFactoryHelper factoryHelper) {
            return factoryHelper;
        }

    }

}
