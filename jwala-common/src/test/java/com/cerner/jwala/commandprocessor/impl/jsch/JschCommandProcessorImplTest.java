package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.exec.*;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.exception.RemoteCommandFailureException;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JschCommandProcessorImplTest {
    private InputStream remoteOutputStream;

    @Mock
    private JSch mockJsch;

    @Mock
    private Session mockSession;

    @Mock
    private ChannelExec mockChannel;

    @Mock
    private ChannelShell mockChannelShell;

    @Mock
    InputStream mockRemoteErr;

    @Mock
    OutputStream mockLocalInput;

    @Mock
    GenericKeyedObjectPool<ChannelSessionKey, Channel> mockChannelPool;

    JschCommandProcessorImpl jschCommandProcessor;

    @Before
    public void setup() throws JSchException {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");

        remoteOutputStream = new InputStream() {
            private int i = 0;
            byte [] bytes = "Blah blah...EXIT_CODE=0***".getBytes();

            @Override
            public int read() throws IOException {
                if (i < bytes.length) {
                    return bytes[i++];
                }
                return 0xff;
            }
        };
        MockitoAnnotations.initMocks(this);

        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
    }

    @Test
    public void testProcessCommandExec() throws JSchException, IOException {
        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(remoteOutputStream);
        when(mockChannel.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannel.getErrStream()).thenReturn(mockRemoteErr);
        when(mockChannel.isClosed()).thenReturn(true);
        when(mockChannel.getExitStatus()).thenReturn(0);
        jschCommandProcessor = new JschCommandProcessorImpl(mockJsch, new RemoteExecCommand(new RemoteSystemConnection("testUser", "testPassword", "testHost", 1111),
                new ExecCommand("scp ./jwala-services/src/test/resources/known_hosts destpath/testfile.txt".split(" "))), null);
        try {
            jschCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }
    }

    @Test
    public void testProcessCommandExecForNegativeExitStatus() throws JSchException, IOException {
        String restorePropertiesRootPath = System.getProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources/properties/jsch");

        when(mockSession.openChannel("exec")).thenReturn(mockChannel);
        when(mockChannel.getInputStream()).thenReturn(remoteOutputStream);
        when(mockChannel.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannel.getErrStream()).thenReturn(mockRemoteErr);
        when(mockChannel.isClosed()).thenReturn(false);
        when(mockChannel.getExitStatus()).thenReturn(-1);
        jschCommandProcessor = new JschCommandProcessorImpl(mockJsch, new RemoteExecCommand(new RemoteSystemConnection("testUser", "testPassword", "testHost", 1111),
                new ExecCommand("scp ./jwala-services/src/test/resources/known_hosts destpath/testfile.txt".split(" "))), null);
        try {
            jschCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertFalse(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        } finally {
            System.setProperty(ApplicationProperties.PROPERTIES_FILE_NAME, restorePropertiesRootPath);
        }
    }

    @Test
    public void testProcessCommandShell() throws Exception {
        ChannelShell mockChannelShell = mock(ChannelShell.class);
        when(mockChannelShell.getInputStream()).thenReturn(remoteOutputStream);
        when(mockChannelShell.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannelShell.getExtInputStream()).thenReturn(mockRemoteErr);
        when(mockChannelShell.getSession()).thenReturn(mockSession);
        when(mockChannelPool.borrowObject(any(ChannelSessionKey.class))).thenReturn(mockChannelShell);
        when(mockChannelShell.isConnected()).thenReturn(true);
        jschCommandProcessor = new JschCommandProcessorImpl(mockJsch,
                new RemoteExecCommand(new RemoteSystemConnection("testUser", "testPassword", "testHost", 1111),
                new ShellCommand("start", "jvm", "testShellCommand")), mockChannelPool);
        try {
            jschCommandProcessor.processCommand();
            assertTrue(jschCommandProcessor.getExecutionReturnCode().getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }
    }

}