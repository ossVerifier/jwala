package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
import com.cerner.jwala.commandprocessor.CommandProcessorBuilder;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class JschCommandProcessorBuilder implements CommandProcessorBuilder {

    private JSch jsch;
    private RemoteExecCommand remoteCommand;
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    public JschCommandProcessorBuilder() {
    }

    public JschCommandProcessorBuilder setJsch(final JSch aJsch) {
        jsch = aJsch;
        return this;
    }

    public JschCommandProcessorBuilder setRemoteCommand(final RemoteExecCommand aRemoteCommand) {
        remoteCommand = aRemoteCommand;
        return this;
    }

    public JschCommandProcessorBuilder setChannelPool(final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool) {
        this.channelPool = channelPool;
        return this;
    }

    @Override
    public CommandProcessor build() throws RemoteCommandFailureException {
        return new JschCommandProcessorImpl(jsch, remoteCommand, channelPool);
    }

}
