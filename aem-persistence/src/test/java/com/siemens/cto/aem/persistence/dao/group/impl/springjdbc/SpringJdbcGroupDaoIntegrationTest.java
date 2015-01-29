package com.siemens.cto.aem.persistence.dao.group.impl.springjdbc;

import javax.sql.DataSource;

import org.h2.Driver;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.siemens.cto.aem.common.configuration.ConfigurationProfile;
import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.dao.group.AbstractGroupDaoIntegrationTest;
import com.siemens.cto.aem.persistence.dao.group.GroupDao;
import com.siemens.cto.aem.persistence.dao.group.impl.springjdbc.SpringJdbcGroupDaoImpl;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {SpringJdbcGroupDaoIntegrationTest.CommonConfiguration.class,
                                 SpringJdbcGroupDaoIntegrationTest.IntegrationConfiguration.class,
                                 SpringJdbcGroupDaoIntegrationTest.LocalDevConfiguration.class})
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class SpringJdbcGroupDaoIntegrationTest extends AbstractGroupDaoIntegrationTest {

    @Configuration
    static class CommonConfiguration {

        @Autowired
        private DataSource transactionDataSource;

        @Bean(name = "transactionManager")
        public PlatformTransactionManager getTransactionManager() {
            return new DataSourceTransactionManager(transactionDataSource);
        }

        @Bean
        public GroupDao getGroupDao() {
            return new SpringJdbcGroupDaoImpl(transactionDataSource);
        }
    }

    @Configuration
    @Profile({ConfigurationProfile.LOCAL_DEVELOPMENT,
              ConfigurationProfile.DEFAULT})
    static class LocalDevConfiguration {

        @Bean
        public DataSource getDataSource() {
            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource(new Driver(),
                                                                                 "jdbc:h2:mem:test-persistence;DB_CLOSE_DELAY=-1;LOCK_MODE=0",
                                                                                 "sa",
                                                                                 "");
            return dataSource;
        }
    }

    @Configuration
    @Profile(ConfigurationProfile.INTEGRATION)
    static class IntegrationConfiguration {

        @Bean
        public DataSource getDataSource() {
            final SimpleDriverDataSource dataSource = new SimpleDriverDataSource(new Driver(),
                                                                                 "jdbc:h2:mem:test-persistence;DB_CLOSE_DELAY=-1;LOCK_MODE=0",
                                                                                 "sa",
                                                                                 "");
            return dataSource;
        }
    }
}
