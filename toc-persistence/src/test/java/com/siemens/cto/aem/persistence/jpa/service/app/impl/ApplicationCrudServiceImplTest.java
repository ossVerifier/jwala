package com.siemens.cto.aem.persistence.jpa.service.app.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.fault.AemFaultType;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.app.UploadAppTemplateRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaApplication;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaJvm;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.impl.JpaJvmPersistenceServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {ApplicationCrudServiceImplTest.Config.class
        })
public class ApplicationCrudServiceImplTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationCrudServiceImplTest.class);

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(),
                    getGroupJvmRelationshipService());
        }

        @Bean
        public JvmPersistenceService getJvmPersistenceService() {
            return new JpaJvmPersistenceServiceImpl(getJvmCrudService(), getApplicationCrudService(), getGroupJvmRelationshipService());
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(),
                    getJvmCrudService());
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }
    }

    @Autowired
    ApplicationCrudService applicationCrudService;

    @Autowired
    GroupCrudService groupCrudService;

    @Autowired
    JvmCrudService jvmCrudService;

    private String aUser;

    private String alphaLower = "abcdefghijklmnopqrstuvwxyz";
    private String alpha = alphaLower + alphaLower.toUpperCase();
    private String alphaNum = alpha + "0123456789,.-/_$ ";
    private String alphaUnsafe = alphaNum + "\\\t\r\n";

    private String textContext = "/" + RandomStringUtils.random(25, alphaUnsafe.toCharArray());
    private String textName = RandomStringUtils.random(25, alphaUnsafe.toCharArray());
    private String textGroup = RandomStringUtils.random(25, alphaUnsafe.toCharArray());


    private Identifier<Group> expGroupId;
    private JpaGroup jpaGroup;

    private User userObj;


    @Before
    public void setup() {
        User user = new User("testUser");
        user.addToThread();

        aUser = "TestUserId";
        userObj = new User(aUser);
        jpaGroup = groupCrudService.createGroup(new CreateGroupRequest(textGroup));
        expGroupId = Identifier.id(jpaGroup.getId());
    }

    @After
    public void tearDown() {
        try {
            groupCrudService.removeGroup(expGroupId);
        } catch (Exception x) {
            LOGGER.trace("Test tearDown", x);
        }
        User.getThreadLocalUser().invalidate();
    }

    @Test(expected = BadRequestException.class)
    public void testApplicationCrudServiceEEE() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId, textName, textContext, true, true, false);

        JpaApplication created = applicationCrudService.createApplication(request, jpaGroup);

        assertNotNull(created);

        try {
            JpaApplication duplicate = applicationCrudService.createApplication(request, jpaGroup);
            fail(duplicate.toString());
        } catch (BadRequestException e) {
            assertEquals(AemFaultType.DUPLICATE_APPLICATION, e.getMessageResponseStatus());
            throw e;
        } finally {
            try {
                applicationCrudService.removeApplication(Identifier.<Application>id(created.getId())
                );
            } catch (Exception x) {
                LOGGER.trace("Test tearDown", x);
            }
        }

    }

    @Test
    public void testDuplicateContextsOk() {
        CreateApplicationRequest request = new CreateApplicationRequest(expGroupId, textName, textContext, true, true, false);

        JpaApplication created2 = null;
        JpaApplication created = applicationCrudService.createApplication(request, jpaGroup);

        assertNotNull(created);

        try {
            CreateApplicationRequest request2 = new CreateApplicationRequest(expGroupId, textName + "-another", textContext, true, true, false);

            created2 = applicationCrudService.createApplication(request2, jpaGroup);

            assertNotNull(created2);
        } finally {
            try {
                applicationCrudService.removeApplication(Identifier.<Application>id(created.getId()));
            } catch (Exception x) {
                LOGGER.trace("Test tearDown", x);
            }
            try {
                if (created2 != null) {
                    applicationCrudService.removeApplication(Identifier.<Application>id(created2.getId()));
                }
            } catch (Exception x) {
                LOGGER.trace("Test tearDown", x);
            }
        }
    }

    @Test
    public void testGetResourceTemplateNames() {
        List<String> templateNames = applicationCrudService.getResourceTemplateNames("testNoAppExists");
        assertEquals(0, templateNames.size());
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetResourceTemplateNonExistent() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testGetResourceTemplateJvm", "testHost", 9100, 9101, 9102, -1, 9103, new Path("./"), "", null, null);
        JpaJvm jvm = jvmCrudService.createJvm(createJvmRequest);
        applicationCrudService.getResourceTemplate("testNoAppExists", "hct.xml", jvm);
    }

    @Test
    public void testGetResourceTemplate() throws FileNotFoundException {
        InputStream data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "testHost", 9100, 9101, 9102, -1, 9103, new Path("./"), "", null, null);
        CreateApplicationRequest createApplicationRequest = new CreateApplicationRequest(new Identifier<Group>(jpaGroup.getId()), "testAppResourceTemplateName", "/hctTest", true, true, false);
        Group group = new Group(new Identifier<Group>(jpaGroup.getId()), jpaGroup.getName());
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest);
        JpaApplication jpaApp = applicationCrudService.createApplication(createApplicationRequest, jpaGroup);

        List<Application> appsForJpaGroup = applicationCrudService.findApplicationsBelongingTo(new Identifier<Group>(jpaGroup.getId()));
        assertEquals(1, appsForJpaGroup.size());

        Application app = new Application(new Identifier<Application>(jpaApp.getId()), jpaApp.getName(), jpaApp.getWarPath(), jpaApp.getWebAppContext(), group, true, true, false, "testApp.war");
        UploadAppTemplateRequest uploadTemplateRequest = new UploadAppTemplateRequest(app, "ServerXMLTemplate.tpl", "hct.xml",
                "testJvmName", StringUtils.EMPTY, data);

        applicationCrudService.uploadAppTemplate(uploadTemplateRequest, jpaJvm);
        String templateContent = applicationCrudService.getResourceTemplate("testAppResourceTemplateName", "hct.xml", jpaJvm);

        assertTrue(!templateContent.isEmpty());

        data = new FileInputStream(new File("./src/test/resources/ServerXMLTemplate.tpl"));
        uploadTemplateRequest = new UploadAppTemplateRequest(app, "ServerXMLTemplate.tpl", "hct.xml", "testJvmName", StringUtils.EMPTY, data
        );
        applicationCrudService.uploadAppTemplate(uploadTemplateRequest, jpaJvm);
        String templateContentUpdateWithTheSame = applicationCrudService.getResourceTemplate("testAppResourceTemplateName", "hct.xml", jpaJvm);

        assertEquals(templateContent, templateContentUpdateWithTheSame);

        applicationCrudService.updateResourceTemplate(app.getName(), "hct.xml", "new template content", jpaJvm);
        String updatedContent = applicationCrudService.getResourceTemplate(app.getName(), "hct.xml", jpaJvm);
        assertEquals("new template content", updatedContent);
    }

    @Test (expected = ResourceTemplateUpdateException.class)
    public void testUpdateResourceTemplate() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testJvmName", "testHost", 9100, 9101, 9102, -1, 9103, new Path("./"), "", null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest);
        applicationCrudService.updateResourceTemplate("noApp", "noTemplate", "doesn't matter", jpaJvm);
    }

    @Test(expected = NotFoundException.class)
    public void testGetApplicationThrowsException() {
        applicationCrudService.getApplication(new Identifier<Application>(888888L));
    }

    @Test
    public void testGetApplication() {
        CreateApplicationRequest createTestApp = new CreateApplicationRequest(new Identifier<Group>(jpaGroup.getId()), "testAppName", "/testApp", true, true, false);
        JpaApplication jpaApp = applicationCrudService.createApplication(createTestApp, jpaGroup);
        Application application = applicationCrudService.getApplication(new Identifier<Application>(jpaApp.getId()));
        assertEquals(jpaApp.getName(), application.getName());
    }

    @Test(expected = NotFoundException.class)
    public void testGetApplicationThrowsNotFoundException() {
        applicationCrudService.getApplication(new Identifier<Application>(808L));
    }

    @Test
    public void testGetApplications() {
        List<Application> apps = applicationCrudService.getApplications();
        assertEquals(0, apps.size());
    }

    @Test
    public void testFindApplicationBelongingToJvm() {
        CreateJvmRequest createJvmRequest = new CreateJvmRequest("testAppJvm", "theHost", 9100, 9101, 9102, -1, 9103, new Path("."), "", null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest);

        List<Application> apps = applicationCrudService.findApplicationsBelongingToJvm(new Identifier<Jvm>(jpaJvm.getId()));
        assertEquals(0, apps.size());
    }

    @Test
    public void testFindApplication() {
        CreateApplicationRequest createTestApp = new CreateApplicationRequest(new Identifier<Group>(jpaGroup.getId()), "testAppName", "/testApp", true, true, false);
        JpaApplication jpaApp = applicationCrudService.createApplication(createTestApp, jpaGroup);

        CreateJvmRequest createJvmRequest =new CreateJvmRequest("testJvmName", "hostName", 9100, 9101, 9102, -1, 9103, new Path("./"), "", null, null);
        JpaJvm jpaJvm = jvmCrudService.createJvm(createJvmRequest);

        List<JpaJvm> jvmList = new ArrayList<>();
        jvmList.add(jpaJvm);
        jpaGroup.setJvms(jvmList);

        applicationCrudService.findApplication(jpaApp.getName(), jpaGroup.getName(), jpaJvm.getName());
    }

}
