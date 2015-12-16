package com.siemens.cto.aem.persistence.dao.webserver.impl.jpa;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.dao.GroupDao;
import com.siemens.cto.aem.persistence.dao.impl.JpaGroupDaoImpl;
import com.siemens.cto.aem.persistence.dao.impl.JpaWebServerDaoImpl;
import com.siemens.cto.aem.persistence.dao.webserver.AbstractWebServerDaoIntegrationTest;
import com.siemens.cto.aem.persistence.dao.WebServerDao;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {
		JpaWebServerDaoIntegrationTest.CommonConfiguration.class,
		TestJpaConfiguration.class })
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@RunWith(SpringJUnit4ClassRunner.class)
@EnableTransactionManagement
@Transactional
public class JpaWebServerDaoIntegrationTest extends AbstractWebServerDaoIntegrationTest {

	@Configuration
	static class CommonConfiguration {

		@Bean
		public WebServerDao getWebServerDao() {
			return new JpaWebServerDaoImpl();
		}

		@Bean
		public GroupDao getGroupDao() {
			return new JpaGroupDaoImpl();
		}
}
}
