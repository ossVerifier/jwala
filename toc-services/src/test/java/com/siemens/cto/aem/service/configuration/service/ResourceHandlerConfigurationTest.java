package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.persistence.service.*;
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
        Mockito.reset(MockConfig.MOCK_RESOURCE_DAO);
    }

    @Test
    public void testFetchWebServerResourceHandler() {
        resourceHandler.fetchResource(getWebServerResourceIdentifier());
        verify(MockConfig.MOCK_RESOURCE_DAO).getWebServerResource(eq("sample.xml"), eq("sampleWebServer"));
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getAppResource(anyString(), anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelAppResource(anyString(), anyString(), anyString());
    }

    // TODO: Complete all test cases...

    private ResourceIdentifier getWebServerResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setWebServerName("sampleWebServer").build();
    }

    @Configuration
    static class MockConfig {

        public static final ResourceDao MOCK_RESOURCE_DAO = mock(ResourceDao.class);
        public static final GroupPersistenceService MOCK_GROUP_PERSISTENCE_SERVICE = mock(GroupPersistenceService.class);
        public static final WebServerPersistenceService MOCK_WEB_SERVER_PERSISTENCE_SERVICE = mock(WebServerPersistenceService.class);
        public static final JvmPersistenceService MOCK_JVM_PERSISTENCE_SERVICE = mock(JvmPersistenceService.class);
        public static final ApplicationPersistenceService MOCK_APPLICATION_PERSISTENCE_SERVICE = mock(ApplicationPersistenceService.class);

        @Bean
        public ResourceDao resourceDao() {
            return MOCK_RESOURCE_DAO;
        }

        public static ResourceDao getMockResourceDao() {
            return MOCK_RESOURCE_DAO;
        }

        @Bean
        public static GroupPersistenceService getMockGroupPersistenceService() {
            return MOCK_GROUP_PERSISTENCE_SERVICE;
        }

        @Bean
        public static WebServerPersistenceService getMockWebServerPersistenceService() {
            return MOCK_WEB_SERVER_PERSISTENCE_SERVICE;
        }

        @Bean
        public static JvmPersistenceService getMockJvmPersistenceService() {
            return MOCK_JVM_PERSISTENCE_SERVICE;
        }

        @Bean
        public static ApplicationPersistenceService getMockApplicationPersistenceService() {
            return MOCK_APPLICATION_PERSISTENCE_SERVICE;
        }
    }
}
