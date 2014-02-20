package com.siemens.cto.aem.service.configuration.application;

import com.siemens.cto.aem.persistence.dao.JvmDaoJpa;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoConfig {

    @Bean
    public JvmDaoJpa jvmDaoJpa() {
        return new JvmDaoJpa();
    }

}
