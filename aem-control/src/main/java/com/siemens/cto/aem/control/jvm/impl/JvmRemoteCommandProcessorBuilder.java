package com.siemens.cto.aem.control.jvm.impl;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandProcessor;
import com.siemens.cto.aem.commandprocessor.CommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorImpl;
import com.siemens.cto.aem.domain.command.exec.ExecCommand;
import com.siemens.cto.aem.domain.command.exec.RemoteExecCommand;
import com.siemens.cto.aem.domain.command.exec.RemoteSystemConnection;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.exception.CommandFailureException;

public class JvmRemoteCommandProcessorBuilder implements CommandProcessorBuilder {

    private ExecCommand command;
    private Jvm jvm;
    private JSch jsch;
    private SshConfiguration sshConfig;

    public JvmRemoteCommandProcessorBuilder() {
    }

    public JvmRemoteCommandProcessorBuilder setCommand(final ExecCommand aCommand) {
        command = aCommand;
        return this;
    }

    public JvmRemoteCommandProcessorBuilder setJvm(final Jvm aJvm) {
        jvm = aJvm;
        return this;
    }

    public JvmRemoteCommandProcessorBuilder setJsch(final JSch aJsch) {
        jsch = aJsch;
        return this;
    }

    public JvmRemoteCommandProcessorBuilder setSshConfig(final SshConfiguration aConfig) {
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
                                                                             jvm.getHostName(),
                                                                             sshConfig.getPort());
        return connection;
    }
}
