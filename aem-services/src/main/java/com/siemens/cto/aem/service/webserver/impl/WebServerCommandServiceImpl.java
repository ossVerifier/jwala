package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.exception.InternalErrorException;
import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.WebServerExecCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.impl.DefaultWebServerExecCommandBuilderImpl;
import com.siemens.cto.aem.control.webserver.impl.WebServerRemoteCommandProcessorBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.ExecReturnCode;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import org.apache.http.conn.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.ConnectException;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_SCRIPT_NAME;

/**
 * Encapsulates non-state altering commands to a web server.
 * <p>
 * Created by z003bpej on 8/25/14.
 */
public class WebServerCommandServiceImpl implements WebServerCommandService {

    private final WebServerService webServerService;
    private final CommandExecutor executor;
    private final JschBuilder jsch;
    private final SshConfiguration sshConfig;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebServerCommandServiceImpl.class);
    private ClientFactoryHelper clientFactoryHelper;

    public WebServerCommandServiceImpl(final WebServerService theWebServerService,
                                       final CommandExecutor theExecutor,
                                       final JschBuilder theJschBuilder,
                                       final SshConfiguration theSshConfig,
                                       final ClientFactoryHelper factoryHelper) {
        webServerService = theWebServerService;
        executor = theExecutor;
        jsch = theJschBuilder;
        sshConfig = theSshConfig;
        clientFactoryHelper = factoryHelper;
    }

    @Override
    public ExecData getHttpdConf(Identifier<WebServer> aWebServerId) throws CommandFailureException {
        final WebServer aWebServer = webServerService.getWebServer(aWebServerId);
        final ExecCommand execCommand = createExecCommand(aWebServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE, aWebServer.getHttpConfigFile().getUriPath());

        return executeCommand(aWebServer, execCommand);
    }

    @Override
    public ExecData secureCopyHttpdConf(String aWebServerName, String sourcePath, RuntimeCommandBuilder rtCommandBuilder) throws CommandFailureException {

        final WebServer aWebServer = webServerService.getWebServer(aWebServerName);

        // ping the web server and return if not stopped
        try {
            ClientHttpResponse response = clientFactoryHelper.requestGet(aWebServer.getStatusUri());
            if (response.getStatusCode() == HttpStatus.OK) {
                return new ExecData(new ExecReturnCode(1), "", "The target web server must be stopped before attempting to copy the httpd.conf file to the server");
            }
        } catch (IOException e) {
            if (!(e instanceof ConnectException || e instanceof ConnectTimeoutException)) {
                LOGGER.error("Failed to ping {} while attempting to copy httpd.config :: ERROR: {}",aWebServerName, e.getMessage());
                throw new InternalErrorException(AemFaultType.INVALID_WEBSERVER_OPERATION,
                        "Failed to ping " + aWebServerName + " while attempting to copy httpd.config", e);
            }
        }

        // create and execute the scp command
        String httpdConfUriPath = aWebServer.getHttpConfigFile().getUriPath();
        rtCommandBuilder.setOperation(SCP_SCRIPT_NAME);
        rtCommandBuilder.addParameter(sourcePath);
        rtCommandBuilder.addParameter(sshConfig.getUserName());
        rtCommandBuilder.addParameter(aWebServer.getHost());
        rtCommandBuilder.addParameter(httpdConfUriPath);
        RuntimeCommand rtCommand = rtCommandBuilder.build();
        return rtCommand.execute();
    }

    private ExecData executeCommand(WebServer aWebServer, ExecCommand execCommand) throws CommandFailureException {
        try {
            final WebServerRemoteCommandProcessorBuilder processorBuilder = new WebServerRemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand);
            processorBuilder.setWebServer(aWebServer);
            processorBuilder.setJsch(jsch.build());
            processorBuilder.setSshConfig(sshConfig);

            return executor.execute(processorBuilder);
        } catch (final JSchException jsche) {
            throw new CommandFailureException(execCommand, jsche);
        }
    }

    private ExecCommand createExecCommand(WebServer aWebServer, WebServerControlOperation wsControlOp, String... params) {
        final WebServerExecCommandBuilder builder = new DefaultWebServerExecCommandBuilderImpl();
        builder.setOperation(wsControlOp);
        builder.setWebServer(aWebServer);
        for (String param : params) {
            builder.setParameter(param);
        }
        return builder.build();
    }

}