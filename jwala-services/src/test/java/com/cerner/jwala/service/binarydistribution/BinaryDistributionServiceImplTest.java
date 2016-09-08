package com.cerner.jwala.service.binarydistribution;

import com.cerner.jwala.common.domain.model.binarydistribution.BinaryDistributionControlOperation;
import com.cerner.jwala.common.properties.ApplicationProperties;
import com.cerner.jwala.control.command.RemoteCommandExecutor;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionControlServiceImpl;
import com.cerner.jwala.service.binarydistribution.impl.BinaryDistributionServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;

/**
 * Created by LW044480 on 9/8/2016.
 */
public class BinaryDistributionServiceImplTest {

    @Mock
    private RemoteCommandExecutor<BinaryDistributionControlOperation> remoteCommandExecutor;

    private BinaryDistributionServiceImpl binaryDistributionService;
    @Before
    public void setup() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
        binaryDistributionService = new BinaryDistributionServiceImpl(new BinaryDistributionControlServiceImpl(remoteCommandExecutor));
    }

    @After
    public void tearDown() {
        System.clearProperty(ApplicationProperties.PROPERTIES_ROOT_PATH);
    }

    @Test
    public void testZipBinary(){
        //TODO:
    }
}
