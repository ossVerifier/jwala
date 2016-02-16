package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.siemens.cto.aem.commandprocessor.SimpleCommandProcessor;
import com.siemens.cto.aem.commandprocessor.impl.CommonSshTestConfiguration;
import com.siemens.cto.aem.commandprocessor.impl.SimpleCommandProcessorImpl;
import com.siemens.cto.aem.common.IntegrationTestRule;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
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
// TODO: Make ssh server should be self contained or permanent. The server that this test connects to changes from time to time thus it fails on occasions.
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
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection, new ExecCommand("uname", "-a"));
        final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand);
        sshProcessor.processCommand();
        final SimpleCommandProcessor processor = new SimpleCommandProcessorImpl(sshProcessor);
        assertTrue(processor.getCommandOutput().contains("CYGWIN_NT"));
    }

    @Test
    public void testGetErrorOutput() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection,
                                                                          new ExecCommand("cat", "abcdef.g.should.not.exist"));
        final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand);
        sshProcessor.processCommand();
        final SimpleCommandProcessor processor = new SimpleCommandProcessorImpl(sshProcessor);
        final String remoteOutput = processor.getCommandOutput();
        final String remoteErrorOutput = processor.getErrorOutput();
        assertEquals("", remoteOutput);
        assertTrue(remoteErrorOutput.contains("No such file or directory"));
    }

    @Test(expected = RemoteNotYetReturnedException.class)
    public void testGetReturnCodeBeforeFinishing() throws Exception {
        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection, new ExecCommand("vi"));
        final JschCommandProcessorImpl sshProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand);
        sshProcessor.processCommand();
        final ExecReturnCode returnCode = sshProcessor.getExecutionReturnCode();
    }

    @Test(expected = RemoteCommandFailureException.class)
    public void testBadRemoteCommand() throws Exception {
        final RemoteExecCommand remoteExecCommand =
                new RemoteExecCommand(new RemoteSystemConnection("abc", "123546", "example.com", 123456), new ExecCommand("vi"));
        final JschCommandProcessorImpl jschCommandProcessor = new JschCommandProcessorImpl(builder.build(), remoteExecCommand);
        jschCommandProcessor.processCommand();
    }

}
