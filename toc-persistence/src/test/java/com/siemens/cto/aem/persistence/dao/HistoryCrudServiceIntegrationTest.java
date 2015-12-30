package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.common.domain.model.group.Group;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.impl.HistoryCrudServiceImpl;
import com.siemens.cto.aem.persistence.jpa.service.HistoryCrudService;
import com.siemens.cto.aem.persistence.jpa.type.EventType;
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

/**
 * Integration test for {@link HistoryCrudService}.
 *
 * Created by JC043760 on 11/30/2015.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {HistoryCrudServiceIntegrationTest.Config.class, TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class HistoryCrudServiceIntegrationTest {

    @Configuration
    static class Config {

        @Bean
        HistoryCrudService getHistoryDao() {
            return new HistoryCrudServiceImpl();
        }

    }

    @Autowired
    private HistoryCrudService historyCrudService;

    @Test
    public void testWriteHistory() {
        historyCrudService.createHistory("any", null, "Testing...", EventType.USER_ACTION, "any");
    }

}
