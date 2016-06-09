package com.siemens.cto.aem.persistence.service.webserver.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.WebServerCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.WebServerPersistenceService;
import com.siemens.cto.aem.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.impl.WebServerPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.webserver.AbstractWebServerPersistenceServiceTest;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {JpaWebServerPersistenceServiceImplTest.Config.class
        })
public class JpaWebServerPersistenceServiceImplTest extends AbstractWebServerPersistenceServiceTest{

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {

        @Bean
        public WebServerPersistenceService getWebServerPersistenceService(){
            return new WebServerPersistenceServiceImpl(getGroupCrudServiceImpl(), getWebServerCrudServiceImpl());
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService(){
            return new JpaGroupPersistenceServiceImpl(getGroupCrudServiceImpl(), getGroupJvmRelationshipService());
        }

        @Bean
        public GroupCrudService getGroupCrudServiceImpl(){
            return new GroupCrudServiceImpl();
        }

        @Bean
        public WebServerCrudService getWebServerCrudServiceImpl(){
            return new WebServerCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService(){
            return new GroupJvmRelationshipServiceImpl(getGroupCrudServiceImpl(), getJvmCrudServiceImpl());
        }

        @Bean
        public JvmCrudService getJvmCrudServiceImpl(){
            return new JvmCrudServiceImpl();
        }

    }
}
