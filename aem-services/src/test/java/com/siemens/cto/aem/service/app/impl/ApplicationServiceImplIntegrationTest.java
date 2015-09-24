package com.siemens.cto.aem.service.app.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.group.Group;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.ssh.SshConfiguration;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.app.impl.jpa.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.dao.webserver.impl.jpa.JpaWebServerDaoImpl;
import com.siemens.cto.aem.persistence.jpa.service.app.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.group.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.groupjvm.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.jvm.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.app.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.app.impl.JpaApplicationPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.jvm.JvmPersistenceService;
import com.siemens.cto.aem.persistence.service.jvm.impl.JpaJvmPersistenceServiceImpl;
import com.siemens.cto.aem.service.app.ApplicationCommandService;
import com.siemens.cto.aem.service.app.ApplicationService;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.service.webserver.component.ClientFactoryHelper;
import com.siemens.cto.aem.si.ssl.hc.HttpClientRequestFactory;
import org.junit.Before;
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

import java.util.List;

import static org.junit.Assert.assertEquals;


@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
    ApplicationServiceImplIntegrationTest.CommonConfiguration.class,
        TestJpaConfiguration.class })
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class ApplicationServiceImplIntegrationTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        public WebServerDao getWebServerDao() {
            return new JpaWebServerDaoImpl();
        }

        @Bean
        public ApplicationPersistenceService getApplicationPersistenceService() {
            return new JpaApplicationPersistenceServiceImpl(new ApplicationCrudServiceImpl(), new GroupCrudServiceImpl());
        }
    
        @Bean
        public ApplicationDao getApplicationDao() {
            return new JpaApplicationDaoImpl();
        }

        @Bean
        public GroupDao getGroupDao() {
            return new JpaGroupDaoImpl();
        }

        @Bean
        @Autowired
        public JvmPersistenceService getJvmPersistenceService(final GroupJvmRelationshipService groupJvmRelationshipService) {
            return new JpaJvmPersistenceServiceImpl(new JvmCrudServiceImpl(), groupJvmRelationshipService);
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(new GroupCrudServiceImpl(), new JvmCrudServiceImpl());
        }

        @Bean
        public ApplicationCommandService getApplicationCommandService() {
            final SshConfiguration sshConfiguration = new SshConfiguration("z003bpej", 22, "", "");
            return new ApplicationCommandServiceImpl(sshConfiguration);
        }

        @Bean(name = "webServerHttpRequestFactory")
        public HttpClientRequestFactory getHttpClientRequestFactory() throws Exception {
            return new HttpClientRequestFactory();
        }

        @Bean
        public ClientFactoryHelper getClientFactoryHelper() {
            return new ClientFactoryHelper();
        }

    }
    
    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationPersistenceService applicationPersistenceService;
    
    private ApplicationService cut;

    // @Autowired
    private JvmPersistenceService jvmPersistenceService;

    @Autowired
    private ClientFactoryHelper clientFactoryHelper;

    // @Autowired
    private ApplicationCommandService applicationCommandService;

    @Before
    public void setup() { 
        cut = new ApplicationServiceImpl(applicationDao, applicationPersistenceService, jvmPersistenceService,
                                         clientFactoryHelper, applicationCommandService, null);
    }

    /**
     * With this revision there is no capacity to create. Therefore integration
     * testing at the service layer can only return NotFound.
     * We'll mock the rest.
     */
    @Test(expected = NotFoundException.class)
    public void testGetApplication() {
        cut.getApplication(new Identifier<Application>(0L));
    }

    /**
     * Test getting the full list.
     */
    @Test
    public void testGetAllApplications() {
        List<Application> all = cut.getApplications();
        assertEquals(0, all.size());
    }
    
    /**
     * Test getting the partial list.
     */
    @Test
    public void testGetApplicationsByGroup() {
        List<Application> partial = cut.findApplications(new Identifier<Group>(0L));
        assertEquals(0, partial.size());
    }

}
