package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.*;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.exception.NotYetReturnedException;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import com.siemens.cto.aem.exception.RemoteNotYetReturnedException;
import org.junit.Ignore;
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

@Ignore
@Deprecated
@RunWith(MockitoJUnitRunner.class)
public class JschCommandProcessorImplTest {

    @Test
    public void testProcessCommand() throws JSchException, IOException, NotYetReturnedException {

        // test exec
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
        JschCommandProcessorImpl jschCommandProcessor = new JschCommandProcessorImpl(mockJsch, remoteExecCommand);
        try {
            jschCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }

        // test shell
        ChannelShell mockChannelShell = mock(ChannelShell.class);
        when(mockChannelShell.getInputStream()).thenReturn(mockRemoteOutput);
        when(mockChannelShell.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannelShell.getExtInputStream()).thenReturn(mockRemoteErr);
        when(mockSession.openChannel("shell")).thenReturn(mockChannelShell);
        ShellCommand shellCommand = new ShellCommand("start", "jvm", "testShellCommand");
        remoteExecCommand = new RemoteExecCommand(remoteSystemConnection, shellCommand);
        jschCommandProcessor = new JschCommandProcessorImpl(mockJsch, remoteExecCommand);
        try {
            jschCommandProcessor.processCommand();
            jschCommandProcessor.getCommandOutputStr();
            jschCommandProcessor.getErrorOutputStr();
            jschCommandProcessor.close();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }

        // too soon
        when(mockChannelShell.getExitStatus()).thenReturn(-1);
        try {
            jschCommandProcessor.processCommand();
            jschCommandProcessor.getExecutionReturnCode();
        } catch(RemoteNotYetReturnedException e) {
            assertTrue("This should fail ... " + e.getMessage(), true);
        } catch (RemoteCommandFailureException e) {
            assertTrue("Should not reach this exception", false);
        }

        // test error
        when(mockSession.openChannel(anyString())).thenThrow(new JSchException("test failure branch"));
        try {
            jschCommandProcessor.processCommand();
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should fail ... " + e.getMessage(), true);
        }

    }
}