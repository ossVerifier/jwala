package com.siemens.cto.aem.control.jvm.impl;

import com.siemens.cto.aem.request.jvm.ControlJvmRequest;
import com.siemens.cto.aem.commandprocessor.CommandExecutor;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import com.siemens.cto.aem.commandprocessor.impl.ThreadedCommandExecutorImpl;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschBuilder;
import com.siemens.cto.aem.common.IntegrationTestRule;
import com.siemens.cto.aem.exec.CommandOutput;
import com.siemens.cto.aem.domain.model.jvm.Jvm;
import com.siemens.cto.aem.domain.model.jvm.JvmControlOperation;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import org.junit.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
// TODO: Make this work locally
public class RemoteJvmRequestExecutorImplTest {

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
        final JschBuilder jschBuilder = new JschBuilder().setPrivateKeyFileName(sshConfig.getPrivateKeyFile())
                                                         .setKnownHostsFileName(sshConfig.getKnownHostsFile());
        impl = new RemoteJvmCommandExecutorImpl(executor,
                                                jschBuilder,
                                                sshConfig);
    }

    @After
    public void tearDown() throws Exception {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void testControlJvm() throws Exception {

        final ControlJvmRequest command = mock(ControlJvmRequest.class);
        final Jvm jvm = mock(Jvm.class);
        when(command.getControlOperation()).thenReturn(JvmControlOperation.START);
        when(jvm.getJvmName()).thenReturn("jvm-integration-1");
        when(jvm.getHostName()).thenReturn("usmlvv1cto989");

        final CommandOutput exec = impl.controlJvm(command,
                                              jvm);
        final String output = exec.getStandardOutput();
        final String error = exec.getStandardError();
        assumeFalse(error.contains("The requested service has already been started"));
        assertTrue(exec.getReturnCode().wasSuccessful());
    }
}
