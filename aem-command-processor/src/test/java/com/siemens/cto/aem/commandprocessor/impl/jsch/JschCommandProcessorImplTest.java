package com.siemens.cto.aem.commandprocessor.impl.jsch;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.siemens.cto.aem.commandprocessor.SimpleCommandProcessor;
import com.siemens.cto.aem.commandprocessor.domain.ExecCommand;
import com.siemens.cto.aem.commandprocessor.domain.ExecutionReturnCode;
import com.siemens.cto.aem.commandprocessor.domain.RemoteExecCommand;
import com.siemens.cto.aem.commandprocessor.domain.RemoteNotYetReturnedException;
import com.siemens.cto.aem.commandprocessor.domain.RemoteSystemConnection;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import com.siemens.cto.aem.commandprocessor.impl.SimpleCommandProcessorImpl;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
public class JschCommandProcessorImplTest {

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

            final ExecutionReturnCode returnCode = sshProcessor.getExecutionReturnCode();
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
