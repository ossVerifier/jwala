package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.*;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        jschCommandProcessor = new JschCommandProcessorImpl(mockJsch, new RemoteExecCommand(new RemoteSystemConnection("testUser", "testPassword", "testHost", 1111),
                new ExecCommand("scp ./toc-command-processor/src/test/resources/known_hosts destpath/testfile.txt".split(" "))), null);
        try {
            jschCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
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