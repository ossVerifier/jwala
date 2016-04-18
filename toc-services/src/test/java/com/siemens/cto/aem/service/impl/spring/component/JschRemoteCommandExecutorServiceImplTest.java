package com.siemens.cto.aem.service.impl.spring.component;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.siemens.cto.aem.commandprocessor.jsch.impl.ChannelSessionKey;
import com.siemens.cto.aem.common.exec.ExecCommand;
import com.siemens.cto.aem.common.exec.RemoteExecCommand;
import com.siemens.cto.aem.common.exec.RemoteSystemConnection;
import com.siemens.cto.aem.service.RemoteCommandReturnInfo;
import netscape.javascript.JSException;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by JC043760 on 4/18/2016.
 */
public class JschRemoteCommandExecutorServiceImplTest {

    private JschRemoteCommandExecutorServiceImpl jschRemoteCommandExecutorService;

    @Mock
    private GenericKeyedObjectPool<ChannelSessionKey, Channel> mockChannelPool;

    @Mock
    private RemoteExecCommand mockRemoteExecCommand;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.jschRemoteCommandExecutorService = new JschRemoteCommandExecutorServiceImpl(mockChannelPool);
    }

    @Test
    public void testExecuteCommand() throws Exception {
        final RemoteSystemConnection remoteSystemConnection = new RemoteSystemConnection("theUser", "thePassword", "theHost", 999);
        final ChannelShell mockChannelShell = mock(ChannelShell.class);

        final byte [] bytes = "EXIT_CODE=0*** ".getBytes();
        bytes[14] = (byte) 0xff;
        when(mockChannelShell.getInputStream()).thenReturn(new ByteArrayInputStream(bytes));
        when(mockChannelShell.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockChannelShell.isConnected()).thenReturn(true);
        when(mockChannelPool.borrowObject(any(ChannelSessionKey.class))).thenReturn(mockChannelShell);
        when(mockRemoteExecCommand.getRemoteSystemConnection()).thenReturn(remoteSystemConnection);
        final ExecCommand mockExecCommand = mock(ExecCommand.class);
        when(mockExecCommand.toCommandString()).thenReturn("some-command.sh");
        when(mockRemoteExecCommand.getCommand()).thenReturn(mockExecCommand);
        final RemoteCommandReturnInfo remoteCommandReturnInfo = jschRemoteCommandExecutorService.executeCommand(mockRemoteExecCommand);
        assertEquals(0, remoteCommandReturnInfo.retCode);
    }

}
