package com.siemens.cto.aem.ws.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class RestConfigTest {

    @InjectMocks
    private RestConfig restConfig;

    @Test
    public void testRestConfig() {
        assertNotNull(restConfig);
    }

}
