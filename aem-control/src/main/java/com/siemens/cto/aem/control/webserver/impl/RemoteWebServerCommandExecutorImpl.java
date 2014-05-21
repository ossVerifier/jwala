package com.siemens.cto.aem.control.webserver.impl;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.WebServerExecCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.impl.DefaultWebServerExecCommandBuilderImpl;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public class RemoteWebServerCommandExecutorImpl implements WebServerCommandExecutor {

    private final CommandExecutor executor;
    private final JSch jsch;
    private final SshConfiguration sshConfig;

    public RemoteWebServerCommandExecutorImpl(final CommandExecutor theExecutor,
                                              final JSch theJsch,
                                              final SshConfiguration theSshConfig) {
        executor = theExecutor;
        jsch = theJsch;
        sshConfig = theSshConfig;
    }

    @Override
    public ExecData controlWebServer(final ControlWebServerCommand aCommand,
                                     final WebServer aWebServer) throws CommandFailureException {

        final WebServerExecCommandBuilder commandBuilder = new DefaultWebServerExecCommandBuilderImpl();
        commandBuilder.setOperation(aCommand.getControlOperation());
        commandBuilder.setWebServer(aWebServer);

        final WebServerRemoteCommandProcessorBuilder processorBuilder = new WebServerRemoteCommandProcessorBuilder();
        processorBuilder.setCommand(commandBuilder.build());
        processorBuilder.setWebServer(aWebServer);
        processorBuilder.setJsch(jsch);
        processorBuilder.setSshConfig(sshConfig);

        return executor.execute(processorBuilder);
    }
}