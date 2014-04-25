package com.siemens.cto.aem.persistence.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.persistence.configuration.listener.PersistenceApplicationListener;

@Configuration
@Import({AemJpaConfiguration.class,
         AemDaoConfiguration.class,
         AemDataSourceConfiguration.class,
         AemPersistenceServiceConfiguration.class})
public class AemPersistenceConfigurationReference {
    
    public AemPersistenceConfigurationReference() {}
    
    @Bean
    public PersistenceApplicationListener getPersistenceApplicationListener() {
        return new PersistenceApplicationListener();
    }
    
}
