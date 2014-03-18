package com.siemens.cto.aem.persistence.dao.jvm;

import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.jvm.impl.JpaJvmDaoImpl;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {JpaJvmDaoIntegrationTest.CommonConfiguration.class,
                                 TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class JpaJvmDaoIntegrationTest extends AbstractJvmDaoIntegrationTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        public GroupDao getGroupDao() {
            return new JpaGroupDaoImpl();
        }

        @Bean
        public JvmDao getJvmDao() {
            return new JpaJvmDaoImpl();
        }
    }
}
