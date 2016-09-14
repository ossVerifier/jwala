package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.exception.InternalErrorException;
import com.cerner.jwala.common.exec.CommandOutput;
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

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by LW044480 on 9/8/2016.
 */
public class BinaryDistributionServiceImplTest {

    @Mock
    private RemoteCommandExecutor<BinaryDistributionControlOperation> mockRemoteCommandExecutor;

    @Mock
    private BinaryDistributionControlService mockBinaryDistributionControlService;

    private BinaryDistributionServiceImpl binaryDistributionService;

    @Before
    public void setup() {
        initMocks(this);
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        binaryDistributionService = new BinaryDistributionServiceImpl(mockBinaryDistributionControlService);
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testZipBinary() {
        String source = getClass().getClassLoader().getResource("zip-test").getFile();
        String destination = binaryDistributionService.zipBinary(source);
        assertEquals(source + ".zip", destination);
    }

    @Test
    public void testZipBinaryFail() {
        String source = "../../temp/test";
        String destination = binaryDistributionService.zipBinary(source);
        assertNull(destination);
    }

    @Test
    public void testRemoteFileCheck() throws CommandFailureException {
        String hostname = "localhost";
        String destination = "test1234";
        when(mockBinaryDistributionControlService.checkFileExists(hostname, destination)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
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

    @Test
    public void testRemoteUnzipBinary() throws CommandFailureException {
        final String hostname = "localhost";
        final String zipPath = "testZipPath";
        final String binaryLocation = "testBinaryLocation";
        final String destination = "testDest";
        final String exclude = "xxxx";
        when(mockBinaryDistributionControlService.unzipBinary(hostname, zipPath, binaryLocation, destination, exclude)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.remoteUnzipBinary(hostname, zipPath, binaryLocation, destination, exclude);
    }

    @Test(expected = InternalErrorException.class)
    public void testRemoteUnzipBinaryFail() throws CommandFailureException {
        final String hostname = "localhost";
        final String zipPath = "testZipPath";
        final String binaryLocation = "testBinaryLocation";
        final String destination = "testDest";
        final String exclude = "xxxx";
        when(mockBinaryDistributionControlService.unzipBinary(hostname, zipPath, binaryLocation, destination, exclude)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        binaryDistributionService.remoteUnzipBinary(hostname, zipPath, binaryLocation, destination, exclude);
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

    @Test
    public void testDistributeJdk() throws CommandFailureException {
        final String hostname = "localhost";
        final String java_home = ApplicationProperties.get("remote.jwala.java.home").replace("/", "//");
        when(mockBinaryDistributionControlService.checkFileExists(hostname, java_home)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.createDirectory(hostname, "D:/")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.secureCopyFile(hostname, null, java_home + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.unzipBinary(hostname, "~/.jwala/unzip.exe", java_home + ".zip", "D:/", "")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.deleteBinary(hostname, java_home + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.distributeJdk(hostname);
    }

    @Test
    public void testDistributeTomcat() throws CommandFailureException {
        final String hostname = "localhost";
        File tomcat = new File(ApplicationProperties.get("remote.paths.tomcat.core"));
        final String tomcatDir = tomcat.getParentFile().getName();
        final String binaryDeployDir = tomcat.getParentFile().getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        when(mockBinaryDistributionControlService.checkFileExists(hostname, binaryDeployDir + "/" + tomcatDir)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.createDirectory(hostname, binaryDeployDir)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.secureCopyFile(hostname, null, binaryDeployDir + "/" + tomcatDir + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.unzipBinary(hostname, "~/.jwala/unzip.exe",  binaryDeployDir + "/" + tomcatDir + ".zip", binaryDeployDir, "" )).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.deleteBinary(hostname, binaryDeployDir + "/" + tomcatDir + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.distributeTomcat(hostname);
    }

    @Test
    public void testDistributeWebServer() throws CommandFailureException{
        final String hostname = "localhost";
        File apache = new File(ApplicationProperties.get("remote.paths.apache.httpd"));
        final String webServerDir = apache.getName();
        final String webServerBinaryDeployDir = apache.getParentFile().getAbsolutePath().replaceAll("\\\\", "/");
        when(mockBinaryDistributionControlService.checkFileExists(hostname, webServerBinaryDeployDir + "/" + webServerDir)).thenReturn(new CommandOutput(new ExecReturnCode(1), "FAIL", ""));
        when(mockBinaryDistributionControlService.createDirectory(hostname, webServerBinaryDeployDir)).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.secureCopyFile(hostname, null, webServerBinaryDeployDir + "/" + webServerDir + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.unzipBinary(hostname, "~/.jwala/unzip.exe",  webServerBinaryDeployDir + "/" + webServerDir + ".zip", webServerBinaryDeployDir, "ReadMe.txt *--" )).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        when(mockBinaryDistributionControlService.deleteBinary(hostname, webServerBinaryDeployDir + "/" + webServerDir + ".zip")).thenReturn(new CommandOutput(new ExecReturnCode(0), "SUCCESS", ""));
        binaryDistributionService.distributeWebServer(hostname);
    }

}
