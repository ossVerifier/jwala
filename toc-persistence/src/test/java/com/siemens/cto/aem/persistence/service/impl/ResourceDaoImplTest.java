package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.domain.model.resource.EntityType;
import com.siemens.cto.aem.common.domain.model.resource.ResourceIdentifier;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.*;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaWebServerBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JvmBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.resource.config.template.*;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ResourceDaoImpl}.
 *
 * Created by JC043760 on 6/7/2016.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {ResourceDaoImplTest.Config.class
        })
@Transactional
public class ResourceDaoImplTest {

    @Autowired
    private GroupCrudService groupCrudService;

    @Autowired
    private WebServerCrudService webServerCrudService;

    @Autowired
    private JvmCrudService jvmCrudService;

    @Autowired
    private ApplicationCrudService applicationCrudService;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private ResourcePersistenceService resourcePersistenceService;

    @Before
    public void setUp() throws Exception {
        JpaGroup jpaGroup = new JpaGroup();
        jpaGroup.setName("someGroup");
        jpaGroup = groupCrudService.create(jpaGroup);

        JpaWebServer jpaWebServer = new JpaWebServer();
        jpaWebServer.setName("someWebServer");

        final List<JpaGroup> jpaGroupList = new ArrayList<>();
        jpaGroupList.add(jpaGroup);
        jpaWebServer.setGroups(jpaGroupList);
        jpaWebServer.setHost("someHost");
        jpaWebServer.setPort(0);
        jpaWebServer.setDocRoot("someDocRoot");
        jpaWebServer.setHttpConfigFile("someHttpConfigFile");
        jpaWebServer.setStatusPath("someStatusPath");
        jpaWebServer.setSvrRoot("someSvrRoot");

        jpaWebServer = webServerCrudService.create(jpaWebServer);

        final JpaWebServerBuilder jpaWebServerBuilder = new JpaWebServerBuilder(jpaWebServer);
        final UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(jpaWebServerBuilder.build(),
                "HttpdSslConfTemplate.tpl", "someMetaData", new ByteArrayInputStream("someData".getBytes())) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        groupCrudService.uploadGroupWebServerTemplate(uploadWsTemplateRequest, jpaGroup);

        JpaJvm jpaJvm = new JpaJvm();
        jpaJvm.setName("someJvm");
        jpaJvm.setHostName("someHost");
        jpaJvm.setStatusPath("someStatusPath");
        jpaJvm.setHttpPort(0);
        jpaJvm.setHttpsPort(0);
        jpaJvm.setRedirectPort(0);
        jpaJvm.setShutdownPort(0);
        jpaJvm.setAjpPort(0);
        jpaJvm.setGroups(jpaGroupList);

        jpaJvm = jvmCrudService.create(jpaJvm);

        final JvmBuilder jvmBuilder = new JvmBuilder(jpaJvm);
        final UploadJvmConfigTemplateRequest uploadJvmConfigTemplateRequest = new UploadJvmConfigTemplateRequest(jvmBuilder.build(),
                "someJvmFileName", new ByteArrayInputStream("someData".getBytes()), "someMetaData");
        uploadJvmConfigTemplateRequest.setConfFileName("someConfName");
        groupCrudService.uploadGroupJvmTemplate(uploadJvmConfigTemplateRequest, jpaGroup);

        final JpaApplication jpaApplication = new JpaApplication();
        jpaApplication.setName("someApp");
        jpaApplication.setWebAppContext("someContext");
        jpaApplication.setGroup(jpaGroup);
        applicationCrudService.create(jpaApplication);
        groupCrudService.populateGroupAppTemplate(jpaGroup.getName(), "someApp", "someFileName", "someMetaData", "someData");

        webServerCrudService.uploadWebserverConfigTemplate(uploadWsTemplateRequest);

        jvmCrudService.uploadJvmTemplateXml(uploadJvmConfigTemplateRequest);

        final UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(JpaAppBuilder.appFrom(jpaApplication),
                "someResource", "someFileName",
        "someJvm", "someMetaData", new ByteArrayInputStream("someData".getBytes()));
        applicationCrudService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);

