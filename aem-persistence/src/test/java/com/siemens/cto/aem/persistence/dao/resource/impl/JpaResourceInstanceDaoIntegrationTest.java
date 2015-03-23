package com.siemens.cto.aem.persistence.dao.resource.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.domain.model.resource.ResourceInstance;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.dao.resource.AbstractResourceDaoIntegrationTest;
import com.siemens.cto.aem.persistence.dao.resource.ResourceInstanceDao;
import com.siemens.cto.aem.persistence.dao.resource.impl.jpa.JpaResourceInstanceDaoImpl;
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

/**
 * Created by z003e5zv on 3/20/2015.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {JpaResourceInstanceDaoIntegrationTest.CommonConfiguration.class,
                TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class JpaResourceInstanceDaoIntegrationTest extends AbstractResourceDaoIntegrationTest {

    @Autowired
    private ResourceInstanceDao resourceInstanceDao;

    @Configuration
    static class CommonConfiguration {

        @Bean
        public JpaResourceInstanceDaoImpl getGroupDao() {
            return new JpaResourceInstanceDaoImpl();
        }
    }

    @Override
    protected ResourceInstanceDao getResourceInstanceDao() {
        return this.resourceInstanceDao;
    }
}
