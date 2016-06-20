package com.siemens.cto.aem.persistence.service.impl;

import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.jvm.UploadJvmConfigTemplateRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaAppBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JpaWebServerBuilder;
import com.siemens.cto.aem.persistence.jpa.domain.builder.JvmBuilder;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.ResourceDao;
import org.apache.commons.lang3.StringUtils;
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
// @IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
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
                "HttpdSslConfTemplate.tpl", StringUtils.EMPTY, new ByteArrayInputStream("someData".getBytes())) {
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
        groupCrudService.populateGroupAppTemplate(jpaGroup.getName(), "someApp", "someTemplateFileName", "someMetaData", "someData");

        webServerCrudService.uploadWebserverConfigTemplate(uploadWsTemplateRequest);

        jvmCrudService.uploadJvmTemplateXml(uploadJvmConfigTemplateRequest);

        final UploadAppTemplateRequest uploadAppTemplateRequest = new UploadAppTemplateRequest(JpaAppBuilder.appFrom(jpaApplication),
                "someResource", "someFileName",
        "someJvm", "someMetaData", new ByteArrayInputStream("someData".getBytes()));
        applicationCrudService.uploadAppTemplate(uploadAppTemplateRequest, jpaJvm);
    }

    @Test
    public void testDeleteResources() throws Exception {
        assertEquals(1, resourceDao.deleteGroupLevelWebServerResource("httpd.conf", "someGroup"));
        assertEquals(1, resourceDao.deleteGroupLevelJvmResource("someConfName", "someGroup"));
        assertEquals(1, resourceDao.deleteGroupLevelAppResource("someApp", "someGroup", "someTemplateFileName"));
        assertEquals(1, resourceDao.deleteWebServerResource("httpd.conf", "someWebServer"));
        assertEquals(1, resourceDao.deleteJvmResource("someConfName", "someJvm"));
        assertEquals(1, resourceDao.deleteAppResource("someFileName", "someApp", "someJvm"));
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
    }
}