        resourcePersistenceService.createResource(null, null, null, EntityType.EXT_PROPERTIES, "external.properties", new ByteArrayInputStream("property=test".getBytes()));
    }

    @Test
    public void testDeleteResources() throws Exception {
        assertEquals(1, resourceDao.deleteGroupLevelWebServerResource("httpd.conf", "someGroup"));
        assertEquals(1, resourceDao.deleteGroupLevelJvmResource("someConfName", "someGroup"));
        assertEquals(1, resourceDao.deleteGroupLevelAppResource("someApp", "someGroup", "someFileName"));
        assertEquals(1, resourceDao.deleteWebServerResource("httpd.conf", "someWebServer"));
        assertEquals(1, resourceDao.deleteJvmResource("someConfName", "someJvm"));
        assertEquals(1, resourceDao.deleteAppResource("someFileName", "someApp", "someJvm"));
    }

    @Test
    public void testDeleteResourcesByTemplateNameList() throws Exception {
        final List<String> names = new ArrayList<>();
        names.add("httpd.conf");
        assertEquals(1, resourceDao.deleteWebServerResources(names, "someWebServer"));
        assertEquals(1, resourceDao.deleteGroupLevelWebServerResources(names, "someGroup"));

        names.clear();
        names.add("someConfName");
        assertEquals(1, resourceDao.deleteJvmResources(names, "someJvm"));
        assertEquals(1, resourceDao.deleteGroupLevelJvmResources(names, "someGroup"));

        names.clear();
        names.add("someFileName");
        assertEquals(1, resourceDao.deleteAppResources(names, "someApp", "someJvm"));
        assertEquals(1, resourceDao.deleteGroupLevelAppResources("someApp", "someGroup", names));
    }

    @Test
    public void testGetWebServerResource() {
        final JpaWebServerConfigTemplate jpaWebServerConfigTemplate = resourceDao.getWebServerResource("httpd.conf", "someWebServer");
        assertEquals("someMetaData", jpaWebServerConfigTemplate.getMetaData());
    }

    @Test
    public void testGetJvmResource() {
        final JpaJvmConfigTemplate jpaJvmConfigTemplate = resourceDao.getJvmResource("someConfName", "someJvm");
        assertEquals("someMetaData", jpaJvmConfigTemplate.getMetaData());
    }

    @Test
    public void testGetAppResource() {
        final JpaApplicationConfigTemplate jpaApplicationConfigTemplate =
                resourceDao.getAppResource("someFileName", "someApp", "someJvm");
        assertEquals("someMetaData", jpaApplicationConfigTemplate.getMetaData());
    }

    @Test
    public void testGetGroupLevelWebServerResource() {
        final JpaGroupWebServerConfigTemplate jpaGroupWebServerConfigTemplate
                = resourceDao.getGroupLevelWebServerResource("httpd.conf", "someGroup");
        assertEquals("someMetaData", jpaGroupWebServerConfigTemplate.getMetaData());
    }

    @Test
    public void testGetGroupLevelJvmResource() {
        final JpaGroupJvmConfigTemplate jpaGroupJvmConfigTemplate
                = resourceDao.getGroupLevelJvmResource("someConfName", "someGroup");
        assertEquals("someMetaData", jpaGroupJvmConfigTemplate.getMetaData());
    }

    @Test
    public void testGetGroupLevelAppResource() {
        final JpaGroupAppConfigTemplate jpaGroupAppConfigTemplate
                = resourceDao.getGroupLevelAppResource("someFileName", "someApp", "someGroup");
        assertEquals("someMetaData", jpaGroupAppConfigTemplate.getMetaData());
    }

    @Test
    public void testGetExternalPropertiesResource() {
        JpaResourceConfigTemplate result = resourceDao.getExternalPropertiesResource("external.properties");
        assertEquals("property=test", result.getTemplateContent());
    }

    @Test
    public void testCreateResource() {
        InputStream data = new ByteArrayInputStream("key=value".getBytes());
        String metaData = "{deployFileName:'external.properties'}";

        JpaResourceConfigTemplate result = resourceDao.createResource(1L, 1L, 1L, EntityType.EXT_PROPERTIES, "external.properties", data, metaData);

        assertEquals(new Long(1), result.getEntityId());
        assertEquals(new Long(1), result.getGroupId());
        assertEquals(new Long(1), result.getAppId());
        assertEquals(EntityType.EXT_PROPERTIES, result.getEntityType());
        assertEquals("external.properties", result.getTemplateName());
        assertEquals("key=value", result.getTemplateContent());
        assertEquals(metaData, result.getMetaData());
    }

    @Test
    public void testGetResourceNames() {
        ResourceIdentifier.Builder idBuilder = new ResourceIdentifier.Builder();
        List<String> result = resourceDao.getResourceNames(idBuilder.build(), EntityType.EXT_PROPERTIES);
        assertEquals(1, result.size());
        assertEquals("external.properties", result.get(0));
    }

    @Test
    public void testDeleteExternalProperties() {
        int result = resourceDao.deleteExternalProperties();
        assertEquals(1, result);
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public WebServerCrudService getWebServerCrudService() {
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public ResourceDao getResourceDao() {
            return new ResourceDaoImpl();
        }

        @Bean
        public ResourcePersistenceService getResourcePersistenceService() {
            return new JpaResourcePersistenceServiceImpl();
        }
    }
}