package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.app.ApplicationControlOperation;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.common.exec.RuntimeCommand;
import com.siemens.cto.aem.common.request.app.ControlApplicationRequest;
import com.siemens.cto.aem.exception.CommandFailureException;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by z003bpej on 9/30/2015.
 */
public class ApplicationRequestServiceImplTest {

    private ApplicationCommandService applicationCommandService;
    private SshConfiguration sshConfiguration;
    private RuntimeCommand runtimeCommand;
    private JschBuilder jschBuilder;

    @Before
    public void setup() {
        sshConfiguration = mock(SshConfiguration.class);
        jschBuilder = mock(JschBuilder.class);
        applicationCommandService = new ApplicationCommandServiceImpl(sshConfiguration, jschBuilder);
    }

    // TODO fix this - should not expect a NullPointer
    @Test(expected = NullPointerException.class)
    public void testSecureCopyConfFile() throws CommandFailureException{
        final Application mockApp = mock(Application.class);
        final Identifier<Application> appId = new Identifier<>(11L);
        when(mockApp.getId()).thenReturn(appId);
        when(sshConfiguration.getUserName()).thenReturn("user");
        when(sshConfiguration.getPassword()).thenReturn("oops");
        when(sshConfiguration.getPort()).thenReturn(22);
        ControlApplicationRequest appRequest = new ControlApplicationRequest(appId, ApplicationControlOperation.DEPLOY_CONFIG_FILE);
        applicationCommandService.controlApplication(appRequest, mockApp, "testHost", "source path", "dest path");
    }

}
