package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.control.command.RuntimeCommandBuilder;
import com.siemens.cto.aem.domain.command.exec.RuntimeCommand;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.Times;

import static com.siemens.cto.aem.control.AemControl.Properties.SCP_WITH_TARGET_BK;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by z003bpej on 9/30/2015.
 */
public class ApplicationCommandServiceImplTest {

    private ApplicationCommandService applicationCommandService;
    private SshConfiguration sshConfiguration;
    private RuntimeCommandBuilder runtimeCommandBuilder;
    private RuntimeCommand runtimeCommand;

    @Before
    public void setup() {
        sshConfiguration = mock(SshConfiguration.class);
        applicationCommandService = new ApplicationCommandServiceImpl(sshConfiguration);
    }

    @Test
    public void testSecureCopyConfFile() throws CommandFailureException {
        runtimeCommandBuilder = mock(RuntimeCommandBuilder.class);
        runtimeCommand = mock(RuntimeCommand.class);
        when(runtimeCommandBuilder.build()).thenReturn(runtimeCommand);
        applicationCommandService.secureCopyConfFile("host", "src", "conf", runtimeCommandBuilder);
        verify(runtimeCommandBuilder).setOperation(SCP_WITH_TARGET_BK);
        verify(runtimeCommandBuilder, new Times(4)).addParameter(anyString());
        verify(sshConfiguration).getUserName();
    }

}
