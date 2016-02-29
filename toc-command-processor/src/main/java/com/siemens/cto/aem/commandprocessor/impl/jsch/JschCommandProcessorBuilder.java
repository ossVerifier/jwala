package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
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
