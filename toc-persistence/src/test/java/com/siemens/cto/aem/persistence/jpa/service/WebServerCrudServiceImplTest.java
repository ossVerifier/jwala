package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.app.Application;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.jvm.Jvm;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.BadRequestException;
import com.siemens.cto.aem.common.request.app.CreateApplicationRequest;
import com.siemens.cto.aem.common.request.group.CreateGroupRequest;
import com.siemens.cto.aem.common.request.jvm.CreateJvmRequest;
import com.siemens.cto.aem.common.request.webserver.UploadWebServerTemplateRequest;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.*;
import com.siemens.cto.aem.persistence.jpa.service.exception.NonRetrievableResourceTemplateContentException;
import com.siemens.cto.aem.persistence.jpa.service.exception.ResourceTemplateUpdateException;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test for {@link WebServerCrudServiceImpl}
 * <p/>
 * Created by JC043760 on 12/16/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {WebServerCrudServiceImplTest.Config.class})
public class WebServerCrudServiceImplTest {

    @Autowired
    private WebServerCrudService impl;

    @Autowired
    private GroupCrudService groupCrudService;

    @Autowired
    private ApplicationCrudService applicationCrudService;

    @Autowired
    private JvmCrudService jvmCrudService;


    @Before
    public void setup() {
        User user = new User("testUser");
        user.addToThread();
    }

    @After
    public void tearDown() {
        User.getThreadLocalUser().invalidate();
    }

    @Test
    public void testCrud() {
        final WebServer newWebServer = new WebServer(null,
                                                     new ArrayList<Group>(),
                                                     "zWebServer",
                                                     "zHost",
                                                     8080,
                                                     443,
                                                     new Path("any"),
                                                     new FileSystemPath("any"),
                                                     new Path("any"),
                                                     new Path("any"),
                                                     WebServerReachableState.WS_UNREACHABLE,
                                                     null);
        final WebServer createdWebServer = impl.createWebServer(newWebServer, "me");
        assertTrue(createdWebServer.getId() != null);
        assertTrue(createdWebServer.getId().getId() != null);
        assertEquals(newWebServer.getName(), createdWebServer.getName());

        final WebServer editedWebServer = new WebServer(createdWebServer.getId(),
                                                        new ArrayList<Group>(),
                                                        "zWebServerx",
                                                        "zHostx",
                                                        808,
                                                        44,
                                                        new Path("anyx"),
                                                        new FileSystemPath("anyx"),
                                                        new Path("anyx"),
                                                        new Path("anyx"),
                                                        WebServerReachableState.WS_UNREACHABLE,
                                                        null);
        final WebServer updatedWebServer = impl.updateWebServer(editedWebServer, "me");
        assertEquals(editedWebServer.getId().getId(), updatedWebServer.getId().getId());

        // Test getWebServer
        WebServer gottenWebServer = impl.getWebServer(editedWebServer.getId());
        assertEquals(editedWebServer.getName(), gottenWebServer.getName());

        // Test findWebServerByName
        gottenWebServer = impl.findWebServerByName(editedWebServer.getName());
        assertEquals(editedWebServer.getName(), gottenWebServer.getName());

        // Test getWebServers
        assertEquals(1, impl.getWebServers().size());

        // Test removeWebServer
        impl.removeWebServer(editedWebServer.getId());
        assertEquals(0, impl.getWebServers().size());
    }

    @Test
    public void removeWebServersBelongingToTest() {
        JpaGroup group = new JpaGroup();
        group.setName("zGroup");
        group = groupCrudService.create(group);
        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("zWebServer");
        webServer.setDocRoot("zRoot");
        webServer.setHttpConfigFile("zConfigFile");
        webServer.setStatusPath("zStatusPath");
        webServer.setSvrRoot("zSvrRoot");
        webServer.getGroups().add(group);
        group.getWebServers().add(impl.create(webServer));
        groupCrudService.update(group);
        assertEquals(1, impl.getWebServer(new Identifier<WebServer>(webServer.getId())).getGroups().size());
        impl.removeWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertEquals(0, impl.getWebServers().size());
    }

    @Test
    public void testFindWebServers() {
        final List<WebServer> foundWebServers = impl.findWebServers("toast");
        assertTrue(foundWebServers.size() == 0);
    }

