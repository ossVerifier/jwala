package com.siemens.cto.aem.persistence.configuration;

import com.siemens.cto.aem.persistence.configuration.listener.PersistenceApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemJpaConfiguration.class,
         AemDaoConfiguration.class,
         AemPersistenceServiceConfiguration.class})
public class AemPersistenceConfigurationReference {
    
    public AemPersistenceConfigurationReference() {}

    @Bean
    public PersistenceApplicationListener getPersistenceApplicationListener() {
        return new PersistenceApplicationListener();
    }
    
}
