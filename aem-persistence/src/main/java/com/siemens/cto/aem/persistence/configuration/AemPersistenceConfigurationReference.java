package com.siemens.cto.aem.persistence.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({AemJpaConfiguration.class,
         AemDaoConfiguration.class,
         AemDataSourceConfiguration.class,
         AemPersistenceServiceConfiguration.class})
public class AemPersistenceConfigurationReference {
}
