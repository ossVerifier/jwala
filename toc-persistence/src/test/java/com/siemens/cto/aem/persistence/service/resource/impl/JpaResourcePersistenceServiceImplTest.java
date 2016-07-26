package com.siemens.cto.aem.persistence.service.resource.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.ApplicationCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupCrudService;
import com.siemens.cto.aem.persistence.jpa.service.GroupJvmRelationshipService;
import com.siemens.cto.aem.persistence.jpa.service.JvmCrudService;
import com.siemens.cto.aem.persistence.jpa.service.impl.ApplicationCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.GroupJvmRelationshipServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.impl.JvmCrudServiceImpl;
import com.siemens.cto.aem.persistence.service.ApplicationPersistenceService;
import com.siemens.cto.aem.persistence.service.GroupPersistenceService;
import com.siemens.cto.aem.persistence.service.ResourcePersistenceService;
import com.siemens.cto.aem.persistence.service.impl.JpaApplicationPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.impl.JpaGroupPersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.impl.JpaResourcePersistenceServiceImpl;
import com.siemens.cto.aem.persistence.service.resource.AbstractResourcePersistenceServiceTest;
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
        classes = {JpaResourcePersistenceServiceImplTest.Config.class
        })

public class JpaResourcePersistenceServiceImplTest extends AbstractResourcePersistenceServiceTest{

    @Configuration
    @Import(TestJpaConfiguration.class)

    static class Config {

        @Bean
        public ResourcePersistenceService getResourcePersistenceService(){
            return new JpaResourcePersistenceServiceImpl();
        }

        @Bean
        public GroupPersistenceService getGroupPersistenceService() {
            return new JpaGroupPersistenceServiceImpl(getGroupCrudService(), getGroupJvmRelationshipService());
        }

        @Bean
        public GroupCrudService getGroupCrudService() {
            return new GroupCrudServiceImpl();
        }

        @Bean
        public GroupJvmRelationshipService getGroupJvmRelationshipService() {
            return new GroupJvmRelationshipServiceImpl(getGroupCrudService(), getJvmCrudService());
        }

        @Bean
        public JvmCrudService getJvmCrudService() {
            return new JvmCrudServiceImpl();
        }

        @Bean
        public ApplicationPersistenceService getApplicationPersistenceService(){
            return new JpaApplicationPersistenceServiceImpl(getApplicationCrudService(), getGroupCrudService());
        }

        @Bean
        public ApplicationCrudService getApplicationCrudService() {
            return new ApplicationCrudServiceImpl();
        }
    }
}
