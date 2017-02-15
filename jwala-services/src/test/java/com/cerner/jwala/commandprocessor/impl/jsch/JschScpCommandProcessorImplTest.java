package com.cerner.jwala.commandprocessor.impl.jsch;

import com.cerner.jwala.commandprocessor.CommandProcessor;
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
import org.mockito.stubbing.OngoingStubbing;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class JschScpCommandProcessorImplTest {

    @Mock
    private JSch mockJsch;

    @Mock
    private RemoteExecCommand mockRemoteExecCommand;

    private CommandProcessor jschScpCommandProcessor;

    private static final String PROPERTIES_ROOT_PATH = "PROPERTIES_ROOT_PATH";
    private String resourceDir;

    public JschScpCommandProcessorImplTest() {
        resourceDir = this.getClass().getClassLoader().getResource("vars.properties").getPath();
        resourceDir = resourceDir.substring(0, resourceDir.lastIndexOf("/"));
    }

    @Before
    public void setup() {
        System.setProperty(PROPERTIES_ROOT_PATH, resourceDir);
        initMocks(this);
        jschScpCommandProcessor = new JschScpCommandProcessorImpl(mockJsch, mockRemoteExecCommand);
    }

    @After
    public void tearDown() {
        System.clearProperty(PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testProcessCommand() throws Exception {
        final ExecCommand command = new ExecCommand("frag1", this.getClass().getClassLoader().getResource("jsch-scp.txt").getPath(), "frag3");
        final RemoteSystemConnection mockRemoteSystemConnection = mock(RemoteSystemConnection.class);
        when(mockRemoteExecCommand.getCommand()).thenReturn(command);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(mockRemoteSystemConnection);
        when(mockRemoteSystemConnection.getEncryptedPassword()).thenReturn("#$@%aaa==".toCharArray());
        final Session mockSession = mock(Session.class);
        final ChannelExec mockChannelExec = mock(ChannelExec.class);
        when(mockChannelExec.getOutputStream()).thenReturn(mock(OutputStream.class));
        when(mockChannelExec.getInputStream()).thenReturn(new AckIn());
        when(mockSession.openChannel(eq("exec"))).thenReturn(mockChannelExec);
        when(mockJsch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        jschScpCommandProcessor.processCommand();
    }

    static class AckIn extends InputStream {

        @Override
        public int available() throws IOException {
            return 1;
        }

        @Override
        public int read() throws IOException {
            return 0;
        }

    }

}

