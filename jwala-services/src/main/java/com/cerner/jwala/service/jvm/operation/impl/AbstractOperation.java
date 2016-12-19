package com.cerner.jwala.service.jvm.operation.impl;

import com.cerner.jwala.common.domain.model.jvm.Jvm;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.service.RemoteCommandExecutorService;
import com.cerner.jwala.service.jvm.operation.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * Implement {@link Operation}'s execute
 *
 * Created by Jedd Cuison on 12/16/2016
 */
abstract class AbstractOperation implements Operation {

    @Autowired
    protected SshConfiguration sshConfig;

    @Value("${remote.paths.instances}")
    protected String remoteJvmInstanceDir;

    @Autowired
    private RemoteCommandExecutorService remoteCommandExecutorService;

    @Override
    public void execute(final Jvm jvm) {
        remoteCommandExecutorService.executeCommand(new RemoteExecCommand(
                new RemoteSystemConnection(sshConfig.getUserName(), sshConfig.getPassword(), jvm.getHostName(), sshConfig.getPort()),
                getCommand(jvm)));
    }

    abstract ExecCommand getCommand(Jvm jvm);

}
