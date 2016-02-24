package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.JschChannelService;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.control.command.DefaultExecCommandBuilderImpl;
import com.siemens.cto.aem.control.webserver.command.impl.WindowsWebServerPlatformCommandProvider;
import com.siemens.cto.aem.control.command.RemoteCommandProcessorBuilder;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.webserver.WebServerCommandService;
import com.siemens.cto.aem.service.webserver.WebServerService;

/**
 * Encapsulates non-state altering commands to a web server.
 *
 * Created by z003bpej on 8/25/14.
 */
public class WebServerCommandServiceImpl implements WebServerCommandService {

    private final WebServerService webServerService;
    private final CommandExecutor executor;
    private final JschBuilder jsch;
    private final SshConfiguration sshConfig;
    private final JschChannelService jschChannelService;
    
    public WebServerCommandServiceImpl(final WebServerService theWebServerService,
                                       final CommandExecutor theExecutor,
                                       final JschBuilder theJschBuilder,
                                       final SshConfiguration theSshConfig,
                                       final JschChannelService jschChannelService) {
        webServerService = theWebServerService;
        executor = theExecutor;
        jsch = theJschBuilder;
        sshConfig = theSshConfig;
        this.jschChannelService = jschChannelService;
    }

    @Override
    public CommandOutput getHttpdConf(Identifier<WebServer> aWebServerId) throws CommandFailureException {
        final WebServer aWebServer = webServerService.getWebServer(aWebServerId);
        final ExecCommand execCommand = createExecCommand(aWebServer, WebServerControlOperation.VIEW_HTTP_CONFIG_FILE,
                aWebServer.getHttpConfigFile().getUriPath());

        return executeCommand(aWebServer, execCommand);
    }

    private CommandOutput executeCommand(WebServer aWebServer, ExecCommand execCommand) throws CommandFailureException {
        try {
            final RemoteCommandProcessorBuilder processorBuilder = new RemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand).setHost(aWebServer.getHost()).setJsch(jsch.build()).setSshConfig(sshConfig)
                    .setJschChannelService(jschChannelService);

            return executor.execute(processorBuilder);
        } catch (final JSchException jsche) {
            throw new CommandFailureException(execCommand, jsche);
        }
    }

    private ExecCommand createExecCommand(WebServer aWebServer, WebServerControlOperation wsControlOp, String... params) {

        final DefaultExecCommandBuilderImpl<WebServerControlOperation> builder = new DefaultExecCommandBuilderImpl<>();
        builder.setOperation(wsControlOp);
        builder.setEntityName(aWebServer.getName());
        for (String param : params) {
            builder.setParameter(param);
        }
        return builder.build(new WindowsWebServerPlatformCommandProvider());
    }

}
