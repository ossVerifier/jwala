package com.siemens.cto.aem.service.webserver.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.id.Identifier;
import com.siemens.cto.aem.common.domain.model.webserver.WebServer;
import com.siemens.cto.aem.common.domain.model.webserver.WebServerReachableState;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.StatePersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.WebServerPersistenceServiceImpl;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.spring.component.GrpStateComputationAndNotificationSvc;
import com.siemens.cto.aem.service.state.StateNotificationService;
import com.siemens.cto.aem.service.state.StateService;
import com.siemens.cto.aem.service.webserver.WebServerService;
import com.siemens.cto.toc.files.FileManager;
import com.siemens.cto.toc.files.configuration.TocFileManagerConfigReference;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.mock;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
        WebServerServiceImplIntegrationTest.CommonConfiguration.class,
        TestJpaConfiguration.class, TocFileManagerConfigReference.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class WebServerServiceImplIntegrationTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService() {
            return new WebServerPersistenceServiceImpl(getGroupCrudService(), getWebServerCrudService());
        }

        @Bean
        public WebServerCrudService getWebServerCrudService() {
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

    }

    @Autowired
    private WebServerPersistenceService webServerPersistenceService;

    private WebServerService webServerService;

    @Autowired
    private FileManager fileManager;

    @Autowired
    private StateService<WebServer, WebServerReachableState> webServerStateService;

    @Before
    public void setup() {
        webServerService = new WebServerServiceImpl(webServerPersistenceService, fileManager);
    }

    @Test(expected = NotFoundException.class)
    @Ignore
    // TODO: Fix this!
    public void testServiceLayer() {
        webServerService.getWebServer(new Identifier<WebServer>(0L));
    }
}
