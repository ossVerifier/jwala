package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
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

}
