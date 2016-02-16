package com.siemens.cto.aem.commandprocessor.impl;

import com.jcraft.jsch.JSch;
import com.siemens.cto.aem.commandprocessor.impl.jsch.JschCommandProcessorBuilder;
import com.siemens.cto.aem.common.IntegrationTestRule;
import com.siemens.cto.aem.common.exec.CommandOutput;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import org.junit.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
// TODO: Make ssh server should be self contained or permanent. The server that this test connects to changes from time to time thus it fails on occasions.
public class ThreadedRequestExecutorImplTest {

    @ClassRule
    public static IntegrationTestRule integrationTest = new IntegrationTestRule();

    private ExecutorService executorService;
    private ThreadedCommandExecutorImpl impl;

    @Before
    public void setup() {
        executorService = Executors.newFixedThreadPool(3);
        impl = new ThreadedCommandExecutorImpl();
    }

    @After
    public void tearDown() {
        executorService.shutdown();
        executorService.shutdownNow();
    }

    @Test
    public void testSuccessfulRemoteExecution() throws Exception {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(config.getRemoteSystemConnection(),
                                                                          new ExecCommand("uname", "-a"));
        final JSch jsch = config.getBuilder().build();
        final CommandOutput results = impl.execute(new JschCommandProcessorBuilder().setJsch(jsch).setRemoteCommand(remoteExecCommand));

        assertTrue(results.getReturnCode().wasSuccessful());
        assertTrue(results.getStandardOutput().contains(config.getRemoteSystemConnection()
                                                              .getHost()
                                                              .toUpperCase()));
        assertTrue(results.getStandardOutput().contains("CYGWIN_NT"));
        assertEquals("",
                     results.getStandardError());
    }

    @Test
    public void testUnsuccessfulRemoteExecution() throws Exception {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(config.getRemoteSystemConnection(),
                                                                          new ExecCommand("cat", "abcdef.g.does.not.exist"));
        final JSch jsch = config.getBuilder().build();
        final CommandOutput results = impl.execute(new JschCommandProcessorBuilder().setJsch(jsch).setRemoteCommand(remoteExecCommand));

        assertFalse(results.getReturnCode().wasSuccessful());
        assertTrue(results.getStandardError().contains("No such file or directory"));
        assertEquals("",
                     results.getStandardOutput());
    }

}
