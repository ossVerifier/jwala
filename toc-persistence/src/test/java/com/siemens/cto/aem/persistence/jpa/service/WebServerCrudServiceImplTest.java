package com.siemens.cto.aem.persistence.jpa.service;

import com.siemens.cto.aem.common.configuration.TestExecutionProfile;
import com.siemens.cto.aem.persistence.configuration.TestJpaConfiguration;
import com.siemens.cto.aem.persistence.jpa.service.impl.WebServerCrudServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by JC043760 on 12/16/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@EnableTransactionManagement
@IfProfileValue(name = TestExecutionProfile.RUN_TEST_TYPES, value = TestExecutionProfile.INTEGRATION)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class,
                      classes = {WebServerCrudServiceImplTest.Config.class})
public class WebServerCrudServiceImplTest {

    @Autowired
    private WebServerCrudServiceImpl impl;

    @Test
    public void testCreateWebServer() {

    }

    @Test
    public void testUpdateWebServer() {

    }

    @Configuration
    @Import(TestJpaConfiguration.class)
    static class Config {
        @Bean
        public WebServerCrudServiceImpl getWebServerStateCrudServiceImpl() {
            return new WebServerCrudServiceImpl();
        }
    }

}
