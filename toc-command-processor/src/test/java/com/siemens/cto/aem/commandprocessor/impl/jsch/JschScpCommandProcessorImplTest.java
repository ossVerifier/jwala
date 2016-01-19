package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.ExecReturnCode;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void testProcessCommand() throws JSchException, IOException, NotYetReturnedException {
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
        ExecCommand execCommand = new ExecCommand("scp ./toc-command-processor/src/test/resources/known_hosts destpath/testfile.txt".split(" "));
        RemoteExecCommand remoteExecCommand = new RemoteExecCommand(remoteSystemConnection, execCommand);
        JschScpCommandProcessorImpl jschScpCommandProcessor = new JschScpCommandProcessorImpl(mockJsch, remoteExecCommand);
        try {
            jschScpCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschScpCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }
    }
}
