package com.siemens.cto.aem.control.jvm.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.domain.model.exec.ExecData;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.IntegrationTestRule;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.jvm.command.ControlJvmCommand;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoteJvmCommandExecutorImplTest {

    private RemoteJvmCommandExecutorImpl impl;
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
        impl = new RemoteJvmCommandExecutorImpl(executor,
                                                jsch,
                                                sshConfig);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void testControlJvm() throws Exception {

        final ControlJvmCommand command = mock(ControlJvmCommand.class);
        final Jvm jvm = mock(Jvm.class);
        when(command.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(jvm.getJvmName()).thenReturn("jvm-integration-1");
        when(jvm.getHostName()).thenReturn("usmlvv1cto989");

        final ExecData exec = impl.controlJvm(command,
                                              jvm);
        final String output = exec.getStandardOutput();
        final String error = exec.getStandardError();
        assumeFalse(error.contains("The requested service has already been started"));
        assertTrue(exec.getReturnCode().wasSuccessful());
    }
}
