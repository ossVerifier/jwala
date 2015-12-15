package com.siemens.cto.aem.control.jvm.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.control.jvm.JvmCommandExecutor;
import com.siemens.cto.aem.control.jvm.command.JvmExecCommandBuilder;
import com.siemens.cto.aem.control.jvm.command.impl.DefaultJvmExecCommandBuilderImpl;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;

public class RemoteJvmCommandExecutorImpl implements JvmCommandExecutor {

    private final CommandExecutor executor;
    private final JschBuilder jschBuilder;
    private final SshConfiguration sshConfig;

    public RemoteJvmCommandExecutorImpl(final CommandExecutor theExecutor,
                                        final JschBuilder theJschBuilder,
                                        final SshConfiguration theSshConfig) {
        executor = theExecutor;
        jschBuilder = theJschBuilder;
        sshConfig = theSshConfig;
    }

    @Override
    public CommandOutput controlJvm(final ControlJvmRequest aCommand, final JpaJvm jvm, String... aParam) throws CommandFailureException {

        final JvmExecCommandBuilder commandBuilder = new DefaultJvmExecCommandBuilderImpl();
        commandBuilder.setOperation(aCommand.getControlOperation());
        commandBuilder.setJvm(jvm);
        if (aParam.length > 0) {
            commandBuilder.setParameter(aParam);
        }
        final ExecCommand execCommand = commandBuilder.build();

        try {
            final JvmRemoteCommandProcessorBuilder processorBuilder = new JvmRemoteCommandProcessorBuilder();
            processorBuilder.setCommand(execCommand);
            processorBuilder.setJvm(jvm);
            processorBuilder.setJsch(jschBuilder.build());
            processorBuilder.setSshConfig(sshConfig);

            return executor.execute(processorBuilder);
        } catch (JSchException jsche) {
            throw new CommandFailureException(execCommand,
                    jsche);
        }
    }
}