    @Test
    public void testFindWebServersBelongingTo() {
        CreateGroupRequest createGroupReq = new CreateGroupRequest("testGroupName");
        JpaGroup group = groupCrudService.createGroup(createGroupReq);
        List<WebServer> webServersBelongingTo = impl.findWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertTrue(webServersBelongingTo.size() == 0);

        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost", 101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"), new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = impl.createWebServer(webServer, "testUser");
        List<JpaWebServer> wsList = new ArrayList<>();
        wsList.add(impl.findById(webServer.getId().getId()));
        group.setWebServers(wsList);
        webServersBelongingTo = impl.findWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertTrue(webServersBelongingTo.size() == 1);
    }

    @Test(expected = BadRequestException.class)
    public void testCreateWebServerThrowsException() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost", 101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"), new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, null);
        impl.createWebServer(webServer, "testUser");
        // causes problems
        impl.createWebServer(webServer, "testUser");
    }

    @Test
    public void testFindApplications() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost", 101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"), new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = impl.createWebServer(webServer, "testUser");
        List<Application> applications = impl.findApplications("testWebServer");
        assertTrue(applications.size() == 0);

        CreateGroupRequest creatGroupReq = new CreateGroupRequest("testGroupName");
        JpaGroup group = groupCrudService.createGroup(creatGroupReq);
        CreateApplicationRequest createAppReq = new CreateApplicationRequest(new Identifier<Group>(group.getId()), "testAppName", "/context", true, true);
        JpaApplication application = applicationCrudService.createApplication(createAppReq, group);
        List<JpaWebServer> wsList = new ArrayList<>();
        wsList.add(impl.findById(webServer.getId().getId()));
        group.setWebServers(wsList);
        CreateJvmRequest createJvmReq = new CreateJvmRequest("testJvmName", "testHostName", 1212, 1213, 1214, -1, 1215, new Path("./statusPath"), "");
        final JpaJvm jvm = jvmCrudService.createJvm(createJvmReq);
        List<JpaJvm> jvmsList = new ArrayList<>();
        jvmsList.add(jvm);
        group.setJvms(jvmsList);
        List<JpaGroup> groupList = new ArrayList<>();
        groupList.add(group);
        impl.findById(webServer.getId().getId()).setGroups(groupList);
        applications = impl.findApplications(webServer.getName());
        assertTrue(applications.size() == 1);

        final List<Jvm> jvms = impl.findJvms(webServer.getName());
        assertTrue(jvms.size() == 1);
    }

    @Test
    public void testFindJvms() {
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost", 101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"), new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, null);
        impl.createWebServer(webServer, "testUser");
        List<Jvm> jvms = impl.findJvms("testWebServer");
        assertTrue(jvms.size() == 0);
    }

    @Test
    public void testGetResourceTemplateNames() {
        final List<String> templates = impl.getResourceTemplateNames("toost");
        assertTrue(templates.size() == 0);
    }

    @Test(expected = NonRetrievableResourceTemplateContentException.class)
    public void testGetResourceTemplate() {
        impl.getResourceTemplate("teest", "tust");
    }

    @Test
    public void testUploadWebServerTemplate() throws FileNotFoundException {
        InputStream dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost", 101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"), new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = impl.createWebServer(webServer, "testUser");
        UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer, "HttpdSslConfTemplate.tpl", dataInputStream) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        JpaWebServerConfigTemplate template = impl.uploadWebserverConfigTemplate(uploadWsTemplateRequest);
        assertNotNull(template);

        // again!
        dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer, "HttpdSslConfTemplate.tpl", dataInputStream) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        template = impl.uploadWebserverConfigTemplate(uploadWsTemplateRequest);
        assertNotNull(template);

        // test getting the template while we're here
        final String resourceTemplate = impl.getResourceTemplate(webServer.getName(), "httpd.conf");
        assertTrue(!resourceTemplate.isEmpty());
    }

    @Test
    public void testPopulateWebServerConfigTemplate() throws FileNotFoundException {
        InputStream dataInputStream = new FileInputStream(new File("./src/test/resources/HttpdSslConfTemplate.tpl"));
        WebServer webServer = new WebServer(new Identifier<WebServer>(1111L), new HashSet<Group>(), "testWebServer", "testHost", 101, 102, new Path("./statusPath"), new FileSystemPath("./httpdConfPath"), new Path("./svrRootPath"), new Path("./docRoot"), WebServerReachableState.WS_UNREACHABLE, null);
        webServer = impl.createWebServer(webServer, "testUser");
        UploadWebServerTemplateRequest uploadWsTemplateRequest = new UploadWebServerTemplateRequest(webServer, "HttpdSslConfTemplate.tpl", dataInputStream) {
            @Override
            public String getConfFileName() {
                return "httpd.conf";
            }
        };
        List<UploadWebServerTemplateRequest> templateCommands = new ArrayList<>();
        templateCommands.add(uploadWsTemplateRequest);
        impl.populateWebServerConfig(templateCommands, new User("userId"), false);
        final String resourceTemplate = impl.getResourceTemplate(webServer.getName(), "httpd.conf");
        assertTrue(!resourceTemplate.isEmpty());
    }

    @Test
    public void testUpdateResourceTemplate() throws FileNotFoundException {
        testUploadWebServerTemplate();
        impl.updateResourceTemplate("testWebServer", "httpd.conf", "this is my template");
        final String updatedContent = impl.getResourceTemplate("testWebServer", "httpd.conf");
        assertEquals("this is my template", updatedContent);
    }

    @Test(expected = ResourceTemplateUpdateException.class)
    public void testUpdateResourceTemplateThrowsException() {
        impl.updateResourceTemplate("tyest", "templateNOOOO", "0_o");
    }

    @Test
    public void findApplicationsTest() {
        JpaGroup group = new JpaGroup();
        group.setName("aGroup");
        group = groupCrudService.create(group);

        final JpaWebServer webServer = new JpaWebServer();
        webServer.setName("aWebServer");
        webServer.setDocRoot("aRoot");
        webServer.setHttpConfigFile("aConfigFile");
        webServer.setStatusPath("aStatusPath");
        webServer.setSvrRoot("aSvrRoot");
        webServer.getGroups().add(group);
        group.getWebServers().add(impl.create(webServer));
        groupCrudService.update(group);

        final JpaApplication application = new JpaApplication();
        application.setName("anApplication");
        application.setWebAppContext("aWebAppContext");
        application.setGroup(group);
        applicationCrudService.create(application);

        assertEquals(1, impl.getWebServer(new Identifier<WebServer>(webServer.getId())).getGroups().size());
        assertEquals(1, impl.findApplications(webServer.getName()).size());
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
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }
    }

}
