package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.binarydistribution.command.impl.WindowsBinaryDistributionPlatformCommandProvider;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionControlServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BinaryDistributionControlServiceImplTest {

    @Mock
    private RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor;

    private BinaryDistributionControlServiceImpl binaryDistributionControlServiceImpl;

    @Before
    public void setup() {
        binaryDistributionControlServiceImpl = new BinaryDistributionControlServiceImpl(remoteCommandExecutor);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testSecureCopyFile() {
        final String hostname = "localhost";
        final String source = "/src/test/resources/binarydistribution/copy.txt";
        final String destination = "/build/tmp/";
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(BinaryDistributionControlOperation.SECURE_COPY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), anyString(), anyString())).thenReturn(execData);
            binaryDistributionControlServiceImpl.secureCopyFile(hostname, source, destination);
            verify(remoteCommandExecutor).executeRemoteCommand(anyString(), eq(hostname), eq(BinaryDistributionControlOperation.SECURE_COPY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), eq(source), eq(destination));
        } catch (CommandFailureException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCreateDirectory() {
        final String hostname = "localhost";
        final String destination = "/build/tmp/";
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(BinaryDistributionControlOperation.CREATE_DIRECTORY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), anyString())).thenReturn(execData);
            binaryDistributionControlServiceImpl.createDirectory(hostname, destination);
            verify(remoteCommandExecutor).executeRemoteCommand(anyString(), eq(hostname), eq(BinaryDistributionControlOperation.CREATE_DIRECTORY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), eq(destination));
        } catch (CommandFailureException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCheckFileExists() {
        final String hostname = "localhost";
        final String destination = "/build/tmp/";
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(BinaryDistributionControlOperation.CHECK_FILE_EXISTS),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), anyString())).thenReturn(execData);
            binaryDistributionControlServiceImpl.checkFileExists(hostname, destination);
            verify(remoteCommandExecutor).executeRemoteCommand(anyString(), eq(hostname), eq(BinaryDistributionControlOperation.CHECK_FILE_EXISTS),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), eq(destination));
        } catch (CommandFailureException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUnzipBinary() {
        final String hostname = "localhost";
        final String binaryLocation = "/src/test/resources/binarydistribution/copy.txt";
        final String destination = "/build/tmp/";
        final String zipPath = "";
        final String exclude = "";
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(BinaryDistributionControlOperation.UNZIP_BINARY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), anyString(), anyString(), anyString(), anyString())).thenReturn(execData);
            binaryDistributionControlServiceImpl.unzipBinary(hostname, zipPath, binaryLocation, destination, exclude);
            verify(remoteCommandExecutor).executeRemoteCommand(anyString(), eq(hostname), eq(BinaryDistributionControlOperation.UNZIP_BINARY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), eq(zipPath), eq(binaryLocation), eq(destination), eq(exclude));
        } catch (CommandFailureException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeleteBinary() {
        final String hostname = "localhost";
        final String destination = "/build/tmp/";
        final CommandOutput execData = mock(CommandOutput.class);
        when(execData.getReturnCode()).thenReturn(new ExecReturnCode(0));
        try {
            when(remoteCommandExecutor.executeRemoteCommand(anyString(), anyString(), eq(BinaryDistributionControlOperation.DELETE_BINARY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), anyString())).thenReturn(execData);
            binaryDistributionControlServiceImpl.deleteBinary(hostname, destination);
            verify(remoteCommandExecutor).executeRemoteCommand(anyString(), eq(hostname), eq(BinaryDistributionControlOperation.DELETE_BINARY),
                    any(WindowsBinaryDistributionPlatformCommandProvider.class), eq(destination));
        } catch (CommandFailureException e) {
            e.printStackTrace();
        }
    }
}
