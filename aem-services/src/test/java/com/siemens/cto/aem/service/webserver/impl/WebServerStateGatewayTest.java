package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.control.webserver.command.impl.WebServerServiceExistenceFacade;
import com.siemens.cto.aem.control.webserver.impl.WebServerRemoteCommandProcessorBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.path.Path;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.webserver.WebServerStateGateway;
import com.siemens.cto.aem.service.webserver.heartbeat.WebServerStateServiceFacade;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link com.siemens.cto.aem.service.webserver.WebServerStateGateway} implementation.
 *
 * Created by Z003BPEJ on 6/9/2015.
 */
public class WebServerStateGatewayTest {

    private WebServerStateGateway gateway;
    private WebServer webServer;
    private HttpClientRequestFactory httpClientRequestFactory;
    private WebServerServiceExistenceFacade webServerServiceExistenceFacade;
    private CommandExecutor commandExecutor;
    private AemSshConfig aemSshConfig;
    private WebServerStateServiceFacade webServerStateServiceFacade;
    private ClientHttpRequest clientHttpRequest;
    private ClientHttpResponse clientHttpResponse;

    @Before
    public void setup() throws URISyntaxException {
        httpClientRequestFactory = mock(HttpClientRequestFactory.class);
        webServerServiceExistenceFacade = mock(WebServerServiceExistenceFacade.class);
        commandExecutor = mock(CommandExecutor.class);
        aemSshConfig = mock(AemSshConfig.class);
        webServerStateServiceFacade = mock(WebServerStateServiceFacade.class);
        clientHttpRequest = mock(ClientHttpRequest.class);
        clientHttpResponse = mock(ClientHttpResponse.class);

        gateway = new WebServerStateGatewayImpl(httpClientRequestFactory, webServerServiceExistenceFacade,
                                                commandExecutor, aemSshConfig, webServerStateServiceFacade);

        webServer = new WebServer(new Identifier<WebServer>(1L),
                new ArrayList<Group>(),
                null,
                "localhost",
                80,
                null,
                new Path("/stp.png"),
                null,
                null,
                null);
    }

    @Test
    public void testReachableInitiateWebServerStateRequest() throws IOException {
            when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
            when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);
            when(httpClientRequestFactory.createRequest(eq(webServer.getStatusUri()), eq(HttpMethod.GET))).thenReturn(clientHttpRequest);
            gateway.initiateWebServerStateRequest(webServer);
            verify(httpClientRequestFactory).createRequest(eq(webServer.getStatusUri()), eq(HttpMethod.GET));
            verify(webServerStateServiceFacade).setState(eq(webServer.getId()), eq(WebServerReachableState.WS_REACHABLE),
                    any(DateTime.class));
    }

    @Test
    public void testUnreachableInitiateWebServerStateRequest() throws IOException, JSchException, CommandFailureException {
            when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
            when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);
            when(httpClientRequestFactory.createRequest(eq(webServer.getStatusUri()), eq(HttpMethod.GET))).thenReturn(clientHttpRequest);
            when(webServerServiceExistenceFacade.getServiceExistenceCommandFor(webServer)).thenReturn(new ExecCommand());

            final JschBuilder jschBuilder = mock(JschBuilder.class);
            when(jschBuilder.build()).thenReturn(new JSch());
            when(aemSshConfig.getJschBuilder()).thenReturn(jschBuilder);
            when(aemSshConfig.getSshConfiguration()).thenReturn(new SshConfiguration("z003bpej", 22, "any", "any"));

            final ExecData execData = new ExecData(new ExecReturnCode(0), null, null);
            when(commandExecutor.execute(any(WebServerRemoteCommandProcessorBuilder.class))).thenReturn(execData);
            gateway.initiateWebServerStateRequest(webServer);
            verify(webServerStateServiceFacade).setState(eq(webServer.getId()), eq(WebServerReachableState.WS_UNREACHABLE),
                    any(DateTime.class));
    }

    /**
     * Scenario where http request throws an {@link java.io.IOException}.
     */
    @Test
    public void testUnreachableInitiateWebServerStateRequest2() throws IOException, JSchException, CommandFailureException {
            when(clientHttpResponse.getStatusCode()).thenThrow(new IOException());
            when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);
            when(httpClientRequestFactory.createRequest(eq(webServer.getStatusUri()), eq(HttpMethod.GET))).thenReturn(clientHttpRequest);
            when(webServerServiceExistenceFacade.getServiceExistenceCommandFor(webServer)).thenReturn(new ExecCommand());

            final JschBuilder jschBuilder = mock(JschBuilder.class);
            when(jschBuilder.build()).thenReturn(new JSch());
            when(aemSshConfig.getJschBuilder()).thenReturn(jschBuilder);
            when(aemSshConfig.getSshConfiguration()).thenReturn(new SshConfiguration("z003bpej", 22, "any", "any"));

            final ExecData execData = new ExecData(new ExecReturnCode(0), null, null);
            when(commandExecutor.execute(any(WebServerRemoteCommandProcessorBuilder.class))).thenReturn(execData);
            gateway.initiateWebServerStateRequest(webServer);
            verify(webServerStateServiceFacade).setState(eq(webServer.getId()), eq(WebServerReachableState.WS_UNREACHABLE),
                    any(DateTime.class));
    }

    @Test
    public void testFailedInitiateWebServerStateRequest() throws IOException, JSchException, CommandFailureException {
            when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
            when(clientHttpRequest.execute()).thenReturn(clientHttpResponse);
            when(httpClientRequestFactory.createRequest(eq(webServer.getStatusUri()), eq(HttpMethod.GET))).thenReturn(clientHttpRequest);
            when(webServerServiceExistenceFacade.getServiceExistenceCommandFor(webServer)).thenReturn(new ExecCommand());

            final JschBuilder jschBuilder = mock(JschBuilder.class);
            when(jschBuilder.build()).thenReturn(new JSch());
            when(aemSshConfig.getJschBuilder()).thenReturn(jschBuilder);
            when(aemSshConfig.getSshConfiguration()).thenReturn(new SshConfiguration("z003bpej", 22, "any", "any"));

            final Throwable t = new CommandFailureException(null, new Exception("Any!"));
            when(commandExecutor.execute(any(WebServerRemoteCommandProcessorBuilder.class))).
                    thenThrow(t);
            gateway.initiateWebServerStateRequest(webServer);
            verify(webServerStateServiceFacade).setStateWithMessageAndException(eq(webServer.getId()),
                    eq(WebServerReachableState.WS_FAILED), any(DateTime.class), eq("java.lang.Exception: Any!"), eq(t));
    }

}
