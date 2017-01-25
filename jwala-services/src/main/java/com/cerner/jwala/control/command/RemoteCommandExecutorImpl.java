package com.cerner.jwala.control.command;

import com.cerner.jwala.commandprocessor.CommandExecutor;
import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.exception.CommandFailureException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;

import org.apache.commons.pool2.impl.GenericKeyedObjectPool;

public class RemoteCommandExecutorImpl<T> implements RemoteCommandExecutor<T> {
    private final com.cerner.jwala.commandprocessor.CommandExecutor executor;
    private final JschBuilder jsch;
    private final JschService jschService;
    private final SshConfiguration sshConfig;
    private final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool;

    public RemoteCommandExecutorImpl(final CommandExecutor theExecutor,
                                     final JschBuilder theJschBuilder,
                                     final SshConfiguration theSshConfig,
                                     final GenericKeyedObjectPool<ChannelSessionKey, Channel> channelPool,
                                     final JschService jschService) {
        executor = theExecutor;
        jsch = theJschBuilder;
        sshConfig = theSshConfig;
        this.channelPool = channelPool;
        this.jschService = jschService;
    }

    @Override
    public CommandOutput executeRemoteCommand(final String entityName,
                                              final String entityHost,
                                              final T anOperation,
                                              final PlatformCommandProvider provider,
                                              String... params) throws CommandFailureException {

        final DefaultExecCommandBuilderImpl<T> commandBuilder = new DefaultExecCommandBuilderImpl<>();
        commandBuilder.setOperation(anOperation);
        commandBuilder.setEntityName(entityName);
        if (params.length > 0) {
            commandBuilder.setParameter(params);
        }
        final ExecCommand execCommand = commandBuilder.build(provider);

        try {
            final RemoteCommandProcessorBuilder processorBuilder = new RemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand).setHost(entityHost).setJsch(jsch.build()).setJschService(jschService)
                    .setSshConfig(sshConfig).setChannelPool(channelPool);

            return executor.execute(processorBuilder);
        } catch (final JSchException jsche) {
            throw new CommandFailureException(execCommand,
                    jsche);
        }
    }
}
