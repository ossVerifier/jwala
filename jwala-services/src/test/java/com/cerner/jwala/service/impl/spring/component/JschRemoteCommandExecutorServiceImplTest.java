package com.cerner.jwala.service.impl.spring.component;

import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelType;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.RemoteExecCommand;
import com.cerner.jwala.common.exec.RemoteSystemConnection;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.common.jsch.RemoteCommandReturnInfo;
import com.jcraft.jsch.*;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JschRemoteCommandExecutorServiceImpl}
 *
 * Created by Jedd Cuison on 4/18/2016
 */
public class JschRemoteCommandExecutorServiceImplTest {

    private static final String SOME_OUTPUT = "some output";
    private static final String SOME_ERR_OUTPUT = "some err output";

    private JschRemoteCommandExecutorServiceImpl jschRemoteCommandExecutorService;

    @Mock
    private JSch mockJSch;

    @Mock
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> mockChannelPool;

    @Mock
    private RemoteExecCommand mockRemoteExecCommand;

    @Mock
    private Session mockSession;

    @Mock
    private RemoteSystemConnection mockRemoteSystemConnection;

    @Mock
    private ChannelExec mockChannelExec;

    @Mock
    private ExecCommand mockExecCommand;

    @Mock
    private JschService mockJschService;

    final RemoteSystemConnection remoteSystemConnection = new RemoteSystemConnection("theUser", "==theEncryptedPassword==".toCharArray(), "theHost", 999);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.jschRemoteCommandExecutorService = new JschRemoteCommandExecutorServiceImpl(mockJSch, mockChannelPool, mockJschService);
    }

    @Test
    public void testExecuteShellCommand() throws Exception {
        final ChannelShell mockChannelShell = mock(ChannelShell.class);
        final byte [] bytes = "EXIT_CODE=0*** ".getBytes();
        bytes[14] = (byte) 0xff;

        when(mockChannelShell.isConnected()).thenReturn(true);
        when(mockChannelPool.borrowObject(any(ChannelSessionKey.class))).thenReturn(mockChannelShell);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(remoteSystemConnection);
        final ExecCommand mockExecCommand = mock(ExecCommand.class);
        when(mockExecCommand.toCommandString()).thenReturn("some-command.sh");
        when(mockExecCommand.getRunInShell()).thenReturn(true);
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        when(mockJschService.runCommand(eq("some-command.sh"), eq(mockChannelShell), anyLong())).thenReturn(new RemoteCommandReturnInfo(0, "EXIT_CODE=0*** ", null));
        final RemoteCommandReturnInfo remoteCommandReturnInfo = jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);
        assertEquals(0, remoteCommandReturnInfo.retCode);
    }

    @Test
    public void testExecuteExecCommand() throws Exception {
        when(mockJSch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(mockRemoteSystemConnection);
        when(mockSession.openChannel(eq(ChannelType.EXEC.getChannelType()))).thenReturn(mockChannelExec);
        when(mockChannelExec.getExitStatus()).thenReturn(0);
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        when(mockExecCommand.toCommandString()).thenReturn("sc query something");
        when(mockJschService.runCommand(eq("sc query something"), eq(mockChannelExec), anyLong())).thenReturn(new RemoteCommandReturnInfo(0, SOME_OUTPUT, null));
        final RemoteCommandReturnInfo returnInfo = this.jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);
        assertEquals("some output", returnInfo.standardOuput);
    }

    @Test
    public void testExecuteExecCommandWithPassword() throws Exception {
        when(mockJSch.getSession(anyString(), anyString(), anyInt())).thenReturn(mockSession);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(mockRemoteSystemConnection);
        when(mockSession.openChannel(eq(ChannelType.EXEC.getChannelType()))).thenReturn(mockChannelExec);
        when(mockRemoteSystemConnection.getEncryptedPassword()).thenReturn("==test==".toCharArray());
        when(mockChannelExec.getExitStatus()).thenReturn(0);
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        when(mockExecCommand.toCommandString()).thenReturn("sc query something");
        when(mockJschService.runCommand(eq("sc query something"), eq(mockChannelExec), anyLong())).thenReturn(
                new RemoteCommandReturnInfo(0, SOME_OUTPUT, SOME_ERR_OUTPUT));
        final RemoteCommandReturnInfo returnInfo = this.jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);
        assertEquals("some output", returnInfo.standardOuput);
        assertEquals("some err output", returnInfo.errorOupout);
    }

}
