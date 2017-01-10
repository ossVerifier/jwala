package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
import com.cerner.jwala.common.exec.ExecCommand;
import com.cerner.jwala.common.exec.ExecReturnCode;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.exception.CommandFailureException;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by Lin-Hung Wu on 9/8/2016.
 */
public class BinaryDistributionServiceImplTest {

    @Mock
    private RemoteCommandExecutor<BinaryDistributionControlOperation> mockRemoteCommandExecutor;

    @Mock
    private BinaryDistributionControlService mockBinaryDistributionControlService;

    @Mock
    private BinaryDistributionLockManager mockBinaryDistributionLockManager;

    private BinaryDistributionServiceImpl binaryDistributionService;

    @Before
    public void setup() {
        initMocks(this);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        binaryDistributionService = new BinaryDistributionServiceImpl(mockBinaryDistributionControlService, mockBinaryDistributionLockManager);
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testRemoteFileCheck() throws CommandFailureException {
        String hostname = "localhost";
        String destination = "test1234";
        when(mockBinaryDistributionControlService.checkFileExists(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        assertTrue(binaryDistributionService.remoteFileCheck(hostname, destination));
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteFileCheckException() throws CommandFailureException {
        String hostname = "localhost";
        String destination = "test1234";
        when(mockBinaryDistributionControlService.checkFileExists(hostname, destination)).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        assertTrue(binaryDistributionService.remoteFileCheck(hostname, destination));
    }

    @Test
    public void testRemoteCreateDirectory() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.createDirectory(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteCreateDirectory(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteCreateDirectoryFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.createDirectory(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteCreateDirectory(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteCreateDirectoryException() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.createDirectory(hostname, destination)).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteCreateDirectory(hostname, destination);
    }

    @Test
    public void testRemoteSecureCopyFile() throws CommandFailureException {
        final String hostname = "localhost";
        final String source = "testSource";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.secureCopyFile(hostname, source, destination)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteSecureCopyFile(hostname, source, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteSecureCopyFileFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String source = "testSource";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.secureCopyFile(hostname, source, destination)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteSecureCopyFile(hostname, source, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteSecureCopyFileException() throws CommandFailureException {
        final String hostname = "localhost";
        final String source = "testSource";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.secureCopyFile(hostname, source, destination)).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteSecureCopyFile(hostname, source, destination);
    }

    @Test
    public void testRemoteUnzipBinary() throws CommandFailureException {
        final String hostname = "localhost";
        final String zipPath = "testZipPath";
        final String binaryLocation = "testBinaryLocation";
        final String destination = "testDest";
        final String exclude = "xxxx";
        when(mockBinaryDistributionControlService.unzipBinary(hostname, binaryLocation, destination, exclude)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteUnzipBinary(hostname, binaryLocation, destination, exclude);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteUnzipBinaryFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String zipPath = "testZipPath";
        final String binaryLocation = "testBinaryLocation";
        final String destination = "testDest";
        final String exclude = "xxxx";
        when(mockBinaryDistributionControlService.unzipBinary(hostname,  binaryLocation, destination, exclude)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteUnzipBinary(hostname,  binaryLocation, destination, exclude);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteUnzipBinaryException() throws CommandFailureException {
        final String hostname = "localhost";
        final String zipPath = "testZipPath";
        final String binaryLocation = "testBinaryLocation";
        final String destination = "testDest";
        final String exclude = "xxxx";
        when(mockBinaryDistributionControlService.unzipBinary(hostname,  binaryLocation, destination, exclude)).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteUnzipBinary(hostname, binaryLocation, destination, exclude);
    }

    @Test
    public void testRemoteDeleteBinary() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.deleteBinary(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteDeleteBinary(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteDeleteBinaryFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.deleteBinary(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteDeleteBinary(hostname, destination);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteDeleteBinaryException() throws CommandFailureException {
        final String hostname = "localhost";
        final String destination = "testDest";
        when(mockBinaryDistributionControlService.deleteBinary(hostname, destination)).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.remoteDeleteBinary(hostname, destination);
    }

    @Test
    public void testChangeFileMode() throws CommandFailureException {
        final String hostname = "localhost";
        final String mode = "testMode";
        final String targetDir = "~/test";
        final String target = "testFile";
        when(mockBinaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.changeFileMode(hostname, mode, targetDir, target);
    }

    @Test(expected = InternalErrorException.class)
    public void testChangeFileModeFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String mode = "testMode";
        final String targetDir = "~/test";
        final String target = "testFile";
        when(mockBinaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.changeFileMode(hostname, mode, targetDir, target);
    }

    @Test(expected = InternalErrorException.class)
    public void testChangeFileModeException() throws CommandFailureException{
        final String hostname = "localhost";
        final String mode = "testMode";
        final String targetDir = "~/test";
        final String target = "testFile";
        when(mockBinaryDistributionControlService.changeFileMode(hostname, mode, targetDir, target)).thenThrow(new CommandFailureException(new ExecCommand("failed command"), new Throwable()));
        binaryDistributionService.changeFileMode(hostname, mode, targetDir, target);
    }

//    @Test
//    public void testDistributeJdk() throws CommandFailureException {
//        final String hostname = "localhost";
//        final String javaHome = ApplicationProperties.get("remote.jwala.java.home");
//        final String javaParentDir = new File(javaHome).getParent().replaceAll("\\\\", "/");
//        when(mockBinaryDistributionControlService.checkFileExists(hostname, javaHome)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
//        when(mockBinaryDistributionControlService.createDirectory(hostname, javaParentDir)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
//        when(mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
//        when(mockBinaryDistributionControlService.unzipBinary(hostname, "~/.jwala/unzip.exe", javaHome + ".zip", "D:/ctp", "")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
//        when(mockBinaryDistributionControlService.deleteBinary(hostname, javaHome + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
//        binaryDistributionService.distributeJdk(hostname);
//    }

    @Test
    public void testDistributeTomcat() throws CommandFailureException {
        final String hostname = "localhost";
        File tomcat = new File(ApplicationProperties.get("remote.paths.tomcat.core"));
        final String tomcatDir = tomcat.getParentFile().getName();
        final String binaryDeployDir = tomcat.getParentFile().getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        when(mockBinaryDistributionControlService.checkFileExists(hostname, binaryDeployDir + "/" + tomcatDir)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.createDirectory(hostname, binaryDeployDir)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.unzipBinary(hostname, binaryDeployDir + "/" + tomcatDir + ".zip", binaryDeployDir, "")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.deleteBinary(hostname, binaryDeployDir + "/" + tomcatDir + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.distributeTomcat(hostname);
    }

    @Test
    public void testDistributeWebServer() throws CommandFailureException {
        final String hostname = "localhost";
        File apache = new File(ApplicationProperties.get("remote.paths.apache.httpd"));
        final String webServerDir = apache.getName();
        final String webServerBinaryDeployDir = apache.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        when(mockBinaryDistributionControlService.checkFileExists(hostname, webServerBinaryDeployDir + "/" + webServerDir)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.createDirectory(hostname, webServerBinaryDeployDir)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        ApplicationProperties.get("remote.commands.user-scripts");
        when(mockBinaryDistributionControlService.unzipBinary(hostname, webServerBinaryDeployDir + "/" + webServerDir + ".zip", webServerBinaryDeployDir, "ReadMe.txt *--")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.deleteBinary(hostname, webServerBinaryDeployDir + "/" + webServerDir + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.distributeWebServer(hostname);
    }

    @Test
    public void testPrepareUnzip() throws CommandFailureException {
        final String hostname = "localhost";
        final String jwalaScriptsPath = ApplicationProperties.get("remote.commands.user-scripts");
        when(mockBinaryDistributionControlService.checkFileExists(hostname, jwalaScriptsPath)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.createDirectory(hostname, jwalaScriptsPath)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.checkFileExists(hostname, jwalaScriptsPath + "/unzip.exe")).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.secureCopyFile(anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.changeFileMode(anyString(), anyString(), anyString(), anyString())).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.prepareUnzip(hostname);
    }
}
