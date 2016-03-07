package com.siemens.cto.aem.service.configuration.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.siemens.cto.aem.persistence.configuration.AemJpaConfiguration;

@Configuration
@EnableTransactionManagement
public class AemTransactionConfiguration {

    @Autowired
    private AemJpaConfiguration jpaConfiguration;

    @Bean(name = "txManager")
    @Qualifier("txManager")
    public PlatformTransactionManager getPlatformTransactionManager() {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDefaultTimeout(30);
        transactionManager.setEntityManagerFactory(jpaConfiguration.getEntityManagerFactory());
        return transactionManager;
    }

}
