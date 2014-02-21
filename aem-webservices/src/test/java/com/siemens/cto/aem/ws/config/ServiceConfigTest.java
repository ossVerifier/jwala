package com.siemens.cto.aem.ws.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by z003bpej on 2/21/14.
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceConfigTest {

    @InjectMocks
    private ServiceConfig serviceConfig;

    @Test
    public void testServiceConfig() {
        assertNotNull(serviceConfig.jvmInfoService());
    }

}
