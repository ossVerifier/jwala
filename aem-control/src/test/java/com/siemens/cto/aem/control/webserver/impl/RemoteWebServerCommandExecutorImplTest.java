package com.siemens.cto.aem.control.webserver.impl;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.IntegrationTestRule;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.domain.model.webserver.WebServer;
import com.siemens.cto.aem.domain.model.webserver.WebServerControlOperation;
import com.siemens.cto.aem.domain.model.webserver.command.ControlWebServerCommand;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public class RemoteWebServerCommandExecutorImplTest {

    private RemoteWebServerCommandExecutorImpl impl;
    private ExecutorService executorService;

    @ClassRule
    public static IntegrationTestRule integration = new IntegrationTestRule();

    @Before
    public void setup() throws Exception {

        executorService = Executors.newFixedThreadPool(3);
        final CommonSshTestConfiguration testConfiguration = new CommonSshTestConfiguration();
        final CommandExecutor executor = new ThreadedCommandExecutorImpl(executorService);
        final SshConfiguration sshConfig = new SshConfiguration(testConfiguration.getRemoteSystemConnection().getUser(),
                                                                testConfiguration.getRemoteSystemConnection().getPort(),
                                                                testConfiguration.getPrivateKey(),
                                                                testConfiguration.getKnownHostsFile());
        final JSch jsch = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                                           .setKnownHostsFileName(sshConfig.getKnownHostsFile()).build();
        impl = new RemoteWebServerCommandExecutorImpl(executor,
                                                jsch,
                                                sshConfig);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void testControlWebServer() throws Exception {
        final ControlWebServerCommand command = mock(ControlWebServerCommand.class);
        final WebServer webServer = mock(WebServer.class);
        when(command.getControlOperation()).thenReturn(WebServerControlOperation.START);
        when(webServer.getName()).thenReturn("Apache2.4");
        when(webServer.getHost()).thenReturn("usmlvv1cto989");

        final ExecData exec = impl.controlWebServer(command, webServer);
        final String output = exec.getStandardOutput();
        final String error = exec.getStandardError();
        assumeFalse(error.contains("The requested service has already been started"));

        System.out.println(">>> " + exec.getReturnCode());

        assertTrue(exec.getReturnCode().wasSuccessful());
    }
}
