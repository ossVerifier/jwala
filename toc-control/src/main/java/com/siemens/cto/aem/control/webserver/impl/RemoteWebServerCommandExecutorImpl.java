package com.siemens.cto.aem.control.webserver.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.request.webserver.ControlWebServerRequest;
import com.siemens.cto.aem.control.webserver.WebServerCommandExecutor;
import com.siemens.cto.aem.control.webserver.command.WebServerExecCommandBuilder;
import com.siemens.cto.aem.control.webserver.command.impl.DefaultWebServerExecCommandBuilderImpl;
import com.siemens.cto.aem.exception.CommandFailureException;

import java.text.SimpleDateFormat;
import java.util.Date;

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
                                          final WebServer aWebServer) throws CommandFailureException {

        final WebServerExecCommandBuilder commandBuilder = new DefaultWebServerExecCommandBuilderImpl();
        commandBuilder.setOperation(controlWebServerRequest.getControlOperation());
        commandBuilder.setWebServer(aWebServer);
        final ExecCommand execCommand = commandBuilder.build();

        return executeRemoteJschCommand(aWebServer, execCommand);
    }

    @Override
    public CommandOutput secureCopyHttpdConf(WebServer aWebServer, String sourcePath, String destPath) throws CommandFailureException {

        final WebServerExecCommandBuilder commandBuilder = new DefaultWebServerExecCommandBuilderImpl();

        // back up the original file first
        commandBuilder.setOperation(WebServerControlOperation.BACK_UP_HTTP_CONFIG_FILE);
        String currentDateSuffix = new SimpleDateFormat(".yyyyMMdd_HHmmss").format(new Date());
        commandBuilder.setParameter(destPath, destPath + currentDateSuffix);
        commandBuilder.setWebServer(aWebServer);
        ExecCommand execCommand = commandBuilder.build();

        final CommandOutput commandOutput = executeRemoteJschCommand(aWebServer, execCommand);
        if (!commandOutput.getReturnCode().wasSuccessful()) {
            throw new CommandFailureException(execCommand, new Throwable("Failed to back up the httpd.conf for " + aWebServer));
        }

        // run the scp command
        commandBuilder.setOperation(WebServerControlOperation.DEPLOY_HTTP_CONFIG_FILE);
        commandBuilder.setParameter(sourcePath, destPath);
        commandBuilder.setWebServer(aWebServer);
        execCommand = commandBuilder.build();

        return executeRemoteJschCommand(aWebServer, execCommand);
    }

    private CommandOutput executeRemoteJschCommand(WebServer aWebServer, ExecCommand execCommand) throws CommandFailureException {
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
