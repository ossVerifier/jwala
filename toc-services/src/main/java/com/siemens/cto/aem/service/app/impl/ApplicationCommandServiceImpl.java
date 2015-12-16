package com.siemens.cto.aem.service.app.impl;

import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.common.request.app.ControlApplicationRequest;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.app.ApplicationCommandService;

/**
 * An implementation of ApplicationCommandService.
 * <p/>
 * Created by z003bpej on 9/9/2015.
 */
public class ApplicationCommandServiceImpl implements ApplicationCommandService {

    private final SshConfiguration sshConfig;
    private JschBuilder jschBuilder;

    public ApplicationCommandServiceImpl(final SshConfiguration sshConfig, JschBuilder jschBuilder) {
        this.sshConfig = sshConfig;
        this.jschBuilder = jschBuilder;
    }

    @Override
    public CommandOutput controlApplication(ControlApplicationRequest applicationRequest, Application app, String... params) throws CommandFailureException {
        RemoteSystemConnection remoteConnection = new RemoteSystemConnection(
                sshConfig.getUserName(),
                sshConfig.getPassword(),
                params[0],
                sshConfig.getPort());
        ExecCommand execCommand = new ExecCommand("secure-copy", params[1], params[2]);
        RemoteExecCommand remoteCommand = new RemoteExecCommand(remoteConnection, execCommand);
        try {
            final JschScpCommandProcessorImpl jschScpCommandProcessor = new JschScpCommandProcessorImpl(jschBuilder.build(), remoteCommand);
            jschScpCommandProcessor.processCommand();
            // if processCommand fails it throws an exception before completing
            return new CommandOutput(new ExecReturnCode(0), "", "");
        } catch (JSchException e) {
            throw new CommandFailureException(execCommand, new Throwable(e));
        }
    }
}
