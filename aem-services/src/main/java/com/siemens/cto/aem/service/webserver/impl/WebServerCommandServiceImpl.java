package com.siemens.cto.aem.service.webserver.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.control.jvm.command.windows.WindowsJvmNetOperation;
import com.siemens.cto.aem.control.webserver.command.WebServerExecCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.impl.DefaultWebServerExecCommandBuilderImpl;
import com.siemens.cto.aem.control.webserver.command.windows.WindowsWebServerNetOperation;
import com.siemens.cto.aem.control.webserver.impl.WebServerRemoteCommandProcessorBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecCommand;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
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

    public WebServerCommandServiceImpl(final WebServerService theWebServerService,
                                       final CommandExecutor theExecutor,
                                       final JschBuilder theJschBuilder,
                                       final SshConfiguration theSshConfig) {
        webServerService = theWebServerService;
        executor = theExecutor;
        jsch = theJschBuilder;
        sshConfig = theSshConfig;
    }

    @Override
    public ExecData getHttpdConf(Identifier<WebServer> aWebServerId) throws CommandFailureException {
        final WebServer aWebServer = webServerService.getWebServer(aWebServerId);
        final WebServerExecCommandBuilder builder = new DefaultWebServerExecCommandBuilderImpl();
        final ExecCommand execCommand = builder.setOperation(WebServerControlOperation.VIEW_HTTP_CONFIG_FILE)
                                                .setWebServer(aWebServer)
                                                .setParameter(aWebServer.getHttpConfigFile().getPath())
                                                .build();

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

}