package com.siemens.cto.aem.commandprocessor.impl.jsch;

import com.jcraft.jsch.*;
import com.siemens.cto.aem.commandprocessor.impl.jsch.impl.spring.component.JschChannelServiceImpl;
import com.siemens.cto.aem.common.exec.*;
import com.siemens.cto.aem.exception.RemoteCommandFailureException;
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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
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

    JschChannelService jschChannelService = new JschChannelServiceImpl();

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
                new ExecCommand("scp ./toc-command-processor/src/test/resources/known_hosts destpath/testfile.txt".split(" "))), jschChannelService);
        try {
            jschCommandProcessor.processCommand();
            ExecReturnCode returnCode = jschCommandProcessor.getExecutionReturnCode();
            assertTrue(returnCode.getWasSuccessful());
        } catch (RemoteCommandFailureException e) {
            assertTrue("This should not fail ... " + e.getMessage(), false);
        }
    }

    @Test
    public void testProcessCommandShell() throws JSchException, IOException {
        ChannelShell mockChannelShell = mock(ChannelShell.class);
        when(mockChannelShell.getInputStream()).thenReturn(remoteOutputStream);
        when(mockChannelShell.getOutputStream()).thenReturn(mockLocalInput);
        when(mockChannelShell.getExtInputStream()).thenReturn(mockRemoteErr);
        when(mockSession.openChannel("shell")).thenReturn(mockChannelShell);
        when(mockChannelShell.getSession()).thenReturn(mockSession);

        jschCommandProcessor = new JschCommandProcessorImpl(mockJsch,
                new RemoteExecCommand(new RemoteSystemConnection("testUser", "testPassword", "testHost", 1111),
                new ShellCommand("start", "jvm", "testShellCommand")), jschChannelService);
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
    }

}