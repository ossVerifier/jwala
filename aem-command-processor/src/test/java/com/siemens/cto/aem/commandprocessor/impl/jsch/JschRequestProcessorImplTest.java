package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.siemens.cto.aem.commandprocessor.SimpleCommandProcessor;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import com.siemens.cto.aem.commandprocessor.impl.SimpleCommandProcessorImpl;
import com.siemens.cto.aem.common.IntegrationTestRule;
import com.siemens.cto.aem.exec.ExecCommand;
import com.siemens.cto.aem.exec.ExecReturnCode;
import com.siemens.cto.aem.exec.RemoteExecCommand;
import com.siemens.cto.aem.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.exception.RemoteNotYetReturnedException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

@Ignore
// TODO: Make this work locally
public class JschRequestProcessorImplTest {

    @ClassRule
    public static IntegrationTestRule integrationTestRule = new IntegrationTestRule();

    private JschBuilder builder;
    private RemoteSystemConnection remoteSystemConnection;

    @Before
    public void setup() {
        final CommonSshTestConfiguration config = new CommonSshTestConfiguration();
        builder = config.getBuilder();
        remoteSystemConnection = config.getRemoteSystemConnection();
    }

    @Test
    public void testGetCommandOutput() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection,
                                                                          new ExecCommand("uname", "-a"));

        try (final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand)) {
            final SimpleCommandProcessor processor = new SimpleCommandProcessorImpl(sshProcessor);
            final String remoteOutput = processor.getCommandOutput();

            assertTrue(remoteOutput.contains(remoteSystemConnection.getHost()
                                                                   .toUpperCase()));
            assertTrue(remoteOutput.contains("CYGWIN_NT-6.1"));
        }
    }

    @Test
    public void testGetErrorOutput() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection,
                                                                          new ExecCommand("cat", "abcdef.g.should.not.exist"));

        try (final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand)) {
            final SimpleCommandProcessor processor = new SimpleCommandProcessorImpl(sshProcessor);
            final String remoteOutput = processor.getCommandOutput();
            final String remoteErrorOutput = processor.getErrorOutput();

            assertEquals("",
                         remoteOutput);
            assertTrue(remoteErrorOutput.contains("No such file or directory"));
        }
    }

    @Test
    public void testGetCommandInput() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection,
                                                                          new ExecCommand("vi"));
        try (final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand)) {
            final OutputStream outputStream = sshProcessor.getCommandInput();
            outputStream.write(":q\n".getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    @Test(expected = RemoteNotYetReturnedException.class)
    public void testGetReturnCodeBeforeFinishing() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection,
                                                                          new ExecCommand("vi"));
        try (final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand)) {

            final ExecReturnCode returnCode = sshProcessor.getExecutionReturnCode();
        }
    }

    @Test(expected = RemoteCommandFailureException.class)
    public void testBadRemoteCommand() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(new RemoteSystemConnection("abc",
                                                                                                     "example.com",
                                                                                                     123456),
                                                                          new ExecCommand("vi"));
        try (final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand)) {
            fail("RemoteCommandFailureException expected");
        }
    }
}
