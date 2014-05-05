package com.siemens.cto.aem.control.jvm.impl;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionData;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.control.jvm.command.JvmExecCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.impl.DefaultJvmExecCommandBuilderImpl;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.exception.CommandFailureException;

public class RemoteJvmCommandExecutorImpl implements JvmCommandExecutor {

    private final CommandExecutor executor;
    private final JSch jsch;
    private final SshConfiguration sshConfig;

    public RemoteJvmCommandExecutorImpl(final CommandExecutor theExecutor,
                                        final JSch theJsch,
                                        final SshConfiguration theSshConfig) {
        executor = theExecutor;
        jsch = theJsch;
        sshConfig = theSshConfig;
    }

    @Override
    public ExecutionData controlJvm(final ControlJvmCommand aCommand,
                                    final Jvm aJvm) throws CommandFailureException {

        final JvmExecCommandBuilder commandBuilder = new DefaultJvmExecCommandBuilderImpl();
        commandBuilder.setOperation(aCommand.getControlOperation());
        commandBuilder.setJvm(aJvm);

        final JvmRemoteCommandProcessorBuilder processorBuilder = new JvmRemoteCommandProcessorBuilder();
        processorBuilder.setCommand(commandBuilder.build());
        processorBuilder.setJvm(aJvm);
        processorBuilder.setJsch(jsch);
        processorBuilder.setSshConfig(sshConfig);

        return executor.execute(processorBuilder);
    }
}
