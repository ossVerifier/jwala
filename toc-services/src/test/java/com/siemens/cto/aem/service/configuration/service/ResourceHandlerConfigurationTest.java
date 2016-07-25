package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.service.resource.impl.handler.WebServerResourceHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test resource handler chain
 *
 * Created by JC043760 on 7/22/2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {ResourceHandlerConfiguration.class,
        ResourceHandlerConfigurationTest.MockConfig.class})
public class ResourceHandlerConfigurationTest {

    @Autowired
    private WebServerResourceHandler resourceHandler;

    @Before
    public void setup() {
        Mockito.reset(MockConfig.mockResourceDao);
    }

    @Test
    public void testFetchWebServerResourceHandler() {
        resourceHandler.fetchResource(getWebServerResourceIdentifier());
        verify(MockConfig.mockResourceDao).getWebServerResource(eq("sample.xml"), eq("sampleWebServer"));
        verify(MockConfig.mockResourceDao, never()).getJvmResource(anyString(), anyString());
        verify(MockConfig.mockResourceDao, never()).getAppResource(anyString(), anyString(), anyString());
        verify(MockConfig.mockResourceDao, never()).getGroupLevelWebServerResource(anyString(), anyString());
        verify(MockConfig.mockResourceDao, never()).getGroupLevelJvmResource(anyString(), anyString());
        verify(MockConfig.mockResourceDao, never()).getGroupLevelAppResource(anyString(), anyString(), anyString());
    }

    // TODO: Complete all test cases...

    private ResourceIdentifier getWebServerResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setWebServerName("sampleWebServer").build();
    }

    @Configuration
    static class MockConfig {

        public static final ResourceDao mockResourceDao = mock(ResourceDao.class);

        @Bean
        public ResourceDao resourceDao() {
            return mockResourceDao;
        }
    }
}
