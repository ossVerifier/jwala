package com.siemens.cto.aem.persistence.dao;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.dao.impl.HistoryDaoImpl;
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
 * Integration test for {@link HistoryDao}.
 *
 * Created by JC043760 on 11/30/2015.
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
        classes = {HistoryDaoIntegrationTest.Config.class, TestJpaConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class HistoryDaoIntegrationTest {

    @Configuration
    static class Config {

        @Bean
        HistoryDao getHistoryDao() {
            return new HistoryDaoImpl();
        }

    }

    @Autowired
    private HistoryDao historyDao;

    @Test
    public void testWriteHistory() {
        historyDao.createHistory("any", null, "Testing...", EventType.USER_ACTION, "any");
    }

}
