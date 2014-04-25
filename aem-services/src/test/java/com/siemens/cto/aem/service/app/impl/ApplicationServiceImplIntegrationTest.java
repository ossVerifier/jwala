package com.siemens.cto.aem.service.app.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

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

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.exception.NotFoundException;
import com.siemens.cto.aem.domain.model.app.Application;
import com.siemens.cto.aem.domain.model.id.Identifier;
import com.siemens.cto.aem.domain.model.temporary.PaginationParameter;
import com.siemens.cto.aem.persistence.dao.app.ApplicationDao;
import com.siemens.cto.aem.persistence.dao.app.impl.jpa.JpaApplicationDaoImpl;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.jpa.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.webserver.WebServerDao;
import com.siemens.cto.aem.persistence.dao.webserver.impl.jpa.JpaWebServerDaoImpl;
import com.siemens.cto.aem.service.configuration.TestJpaConfiguration;


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
        public ApplicationDao getApplicationDao() {
            return new JpaApplicationDaoImpl();
        }

        @Bean
        public GroupDao getGroupDao() {
            return new JpaGroupDaoImpl();
        }
    }   
    
    @Autowired
    private ApplicationDao           applicationDao;
    
    private ApplicationServiceImpl   cut;

    @Before
    public void setup() { 
        cut = new ApplicationServiceImpl(applicationDao);
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
     * With this revision there is no capacity to create. Therefore integration
     * testing at the service layer can only return NotFound.
     * We'll mock the rest.
     */
    @Test
    public void testGetAllApplications() {
        List<Application> all = cut.getApplications(PaginationParameter.all());
        assertEquals(0, all.size());
    }

}
