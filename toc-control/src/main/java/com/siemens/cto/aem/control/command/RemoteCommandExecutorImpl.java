package com.siemens.cto.aem.control.command;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.exception.CommandFailureException;

public class RemoteCommandExecutorImpl<T> implements RemoteCommandExecutor<T> {
    private final com.siemens.cto.aem.commandprocessor.CommandExecutor executor;
    private final JschBuilder jsch;
    private final SshConfiguration sshConfig;

    public RemoteCommandExecutorImpl(final CommandExecutor theExecutor,
                                     final JschBuilder theJschBuilder,
                                     final SshConfiguration theSshConfig) {
        executor = theExecutor;
        jsch = theJschBuilder;
        sshConfig = theSshConfig;
    }

    @Override
    public CommandOutput executeRemoteCommand(final String entityName,
                                              final String entityHost,
                                              final T anOperation,
                                              final PlatformCommandProvider provider,
                                              String... params) throws CommandFailureException {

        final DefaultExecCommandBuilderImpl<T> commandBuilder = new DefaultExecCommandBuilderImpl();
        commandBuilder.setOperation(anOperation);
        commandBuilder.setEntityName(entityName);
        if (params.length > 0) {
            commandBuilder.setParameter(params);
        }
        final ExecCommand execCommand = commandBuilder.build(provider);

        try {
            final RemoteCommandProcessorBuilder processorBuilder = new RemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand);
            processorBuilder.setHost(entityHost);
            processorBuilder.setJsch(jsch.build());
            processorBuilder.setSshConfig(sshConfig);

            return executor.execute(processorBuilder);
        } catch (final JSchException jsche) {
            throw new CommandFailureException(execCommand,
                    jsche);
        }
    }

}
