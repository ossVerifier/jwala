package com.siemens.cto.aem.service.configuration.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import com.siemens.cto.aem.persistence.configuration.AemJpaConfiguration;

@Configuration
@EnableTransactionManagement
public class AemTransactionConfiguration {

    @Autowired
    private AemJpaConfiguration jpaConfiguration;

    @Bean(name = "transactionManager")
    public PlatformTransactionManager getPlatformTransactionManager() {
        final JtaTransactionManager transactionManager = new JtaTransactionManager();
        transactionManager.setDefaultTimeout(30);
        return transactionManager;
    }
}
