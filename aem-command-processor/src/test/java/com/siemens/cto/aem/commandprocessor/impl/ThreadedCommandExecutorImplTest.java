package com.siemens.cto.aem.commandprocessor.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionData;
import com.siemens.cto.aem.commandprocessor.domain.RemoteExecCommand;
import com.siemens.cto.aem.commandprocessor.impl.cli.LocalRuntimeCommandProcessorBuilder;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorBuilder;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
public class ThreadedCommandExecutorImplTest {

    private ExecutorService executorService;
    private ThreadedCommandExecutorImpl impl;

    @Before
    public void setup() {
        executorService = Executors.newFixedThreadPool(3);
        impl = new ThreadedCommandExecutorImpl(executorService);
    }

    @After
    public void tearDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void testSuccessfulLocalExecution() throws Exception {
        final ExecCommand command = new ExecCommand("ipconfig");
        final ExecutionData results = impl.execute(new LocalRuntimeCommandProcessorBuilder(command));

        assertTrue(results.getReturnCode().wasSuccessful());
        assertTrue(results.getStandardOutput()
                          .contains("Windows IP Configuration"));
        assertEquals("",
                     results.getStandardError());
    }

    @Test
    public void testUnsuccessfulLocalExecution() throws Exception {
        final ExecCommand command = new ExecCommand("net", "/gibberishParametersThatShouldNotWork");
        final ExecutionData results = impl.execute(new LocalRuntimeCommandProcessorBuilder(command));

        assertFalse(results.getReturnCode()
                           .wasSuccessful());
        assertTrue(results.getStandardError()
                          .contains("The syntax of this command is"));
        assertEquals("",
                     results.getStandardOutput());
    }

    @Test
    public void testSuccessfulRemoteExecution() throws Exception {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(config.getRemoteSystemConnection(),
                                                                          new ExecCommand("uname", "-a"));
        final JSch jsch = config.getBuilder().build();
        final ExecutionData results = impl.execute(new JschCommandProcessorBuilder().setJsch(jsch).setRemoteCommand(remoteExecCommand));

        assertTrue(results.getReturnCode()
                          .wasSuccessful());
        assertTrue(results.getStandardOutput()
                          .contains(config.getRemoteSystemConnection()
                                          .getHost()
                                          .toUpperCase()));
        assertTrue(results.getStandardOutput().contains("CYGWIN_NT-6.1"));
        assertEquals("",
                     results.getStandardError());
    }

    @Test
    public void testUnsuccessfulRemoteExecution() throws Exception {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(config.getRemoteSystemConnection(),
                                                                          new ExecCommand("cat", "abcdef.g.does.not.exist"));
        final JSch jsch = config.getBuilder().build();
        final ExecutionData results = impl.execute(new JschCommandProcessorBuilder().setJsch(jsch).setRemoteCommand(remoteExecCommand));

        assertFalse(results.getReturnCode()
                           .wasSuccessful());
        assertTrue(results.getStandardError()
                          .contains("No such file or directory"));
        assertEquals("",
                     results.getStandardOutput());
    }

    @Test(expected = RuntimeException.class)
    @SuppressWarnings("unchecked")
    public void testGetOnFutureWithMockedException() throws Exception {
        final Future future = mock(Future.class);
        when(future.get()).thenThrow(InterruptedException.class);
        final ThreadedCommandExecutorImpl impl = new ThreadedCommandExecutorImpl(null);
        impl.get(future);
    }
}
