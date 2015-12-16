package com.siemens.cto.aem.control.webserver.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.WebServerExecCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.impl.DefaultWebServerExecCommandBuilderImpl;
import com.siemens.cto.aem.exception.CommandFailureException;

public class RemoteWebServerCommandExecutorImpl implements WebServerCommandExecutor {

    private final CommandExecutor executor;
    private final JschBuilder jsch;
    private final SshConfiguration sshConfig;

    public RemoteWebServerCommandExecutorImpl(final CommandExecutor theExecutor,
                                              final JschBuilder theJschBuilder,
                                              final SshConfiguration theSshConfig) {
        executor = theExecutor;
        jsch = theJschBuilder;
        sshConfig = theSshConfig;
    }

    @Override
    public CommandOutput controlWebServer(final ControlWebServerRequest controlWebServerRequest,
                                          final WebServer aWebServer,
                                          String... params) throws CommandFailureException {

        final WebServerExecCommandBuilder commandBuilder = new DefaultWebServerExecCommandBuilderImpl();
        commandBuilder.setOperation(controlWebServerRequest.getControlOperation());
        commandBuilder.setWebServer(aWebServer);
        if (params.length > 0) {
            commandBuilder.setParameter(params);
        }
        final ExecCommand execCommand = commandBuilder.build();

        try {
            final WebServerRemoteCommandProcessorBuilder processorBuilder = new WebServerRemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand);
            processorBuilder.setWebServer(aWebServer);
            processorBuilder.setJsch(jsch.build());
            processorBuilder.setSshConfig(sshConfig);

            return executor.execute(processorBuilder);
        } catch (final JSchException jsche) {
            throw new CommandFailureException(execCommand,
                    jsche);
        }
    }

}
