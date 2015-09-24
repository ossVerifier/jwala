package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import static com.siemens.cto.aem.control.AemControl.Properties.SCP_WITH_TARGET_BK;

/**
 * An implementation of ApplicationCommandService.
 *
 * Created by z003bpej on 9/9/2015.
 */
public class ApplicationCommandServiceImpl implements ApplicationCommandService {

    private final SshConfiguration sshConfig;

    public ApplicationCommandServiceImpl(final SshConfiguration sshConfig) {
        this.sshConfig = sshConfig;
    }

    @Override
    public ExecData secureCopyConfFile(final String host, final String sourcePath, final String appConfPath,
                                       final RuntimeCommandBuilder rtCommandBuilder) throws CommandFailureException {
        rtCommandBuilder.setOperation(SCP_WITH_TARGET_BK);
        rtCommandBuilder.addParameter(sourcePath);
        rtCommandBuilder.addParameter(sshConfig.getUserName());
        rtCommandBuilder.addParameter(host);
        rtCommandBuilder.addParameter(appConfPath);
        RuntimeCommand rtCommand = rtCommandBuilder.build();
        return rtCommand.execute();
    }

}
