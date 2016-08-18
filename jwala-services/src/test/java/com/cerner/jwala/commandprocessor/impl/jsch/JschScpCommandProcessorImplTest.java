package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.impl.jsch.JschScpCommandProcessorImpl;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JschScpCommandProcessorImplTest {

    @Test
    public void testProcessCommand() throws JSchException, IOException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/jwala-services/src/test/resources");

        JSch mockJsch = mock(JSch.class);
        Session mockSession = mock(Session.class);
        ChannelExec mockChannel = mock(ChannelExec.class);
        InputStream mockRemoteOutput = mock(InputStream.class);
        InputStream mockRemoteErr = mock(InputStream.class);
        OutputStream mockLocalInput = mock(OutputStream.class);
        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(mockRemoteOutput);
        when(mockChannel.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannel.getErrStream()).thenReturn(mockRemoteErr);
        when(mockRemoteOutput.read()).thenReturn(0); // return success for checkAck
        RemoteSystemConnection remoteSystemConnection = new RemoteSystemConnection("testUser", "testPassword", "testHost", 1111);
        ExecCommand execCommand = new ExecCommand("scp src/test/resources/known_hosts destpath/testfile.txt".split(" "));
         
        RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection, execCommand);
        JschScpCommandProcessorImpl jschScpCommandProcessor = new JschScpCommandProcessorImpl(mockJsch, remoteExecCommand);
        try {
            jschScpCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschScpCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }

        // test the error output
        when(mockRemoteOutput.read()).thenReturn(1,10); //1 is error, 10 is newline character
        try {
            jschScpCommandProcessor.processCommand();
        } catch (RemoteCommandFailureException e) {
            assertTrue("This will fail because of return code 1" + e.getMessage(), true);
        }

        // test the fatal error output
        when(mockRemoteOutput.read()).thenReturn(2,10); //2 is fatal error, 10 is newline character
        try {
            jschScpCommandProcessor.processCommand();
        } catch (RemoteCommandFailureException e) {
            assertTrue("This will fail because of return code 2" + e.getMessage(), true);
        }

        // test the other non 0, 1, or 2 return codes for more complete branch coverage
        when(mockRemoteOutput.read()).thenReturn(3); //not 0, 1, or 2
        try {
            jschScpCommandProcessor.processCommand();
        } catch (RemoteCommandFailureException e) {
            assertTrue("This will fail because of return code 3" + e.getMessage(), true);
        }

        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }
}
