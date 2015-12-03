package com.siemens.cto.aem.persistence.dao.group.impl.jpa;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.dao.group.AbstractGroupDaoIntegrationTest;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {JpaGroupDaoIntegrationTest.CommonConfiguration.class,
                                 TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class JpaGroupDaoIntegrationTest extends AbstractGroupDaoIntegrationTest {

    @Configuration
    static class CommonConfiguration {

        @Bean
        public GroupDao getGroupDao() {
            return new JpaGroupDaoImpl();
        }
    }
}
