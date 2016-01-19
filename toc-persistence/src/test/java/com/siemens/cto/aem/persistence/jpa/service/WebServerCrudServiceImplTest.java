package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.path.FileSystemPath;
import com.siemens.cto.aem.common.domain.model.path.Path;
import com.siemens.cto.aem.common.domain.model.user.User;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.domain.JpaGroup;
import com.siemens.cto.aem.persistence.jpa.domain.JpaWebServer;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for {@link WebServerCrudServiceImpl}
 *
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
    private WebServerCrudServiceImpl impl;

    @Autowired
    private GroupCrudService groupCrudService;


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
                                                     new Path("any"));
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
                                                        new Path("anyx"));
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
        group.getWebServers().add(impl.create(webServer));
        groupCrudService.update(group);
        assertEquals(1, impl.getWebServers().size());
        impl.removeWebServersBelongingTo(new Identifier<Group>(group.getId()));
        assertEquals(0, impl.getWebServers().size());
    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {
        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public WebServerCrudServiceImpl getWebServerStateCrudServiceImpl() {
            return new WebServerCrudServiceImpl();
        }
    }

}
