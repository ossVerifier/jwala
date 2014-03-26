package com.siemens.cto.aem.service.configuration.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.siemens.cto.aem.persistence.configuration.AemPersistenceConfigurationReference;
import com.siemens.cto.aem.service.configuration.service.AemServiceConfiguration;

@Configuration
@Import({AemServiceConfiguration.class,
         AemPersistenceConfigurationReference.class})
public class ApacheEnterpriseManagerServiceAppConfigReference {

}
