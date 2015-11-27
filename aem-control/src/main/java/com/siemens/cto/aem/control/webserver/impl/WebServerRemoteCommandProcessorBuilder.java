package com.siemens.cto.aem.control.webserver.impl;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorImpl;
import com.siemens.cto.aem.domain.command.exec.ExecCommand;
import com.siemens.cto.aem.domain.command.exec.RemoteExecCommand;
import com.siemens.cto.aem.domain.command.exec.RemoteSystemConnection;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.exception.CommandFailureException;

public class WebServerRemoteCommandProcessorBuilder implements CommandProcessorBuilder {

    private ExecCommand command;
    private WebServer webServer;
    private JSch jsch;
    private SshConfiguration sshConfig;

    public WebServerRemoteCommandProcessorBuilder() {
    }

    public WebServerRemoteCommandProcessorBuilder setCommand(final ExecCommand aCommand) {
        command = aCommand;
        return this;
    }

    public WebServerRemoteCommandProcessorBuilder setWebServer(final WebServer aWebServer) {
        webServer = aWebServer;
        return this;
    }

    public WebServerRemoteCommandProcessorBuilder setJsch(final JSch aJsch) {
        jsch = aJsch;
        return this;
    }

    public WebServerRemoteCommandProcessorBuilder setSshConfig(final SshConfiguration aConfig) {
        sshConfig = aConfig;
        return this;
    }

    @Override
    public CommandProcessor build() throws CommandFailureException {

        final RemoteExecCommand remoteCommand = new RemoteExecCommand(getRemoteSystemConnection(),
                                                                      command);
        return new JschCommandProcessorImpl(jsch,
                                            remoteCommand);
    }

    protected RemoteSystemConnection getRemoteSystemConnection() {
        final RemoteSystemConnection connection = new RemoteSystemConnection(sshConfig.getUserName(),
                                                                             webServer.getHost(),
                                                                             sshConfig.getPort());
        return connection;
    }
}
