package com.siemens.cto.aem.control.command;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.control.AemControl;
import com.siemens.cto.aem.exception.CommandFailureException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class RemoteCommandProcessorBuilder implements CommandProcessorBuilder {

    private ExecCommand command;
    private String hostName;
    private JSch jsch;
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
            return new JschScpCommandProcessorImpl(jsch, remoteCommand);
        } else {
            return new JschCommandProcessorImpl(jsch, remoteCommand, channelPool);
        }
    }

    protected RemoteSystemConnection getRemoteSystemConnection() {
        return new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), hostName, sshConfig.getPort());
    }

}
