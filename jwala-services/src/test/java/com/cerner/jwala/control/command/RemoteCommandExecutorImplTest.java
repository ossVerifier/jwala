package com.cerner.jwala.control.command;

import com.cerner.jwala.commandprocessor.CommandProcessorBuilder;
import com.cerner.jwala.commandprocessor.impl.CommandExecutorImpl;
import com.cerner.jwala.commandprocessor.impl.jsch.JschBuilder;
import com.cerner.jwala.commandprocessor.jsch.impl.ChannelSessionKey;
import com.cerner.jwala.common.domain.model.jvm.JvmControlOperation;
import com.cerner.jwala.common.domain.model.ssh.SshConfiguration;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.jsch.JschService;
import com.cerner.jwala.exception.CommandFailureException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(MockitoJUnitRunner.class)
public class RemoteCommandExecutorImplTest {
    private RemoteCommandExecutor remoteCommandExecutor;

    @Mock
    CommandExecutorImpl mockCommandExecutor;

    @Mock
    JschBuilder mockJschBuilder;

    @Mock
    SshConfiguration mockSshConfiguration;

    @Mock
    GenericKeyedObjectPool<ChannelSessionKey, Channel> mockChannelPool;

    @Mock
    private JschService mockJschService;

    @Before
    public void setup() {
        initMocks(this);
        remoteCommandExecutor = new RemoteCommandExecutorImpl(mockCommandExecutor, mockJschBuilder, mockSshConfiguration,
                mockChannelPool, mockJschService);
    }

    @Test
    public void testExecuteRemoteCommand() throws CommandFailureException {
        final String entityName = "testEntity";
        final String entityHost = "testHost";
        final String[] params = new String[0];
        final JvmControlOperation jvmControlOperation = com.cerner.jwala.common.domain.model.jvm.JvmControlOperation.CHANGE_FILE_MODE;
        PlatformCommandProvider platformCommandProvider = mock(PlatformCommandProvider.class);
        ServiceCommandBuilder serviceCommandBuilder = mock(ServiceCommandBuilder.class);
        when(platformCommandProvider.getServiceCommandBuilderFor(any(jvmControlOperation.getClass()))).thenReturn(serviceCommandBuilder);
        ExecCommand execCommand = mock(ExecCommand.class);
        when(serviceCommandBuilder.buildCommandForService(eq(entityName), eq(params))).thenReturn(execCommand);
        CommandOutput commandOutput = mock(CommandOutput.class);
        when(mockCommandExecutor.execute(any(CommandProcessorBuilder.class))).thenReturn(commandOutput);
        assertEquals(commandOutput, remoteCommandExecutor.executeRemoteCommand(entityName, entityHost, jvmControlOperation, platformCommandProvider, params));
    }

    @Test (expected = CommandFailureException.class)
    public void testExecuteRemoteCommandFail() throws CommandFailureException {
        final String entityName = "testEntity";
        final String entityHost = "testHost";
        final String[] params = new String[0];
        final JvmControlOperation jvmControlOperation = com.cerner.jwala.common.domain.model.jvm.JvmControlOperation.CHANGE_FILE_MODE;
        PlatformCommandProvider platformCommandProvider = mock(PlatformCommandProvider.class);
        ServiceCommandBuilder serviceCommandBuilder = mock(ServiceCommandBuilder.class);
        when(platformCommandProvider.getServiceCommandBuilderFor(any(jvmControlOperation.getClass()))).thenReturn(serviceCommandBuilder);
        ExecCommand execCommand = mock(ExecCommand.class);
        when(serviceCommandBuilder.buildCommandForService(eq(entityName), eq(params))).thenReturn(execCommand);
        CommandOutput commandOutput = mock(CommandOutput.class);
        when(mockCommandExecutor.execute(any(CommandProcessorBuilder.class))).thenThrow(JSchException.class);
        assertEquals(commandOutput, remoteCommandExecutor.executeRemoteCommand(entityName, entityHost, jvmControlOperation, platformCommandProvider, params));
    }
}
