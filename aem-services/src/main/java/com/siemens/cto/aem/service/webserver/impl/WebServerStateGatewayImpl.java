package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.control.configuration.AemSshConfig;
import com.siemens.cto.aem.control.webserver.command.impl.WebServerServiceExistenceFacade;
import com.siemens.cto.aem.control.webserver.impl.WebServerRemoteCommandProcessorBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.exception.SetWebServerStateException;
import com.siemens.cto.aem.service.webserver.WebServerStateGateway;
import com.siemens.cto.aem.service.webserver.heartbeat.WebServerStateServiceFacade;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Created by Z003BPEJ on 6/8/2015.
 */
public class WebServerStateGatewayImpl implements WebServerStateGateway {

    private HttpClientRequestFactory httpClientRequestFactory;
    private WebServerServiceExistenceFacade webServerServiceExistenceFacade;
    private CommandExecutor commandExecutor;
    private AemSshConfig aemSshConfig;
    private WebServerStateServiceFacade webServerStateServiceFacade;

    @Autowired
    public WebServerStateGatewayImpl(@Qualifier("webServerHttpRequestFactory") HttpClientRequestFactory httpClientRequestFactory,
                                     @Qualifier("webServerServiceExistence") WebServerServiceExistenceFacade webServerServiceExistenceFacade,
                                     CommandExecutor commandExecutor,
                                     AemSshConfig aemSshConfig,
                                     @Qualifier("webServerStateServiceFacade") WebServerStateServiceFacade webServerStateServiceFacade) {
        this.httpClientRequestFactory = httpClientRequestFactory;
        this.webServerServiceExistenceFacade = webServerServiceExistenceFacade;
        this.commandExecutor = commandExecutor;
        this.aemSshConfig = aemSshConfig;
        this.webServerStateServiceFacade = webServerStateServiceFacade;
    }

    @Override
    public void initiateWebServerStateRequest(WebServer webServer) {
        ExecData execData = null;
        Exception pingWebServerException = null;

        try {
            if (pingWebServerViaHttp(webServer) != HttpStatus.OK) {
                execData = pingWebServerViaSsh(webServer);
            }
        } catch (Exception e) {
            pingWebServerException = e;
        } finally {
            setWebServerState(webServer, execData, pingWebServerException);
        }
    }

    /**
     * Ping a web server via http.
     * @param webServer- a web server.
     * @return {@link org.springframework.http.HttpStatus} or null if there's an IOException.
     */
    private HttpStatus pingWebServerViaHttp(final WebServer webServer) {
        try {
            ClientHttpRequest request = httpClientRequestFactory.createRequest(webServer.getStatusUri(), HttpMethod.GET);
            ClientHttpResponse response = request.execute();
            return response.getStatusCode();
        } catch (IOException ioe) {
            return null;
        }
    }

    /**
     * Ping a web server using ssh.
     * @param webServer the web server
     * @return {@link com.siemens.cto.aem.domain.model.exec.ExecData} or null if there's an exception.
     */
    private ExecData pingWebServerViaSsh(final WebServer webServer) throws JSchException, CommandFailureException {
        final WebServerRemoteCommandProcessorBuilder builder = new WebServerRemoteCommandProcessorBuilder();

        builder.setCommand(webServerServiceExistenceFacade.getServiceExistenceCommandFor(webServer))
               .setWebServer(webServer)
               .setJsch(aemSshConfig.getJschBuilder().build())
               .setSshConfig(aemSshConfig.getSshConfiguration());
        return commandExecutor.execute(builder);
    }

    /**
     * Set web server state.
     * @param webServer {@link com.siemens.cto.aem.domain.model.webserver.WebServer}
     * @param execData {@link com.siemens.cto.aem.domain.model.exec.ExecData}
     * @param t {@link Throwable}
     */
    private void setWebServerState(final WebServer webServer, final ExecData execData, final Throwable t) {
        if (t == null && execData == null) {
            webServerStateServiceFacade.setState(webServer.getId(),
                                                 WebServerReachableState.WS_REACHABLE,
                                                 DateTime.now());
        } else if (t == null && execData.getReturnCode().wasSuccessful() &&
            StringUtils.isEmpty(execData.getStandardError())) {
            webServerStateServiceFacade.setState(webServer.getId(),
                                                 WebServerReachableState.WS_UNREACHABLE,
                                                 DateTime.now());
        } else {
            try {
                webServerStateServiceFacade.setStateWithMessageAndException(webServer.getId(),
                        WebServerReachableState.WS_FAILED, DateTime.now(),
                        (t == null ? execData.getStandardError() : t.getMessage()), t);
            } catch (IOException e) {
                throw new SetWebServerStateException(e);
            }
        }
    }

}
