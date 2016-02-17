package com.siemens.cto.aem.control.command;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschChannelService;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.CommandFailureException;

public class RemoteCommandProcessorBuilder implements CommandProcessorBuilder {

    private ExecCommand command;
    private String hostName;
    private JSch jsch;
    private SshConfiguration sshConfig;
    private JschChannelService jschChannelService;

    public RemoteCommandProcessorBuilder() {
    }

    public RemoteCommandProcessorBuilder setCommand(final ExecCommand aCommand) {
        command = aCommand;
        return this;
    }

    public RemoteCommandProcessorBuilder setHost(final String host) {
        hostName = host;
        return this;
    }

    public RemoteCommandProcessorBuilder setJsch(final JSch aJsch) {
        jsch = aJsch;
        return this;
    }

    public RemoteCommandProcessorBuilder setSshConfig(final SshConfiguration aConfig) {
        sshConfig = aConfig;
        return this;
    }

    public RemoteCommandProcessorBuilder setJschChannelService(final JschChannelService jschChannelService) {
        this.jschChannelService = jschChannelService;
        return this;
    }

    @Override
    public CommandProcessor build() throws CommandFailureException {
        final RemoteExecCommand remoteCommand = new RemoteExecCommand(getRemoteSystemConnection(), command);
        if (command.getCommandFragments().get(0).contains("secure-copy")) {
            return new JschScpCommandProcessorImpl(jsch, remoteCommand);
        } else {
            return new JschCommandProcessorImpl(jsch, remoteCommand, jschChannelService);
        }
    }

    protected RemoteSystemConnection getRemoteSystemConnection() {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), hostName, sshConfig.getPort());
    }

}
