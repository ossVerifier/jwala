package com.siemens.cto.aem.service.configuration.service;

import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.resource.Entity;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.common.domain.model.resource.ResourceTemplateMetaData;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.properties.ApplicationProperties;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.service.*;
import com.siemens.cto.aem.service.resource.impl.handler.WebServerResourceHandler;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final ResourceTemplateMetaData mockMetaData = mock(ResourceTemplateMetaData.class);

    private final InputStream mockData = mock(InputStream.class);

    private ResourceTemplateMetaData metaData;

    @BeforeClass
    public static void init() {
        System.setProperty(ApplicationProperties.PROPERTIES_ROOT_PATH, new File(".").getAbsolutePath() + "/src/test/resources");
    }

    @Before
    public void setup() {
        Mockito.reset(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE);
        Mockito.reset(MockConfig.MOCK_GROUP_PERSISTENCE_SERVICE);
        Mockito.reset(MockConfig.MOCK_JVM_PERSISTENCE_SERVICE);
        Mockito.reset(MockConfig.MOCK_WEB_SERVER_PERSISTENCE_SERVICE);
        Mockito.reset(MockConfig.MOCK_RESOURCE_DAO);

        metaData = new ResourceTemplateMetaData();
        metaData.setContentType("text/xml");
        metaData.setDeployPath("any");
        metaData.setDeployFileName("any");
        metaData.setTemplateName("any");
        final Entity entity = new Entity();
        entity.setGroup("any");
        metaData.setEntity(entity);
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

    @Test
    public void testFetchJvmResourceHandler() {
        resourceHandler.fetchResource(getJvmResourceIdentifier());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO).getJvmResource(eq("sample.xml"), eq("sampleJvm"));
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getAppResource(anyString(), anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelAppResource(anyString(), anyString(), anyString());
    }

    @Test
    public void testFetchAppResourceHandler() {
        resourceHandler.fetchResource(getAppResourceIdentifier());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO).getAppResource(eq("sample.xml"), eq("sampleApp"), eq("sampleJvm"));
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelAppResource(anyString(), anyString(), anyString());
    }

    @Test
    public void testFetchGroupLevelWebServerResourceHandler() {
        resourceHandler.fetchResource(getGroupLevelWebServerResourceIdentifier());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getAppResource(anyString(), anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO).getGroupLevelWebServerResource(eq("sample.xml"), eq("sampleGroup"));
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelAppResource(anyString(), anyString(), anyString());
    }

    @Test
    public void testFetchGroupLevelJvmResourceHandler() {
        resourceHandler.fetchResource(getGroupLevelJvmResourceIdentifier());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getAppResource(anyString(), anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO).getGroupLevelJvmResource(eq("sample.xml"), eq("sampleGroup"));
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelAppResource(anyString(), anyString(), anyString());
    }

    @Test
    public void testFetchGroupLevelWebAppResourceHandler() {
        resourceHandler.fetchResource(getGroupLevelAppResourceIdentifier());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getJvmResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getAppResource(anyString(), anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelWebServerResource(anyString(), anyString());
        verify(MockConfig.MOCK_RESOURCE_DAO, never()).getGroupLevelJvmResource(eq("sample.xml"), eq("sampleGroup"));
        verify(MockConfig.MOCK_RESOURCE_DAO).getGroupLevelAppResource(eq("sample.xml"), eq("sampleApp"), eq("sampleGroup"));
    }

    @Test
    public void testCreateWebServerResourceHandler() {
        when(MockConfig.MOCK_WEB_SERVER_PERSISTENCE_SERVICE.findWebServerByName(anyString())).thenReturn(mock(WebServer.class));
        resourceHandler.createResource(getWebServerResourceIdentifier(), metaData, mockData);
        verify(MockConfig.MOCK_WEB_SERVER_PERSISTENCE_SERVICE).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class),
                anyString(), anyString());
    }

    @Test
    public void testCreateJvmResourceHandler() {
        when(MockConfig.MOCK_JVM_PERSISTENCE_SERVICE.findJvmByExactName(anyString())).thenReturn(mock(Jvm.class));
        resourceHandler.createResource(getJvmResourceIdentifier(), metaData, mockData);
        verify(MockConfig.MOCK_JVM_PERSISTENCE_SERVICE).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
    }

    @Test
    public void testCreateAppResourceHandler() {
        when(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE.getApplication(anyString())).thenReturn(mock(Application.class));
        when(MockConfig.MOCK_JVM_PERSISTENCE_SERVICE.findJvmByExactName(anyString())).thenReturn(mock(Jvm.class));
        resourceHandler.createResource(getAppResourceIdentifier(), metaData, mockData);
        verify(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE).uploadAppTemplate(any(UploadAppTemplateRequest.class),
                any(JpaJvm.class));
    }

    @Test
    public void testCreateGroupLevelWebServerResourceHandler() {
        final Group mockGroup = mock(Group.class);
        final Set<WebServer> webServers = new HashSet<>();
        webServers.add(mock(WebServer.class));
        when(mockGroup.getWebServers()).thenReturn(webServers);
        when(MockConfig.MOCK_GROUP_PERSISTENCE_SERVICE.getGroupWithWebServers(anyString())).thenReturn(mockGroup);
        resourceHandler.createResource(getGroupLevelWebServerResourceIdentifier(), metaData, new ByteArrayInputStream("data".getBytes()));
        verify(MockConfig.MOCK_GROUP_PERSISTENCE_SERVICE).getGroupWithWebServers(anyString());
        verify(MockConfig.MOCK_WEB_SERVER_PERSISTENCE_SERVICE).uploadWebServerConfigTemplate(any(UploadWebServerTemplateRequest.class),
                anyString(), anyString());
    }

    @Test
    public void testCreateGroupLevelJvmResourceHandler() {
        final Group mockGroup = mock(Group.class);
        final Set<Jvm> jvms = new HashSet<>();
        jvms.add(mock(Jvm.class));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(MockConfig.MOCK_GROUP_PERSISTENCE_SERVICE.getGroup(anyString())).thenReturn(mockGroup);
        resourceHandler.createResource(getGroupLevelJvmResourceIdentifier(), metaData, new ByteArrayInputStream("data".getBytes()));
        verify(MockConfig.MOCK_JVM_PERSISTENCE_SERVICE).uploadJvmTemplateXml(any(UploadJvmConfigTemplateRequest.class));
    }

    @Test
    public void testCreateGroupLevelWebAppResourceHandler() {
        final Group mockGroup = mock(Group.class);
        final Set<Jvm> jvms = new HashSet<>();
        jvms.add(mock(Jvm.class));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(MockConfig.MOCK_GROUP_PERSISTENCE_SERVICE.getGroup(anyString())).thenReturn(mockGroup);
        final List<Application> applications = new ArrayList<>();
        final Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("sampleApp");
        applications.add(mockApplication);
        when(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE.findApplicationsBelongingTo(anyString())).thenReturn(applications);
        final Entity entity = new Entity();
        entity.setDeployToJvms(true);
        metaData.setEntity(entity);
        resourceHandler.createResource(getGroupLevelAppResourceIdentifier(), metaData, new ByteArrayInputStream("data".getBytes()));
        verify(MockConfig.getMockGroupPersistenceService()).populateGroupAppTemplate(anyString(), anyString(), anyString(),
                anyString(), anyString());
        verify(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
    }

    @Test
    public void testCreateGroupLevelWebAppBinaryResourceHandler() {
        final Group mockGroup = mock(Group.class);
        final Set<Jvm> jvms = new HashSet<>();
        jvms.add(mock(Jvm.class));
        when(mockGroup.getJvms()).thenReturn(jvms);
        when(MockConfig.MOCK_GROUP_PERSISTENCE_SERVICE.getGroup(anyString())).thenReturn(mockGroup);
        final List<Application> applications = new ArrayList<>();
        final Application mockApplication = mock(Application.class);
        when(mockApplication.getName()).thenReturn("sampleApp");
        applications.add(mockApplication);
        when(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE.findApplicationsBelongingTo(anyString())).thenReturn(applications);
        final Entity entity = new Entity();
        entity.setDeployToJvms(true);
        metaData.setEntity(entity);
        metaData.setContentType("application/binary");
        when(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE.getApplication(anyString())).thenReturn(mock(Application.class));
        resourceHandler.createResource(getGroupLevelAppResourceIdentifier(), metaData, new ByteArrayInputStream("some.war".getBytes()));
        verify(MockConfig.getMockGroupPersistenceService()).populateGroupAppTemplate(anyString(), anyString(), anyString(),
                anyString(), anyString());
        verify(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE).uploadAppTemplate(any(UploadAppTemplateRequest.class), any(JpaJvm.class));
        verify(MockConfig.MOCK_APPLICATION_PERSISTENCE_SERVICE).updateWarInfo(anyString(), anyString(), anyString());
    }

    private ResourceIdentifier getWebServerResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setWebServerName("sampleWebServer").build();
    }

    private ResourceIdentifier getJvmResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setJvmName("sampleJvm").build();
    }

    private ResourceIdentifier getAppResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setWebAppName("sampleApp").setJvmName("sampleJvm").build();
    }

    private ResourceIdentifier getGroupLevelWebServerResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setGroupName("sampleGroup").setWebServerName("*").build();
    }

    private ResourceIdentifier getGroupLevelJvmResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setGroupName("sampleGroup").setJvmName("*").build();
    }

    private ResourceIdentifier getGroupLevelAppResourceIdentifier() {
        ResourceIdentifier.Builder builder = new ResourceIdentifier.Builder();
        return builder.setResourceName("sample.xml").setGroupName("sampleGroup").setWebAppName("sampleApp").build();
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
