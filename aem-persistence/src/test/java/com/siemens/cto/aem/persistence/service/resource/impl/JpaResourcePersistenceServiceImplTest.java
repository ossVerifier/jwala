package com.siemens.cto.aem.persistence.service.resource.impl;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.AemPersistenceServiceConfiguration;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.group.GroupCrudService;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceService;
import com.siemens.cto.aem.persistence.service.resource.ResourcePersistenceServiceTest;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by z003e5zv on 3/25/2015.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {AemPersistenceServiceConfiguration.class,
                TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class JpaResourcePersistenceServiceImplTest extends ResourcePersistenceServiceTest {

        @Autowired
        private ResourcePersistenceService resourcePersistenceService;

        @Autowired
        private GroupCrudService groupCrudService;

        @Override
        protected ResourcePersistenceService getResourcePersistenceService() {
                return this.resourcePersistenceService;
        }

        @Override
        protected GroupCrudService getGroupCrudService() {
                return this.groupCrudService;
        }

}
