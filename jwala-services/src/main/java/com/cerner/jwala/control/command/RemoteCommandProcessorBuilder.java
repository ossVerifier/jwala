package com.cerner.jwala.control.command;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.CommandProcessorBuilder;
import com.cerner.jwala.commandprocessor.impl.jsch.JschCommandProcessorImpl;
import com.cerner.jwala.commandprocessor.impl.jsch.JschScpCmdProcessorImpl;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.control.AemControl;
import com.cerner.jwala.exception.CommandFailureException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class RemoteCommandProcessorBuilder implements CommandProcessorBuilder {

    private ExecCommand command;
    private String hostName;
    private JSch jsch;
    private JschService jschService;
    private SshConfiguration sshConfig;
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

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

    public RemoteCommandProcessorBuilder setJschService(final JschService jschService) {
        this.jschService = jschService;
        return this;
    }

    public RemoteCommandProcessorBuilder setSshConfig(final SshConfiguration aConfig) {
        sshConfig = aConfig;
        return this;
    }

    public RemoteCommandProcessorBuilder setChannelPool(final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.channelPool = channelPool;
        return this;
    }

    @Override
    public CommandProcessor build() throws CommandFailureException {
        final RemoteExecCommand remoteCommand = new RemoteExecCommand(getRemoteSystemConnection(), command);
        if (command.getCommandFragments().get(0).contains(AemControl.Properties.SCP_SCRIPT_NAME.getValue())) {
            return new JschScpCmdProcessorImpl(jsch, remoteCommand);
        } else {
            return new JschCommandProcessorImpl(remoteCommand, jschService);
        }
    }

    protected RemoteSystemConnection getRemoteSystemConnection() {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getEncryptedPassword(), hostName, sshConfig.getPort());
    }

}
