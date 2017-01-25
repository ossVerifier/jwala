package com.cerner.jwala.commandprocessor.impl.jsch;

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
import org.apache.commons.lang3.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class JschScpCommandProcessorImplTest {

    @Mock
    private JSch mockJsch;

    @Mock
    private Session mockSession;

    @Mock
    private ChannelExec mockChannel;

    @Mock
    private InputStream mockRemoteErr;

    @Mock
    private OutputStream mockLocalInput;

    JschScpCommandProcessorImpl jschScpCommandProcessor;


    @Before
    public void setup() throws JSchException, IOException {
        initMocks(this);

        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannel.getErrStream()).thenReturn(mockRemoteErr);

        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");

        final RemoteExecCommand remoteExecCommand = new RemoteExecCommand(
                new RemoteSystemConnection("testUser", "==encryptedTestPassword==".toCharArray(), "testHost", 1111),
                new ExecCommand("scp src/test/resources/known_hosts destpath/testfile.txt".split(" ")));

        jschScpCommandProcessor = new JschScpCommandProcessorImpl(mockJsch, remoteExecCommand);
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testProcessCommand() throws IOException {
        final byte [] remoteOuputBytes = {0, 0, 0}; // processCommand happy path gets ack 3x
        InputStream remoteOutput = new ByteArrayInputStream(remoteOuputBytes);
        when(mockChannel.getInputStream()).thenReturn(remoteOutput);

        jschScpCommandProcessor.processCommand();
        ExecReturnCode returnCode = jschScpCommandProcessor.getExecutionReturnCode();
        assertTrue(returnCode.getWasSuccessful());
    }

    @Test
    public void testProcessCommandWithErrorCode1()  throws JSchException, IOException {
        final byte [] remoteOuputBytes = {1};
        final byte [] msg = "Error code 1\n".getBytes();
        InputStream remoteOutput = new ByteArrayInputStream(ArrayUtils.addAll(remoteOuputBytes, msg));
        when(mockChannel.getInputStream()).thenReturn(remoteOutput);
        try {
            jschScpCommandProcessor.processCommand();
        } catch (final RemoteCommandFailureException e) {
            assertEquals("java.io.IOException: ERROR in SCP: Error code 1\n", e.getMessage());
        }
    }

    @Test
    public void testProcessCommandWithErrorCode2()  throws JSchException, IOException {
        final byte [] remoteOuputBytes = {2};
        final byte [] msg = "Error code 2\n".getBytes();
        InputStream remoteOutput = new ByteArrayInputStream(ArrayUtils.addAll(remoteOuputBytes, msg));
        when(mockChannel.getInputStream()).thenReturn(remoteOutput);
        try {
            jschScpCommandProcessor.processCommand();
        } catch (final RemoteCommandFailureException e) {
            assertEquals("java.io.IOException: FATAL ERROR in SCP: Error code 2\n", e.getMessage());
        }
    }

    @Test
    public void testProcessCommandWithErrorCode3()  throws JSchException, IOException {
        final byte [] remoteOuputBytes = {3};
        InputStream remoteOutput = new ByteArrayInputStream(remoteOuputBytes);
        when(mockChannel.getInputStream()).thenReturn(remoteOutput);
        try {
            jschScpCommandProcessor.processCommand();
        } catch (final RemoteCommandFailureException e) {
            assertEquals("com.cerner.jwala.exception.RemoteCommandFailureException: java.lang.Throwable: Failed to connect to the remote host during secure copy", e.getMessage());
        }
    }

}

